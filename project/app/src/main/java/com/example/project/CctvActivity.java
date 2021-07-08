package com.example.project;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CctvActivity extends AppCompatActivity{
    //EditText editTextEmail;
    //EditText editTextPassword;
    //Button buttonSignup;
    //TextView textviewSingin;
    //TextView textviewMessage;
    //on create 밖에 onclik으로 분기할때는 private 하고 oncreate안에 작성할거면 private지우기
    // oncreate밖에 작성할 때는 calltext.setOnclickListner(this)지우기
    //안에 작성할 때는 calltext.seronclick~~{}
    private Button callButton,cctvOnButton,cctvOffButton;
    private ImageButton carUpButton,carDownButton,carleftButton,carRightButton;
    private Switch LEDswitch,MotorSwitch,BuzzerSwitch;
    private RadioGroup led_radioGroup;
    private RadioButton led_red,led_green,led_blue;
    ProgressDialog progressDialog;

    private DatabaseReference mdust_ref,mgas_ref,mhumid_ref,mtemp_ref;      //push값, 즉 두번째 값부터~
    private DatabaseReference fdust_ref,fgas_ref,fhumid_ref,ftemp_ref;      //set값, 즉 첫번째 값들

    private LineChart Dust_chart,Gas_chart,Temp_chart,Humid_chart;
    private Thread dust_thread,gas_thread,temp_thread,humid_thread;

    private LineData data_dust,data_temp,data_humid,data_gas;
    private ILineDataSet set_dust,set_gas,set_humid,set_temp;

    private TextView f_dust,f_temp,f_humid,f_gas;                           //파이어베이스 set 초기값


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        //callText=findViewById(R.id.callText);
        //cctvOnButton=findViewById(R.id.cctvOnButton);
        //cctvOffButton=findViewById(R.id.cctvOffButton);

        //mdust_ref = FirebaseDatabase.getInstance().getReference();  //파이어베이스 래퍼런스


        callButton = (Button) findViewById(R.id.callButton);           //신고하기버튼
        carUpButton = (ImageButton) findViewById(R.id.UpButton);       //DC모터 UP
        carDownButton = (ImageButton) findViewById(R.id.DownButton);   //DC모터 down
        carleftButton = (ImageButton) findViewById(R.id.LeftButton);   //DC모터 left
        carRightButton = (ImageButton) findViewById(R.id.RightButton); //DC모터 right

        //MotorSwitch=(Switch)findViewById(R.id.motorSwitch);
        LEDswitch = (Switch) findViewById(R.id.ledswitch);             //LED 스위치 : 켜짐과 동시에 RGB라디오버튼이 보인다. 꺼지면 초기화됨(체크했던게 다 풀림)
        BuzzerSwitch = (Switch) findViewById(R.id.buzzerSwitch);       //buzzer 스위치

        led_radioGroup = findViewById(R.id.LED_radioGroup);           //라디오그룹
        led_red = findViewById(R.id.led_red);                         //라디오버튼 red
        led_blue = findViewById(R.id.led_blue);                       //라디오버튼 blue
        led_green = findViewById(R.id.led_green);                     //라디오버튼 green

        Dust_chart = (LineChart) findViewById(R.id.dust_chart);        //미세먼지 차트
        Gas_chart = (LineChart) findViewById(R.id.gas_chart);          //가스 차트
        Temp_chart = (LineChart) findViewById(R.id.temp_chart);        //온도 차트
        Humid_chart = (LineChart) findViewById(R.id.humid_chart);      //습도 차트

        f_dust=(TextView)findViewById(R.id.tv_fdust);
        f_gas=(TextView)findViewById(R.id.tv_fgas);
        f_humid=(TextView)findViewById(R.id.tv_fhumid);
        f_temp=(TextView)findViewById(R.id.tv_ftemp);

        //dust 초기값 가져오기
        fdust_ref=FirebaseDatabase.getInstance().getReference("data_a").child("1-set");
        fdust_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                String dust_data = dataSnapshot.getValue().toString();
                Log.d("fdust", dust_data);
                f_dust.setText(dust_data);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        });

        //temp 초기값 가져오기
        ftemp_ref=FirebaseDatabase.getInstance().getReference("data_c").child("1-set");
        ftemp_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                String temp_data = dataSnapshot.getValue().toString();
                Log.d("ftemp", temp_data);
                f_temp.setText(temp_data);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        });

        //gas 초기값 가져오기
        fgas_ref=FirebaseDatabase.getInstance().getReference("data_b").child("1-set");
        fgas_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                String gas_data = dataSnapshot.getValue().toString();
                Log.d("fgas", gas_data);
                f_gas.setText(gas_data);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        });

        //humid 초기값 가져오기
        fhumid_ref=FirebaseDatabase.getInstance().getReference("data_d").child("1-set");
        fhumid_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                String humid_data = dataSnapshot.getValue().toString();
                Log.d("fhumid", humid_data);
                f_humid.setText(humid_data);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        });

        WebView webView = (WebView) findViewById(R.id.cctvWeb);
        String url = "http://192.168.0.26:8091/?action=stream";
        webView.loadUrl(url);
        webView.setPadding(0, 0, 0, 0);
        //webView.setInitialScale(100);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        LEDswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean isChecked) {
                if (isChecked) {
                    led_radioGroup.setVisibility(View.VISIBLE);
                    buzzerOn();
                    Toast.makeText(CctvActivity.this, "LED켜짐", Toast.LENGTH_SHORT).show();
                } else {
                    led_radioGroup.setVisibility(View.INVISIBLE);
                    Toast.makeText(CctvActivity.this, "LED꺼짐", Toast.LENGTH_SHORT).show();
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://192.168.0.26:5000/buzzer_off")
                            .build();
                    client.newCall(request).enqueue(new Callback() {

                        @Override
                        public void onFailure(Call call, IOException e) {
                            call.cancel();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            final String myResponse = response.body().string();
                            CctvActivity.this.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    cctvOnButton.setText(myResponse);
                                }
                            });
                        }
                    });
                    //led_blue.setChecked(false);
                    // led_red.setChecked(false);
                    // led_green.setChecked(false);
                }
            }
        });



        BuzzerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean isChecked) {
                if (isChecked) {
                    //mDatabase.child("buzzerState").setValue(1);
                    //Toast.makeText(getApplication(),"Buzzer On",Toast.LENGTH_SHORT);
                } else {
                    //mDatabase.child("buzzerState").setValue(0);
                    //Toast.makeText(getApplication(),"Buzzer Off",Toast.LENGTH_SHORT);
                }
            }
        });


        led_radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.led_blue) {
                    //mDatabase.child("led_State").child("BLUE").setValue(1);
                    //mDatabase.child("led_State").child("RED").setValue(0);
                    //mDatabase.child("led_State").child("GREEN").setValue(0);
                } else if (i == R.id.led_red) {
                    //mDatabase.child("led_State").child("BLUE").setValue(0);
                    // mDatabase.child("led_State").child("RED").setValue(1);
                    //mDatabase.child("led_State").child("GREEN").setValue(0);
                } else {
                    //mDatabase.child("led_State").child("BLUE").setValue(0);
                    //mDatabase.child("led_State").child("RED").setValue(0);
                    // mDatabase.child("led_State").child("GREEN").setValue(1);
                }
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CctvActivity.this);
                builder.setTitle("신고");
                builder.setMessage("신고하시겠습니까?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:119"));
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });




        Dust_chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        Dust_chart.getAxisRight().setEnabled(false);
        Dust_chart.getLegend().setTextColor(Color.WHITE);
        Dust_chart.animateXY(2000, 2000);
        Dust_chart.invalidate();
        LineData data1 = new LineData();
        Dust_chart.setData(data1);
        DrawingGraph_dust();

        Humid_chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        Humid_chart.getAxisRight().setEnabled(false);
        Humid_chart.getLegend().setTextColor(Color.WHITE);
        Humid_chart.animateXY(2000, 2000);
        Humid_chart.invalidate();
        LineData data2 = new LineData();
        Humid_chart.setData(data2);
        DrawingGraph_humid();

        Gas_chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        Gas_chart.getAxisRight().setEnabled(false);
        Gas_chart.getLegend().setTextColor(Color.WHITE);
        Gas_chart.animateXY(2000, 2000);
        Gas_chart.invalidate();
        LineData data3 = new LineData();
        Gas_chart.setData(data3);
        DrawingGraph_gas();

        Temp_chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        Temp_chart.getAxisRight().setEnabled(false);
        Temp_chart.getLegend().setTextColor(Color.WHITE);
        Temp_chart.animateXY(2000, 2000);
        Temp_chart.invalidate();
        LineData data4 = new LineData();
        Temp_chart.setData(data4);
        DrawingGraph_temp();
    }



    public void buzzerOn(){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder()
                .url("http://192.168.0.26:5000/buzzer_on")
                .build();
        client.newCall(request).enqueue(new Callback(){

            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String myResponse=response.body().string();
                CctvActivity.this.runOnUiThread(new Runnable(){

                    @Override
                    public void run() {
                        cctvOnButton.setText(myResponse);

                    }
                });
            }

        });
    }




    private void addEntry_dust(){

        data_dust=Dust_chart.getData();

        if(data_dust!=null){
            set_dust=data_dust.getDataSetByIndex(0);

            if(set_dust==null){
                set_dust=createDustSet();
                data_dust.addDataSet(set_dust);
            }

            //데이터 가져오기
            mdust_ref=FirebaseDatabase.getInstance().getReference("data_a").child("2-push");

            ChildEventListener mChild=new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if(snapshot==null){
                        return;
                    }
                    String dust_data = snapshot.getValue().toString();
                    Log.d("dust", dust_data);
                    //data_dust.addEntry(new Entry(set_dust.getEntryCount(),Float.parseFloat(dust_data)),0);
                    data_dust.addEntry(new Entry(set_dust.getEntryCount(),Float.parseFloat(dust_data)),0);
                    data_dust.notifyDataChanged();
                    Dust_chart.notifyDataSetChanged();
                    Dust_chart.setVisibleXRangeMaximum(500);
                    Dust_chart.moveViewToX(data_dust.getEntryCount());
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }
                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }
                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };


            mdust_ref.addChildEventListener(mChild);
        }
    }

    private void addEntry_temp(){

        data_temp=Temp_chart.getData();

        if(data_temp!=null){
            set_temp=data_temp.getDataSetByIndex(0);

            if(set_temp==null){
                set_temp=createTempSet();
                data_temp.addDataSet(set_temp);
            }

            //데이터 가져오기
            mtemp_ref=FirebaseDatabase.getInstance().getReference("data_c").child("2-push");

            ChildEventListener mChild=new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if(snapshot==null){
                        return;
                    }
                    String dust_temp = snapshot.getValue().toString();
                    Log.d("temp", dust_temp);
                    //data_dust.addEntry(new Entry(set_dust.getEntryCount(),Float.parseFloat(dust_data)),0);
                    data_temp.addEntry(new Entry(set_temp.getEntryCount(),Float.parseFloat(dust_temp)),0);
                    data_temp.notifyDataChanged();
                    Temp_chart.notifyDataSetChanged();
                    Temp_chart.setVisibleXRangeMaximum(500);
                    Temp_chart.moveViewToX(data_temp.getDataSetCount());
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }
                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }
                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };


            mtemp_ref.addChildEventListener(mChild);
        }
    }

    private void addEntry_humid(){

        data_humid=Humid_chart.getData();

        if(data_humid!=null){
            set_humid=data_humid.getDataSetByIndex(0);

            if(set_humid==null){
                set_humid=createHumidSet();
                data_humid.addDataSet(set_humid);
            }

            //데이터 가져오기
            mhumid_ref=FirebaseDatabase.getInstance().getReference("data_d").child("2-push");

            ChildEventListener mChild1=new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if(snapshot==null){
                        return;
                    }
                    String humid_data = snapshot.getValue().toString();
                    Log.d("humid", humid_data);
                    //data_dust.addEntry(new Entry(set_dust.getEntryCount(),Float.parseFloat(dust_data)),0);
                    data_humid.addEntry(new Entry(set_humid.getEntryCount(),Float.parseFloat(humid_data)),0);
                    data_humid.notifyDataChanged();
                    Humid_chart.notifyDataSetChanged();
                    Humid_chart.setVisibleXRangeMaximum(500);
                    Humid_chart.moveViewToX(data_humid.getDataSetCount());
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }
                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }
                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };


            mhumid_ref.addChildEventListener(mChild1);
        }
    }

    private void addEntry_gas(){

        data_gas=Gas_chart.getData();

        if(data_gas!=null){
            set_gas=data_gas.getDataSetByIndex(0);

            if(set_gas==null){
                set_gas=createGasSet();
                data_gas.addDataSet(set_gas);
            }

            //데이터 가져오기
            mgas_ref=FirebaseDatabase.getInstance().getReference("data_b").child("2-push");

            ChildEventListener mChild=new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if(snapshot==null){
                        return;
                    }
                    String dust_data = snapshot.getValue().toString();
                    Log.d("dust", dust_data);
                    //data_dust.addEntry(new Entry(set_dust.getEntryCount(),Float.parseFloat(dust_data)),0);
                    data_gas.addEntry(new Entry(set_gas.getEntryCount(),Float.parseFloat(dust_data)),0);
                    data_gas.notifyDataChanged();
                    Gas_chart.notifyDataSetChanged();
                    Gas_chart.setVisibleXRangeMaximum(500);
                    Gas_chart.moveViewToX(data_gas.getDataSetCount());
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }
                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }
                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };


            mgas_ref.addChildEventListener(mChild);
        }
    }

    private LineDataSet createDustSet(){
        LineDataSet set = new LineDataSet(null, "Dust Data");
        set.setFillAlpha(110);
        set.setFillColor(Color.parseColor("#d7e7fa"));
        set.setColor(Color.parseColor("#0B80C9"));
        set.setCircleColor(Color.parseColor("#FFA1B4DC"));
        set.setCircleHoleColor(Color.BLUE);
        set.setValueTextColor(Color.WHITE);
        set.setDrawValues(false);
        set.setLineWidth(2);
        set.setCircleRadius(6);
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setValueTextSize(9f);
        set.setDrawFilled(true);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setLabel("ug/m");


        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setHighLightColor(Color.rgb(244, 117, 117));

        return set;
    }

    private LineDataSet createTempSet(){
        LineDataSet set = new LineDataSet(null, "Temp Data");
        set.setFillAlpha(110);
        set.setFillColor(Color.parseColor("#d7e7fa"));
        set.setColor(Color.parseColor("#0B80C9"));
        set.setCircleColor(Color.parseColor("#FFA1B4DC"));
        set.setCircleHoleColor(Color.BLUE);
        set.setValueTextColor(Color.WHITE);
        set.setDrawValues(false); set.setLineWidth(2);
        set.setCircleRadius(6); set.setDrawCircleHole(false);
        set.setDrawCircles(false); set.setValueTextSize(9f);
        set.setDrawFilled(true);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setLabel(".C");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setHighLightColor(Color.rgb(244, 117, 117));

        return set;
    }

    private LineDataSet createHumidSet(){
        LineDataSet set = new LineDataSet(null, "Humid Data");
        set.setFillAlpha(110);
        set.setFillColor(Color.parseColor("#d7e7fa"));
        set.setColor(Color.parseColor("#0B80C9"));
        set.setCircleColor(Color.parseColor("#FFA1B4DC"));
        set.setCircleHoleColor(Color.BLUE);
        set.setValueTextColor(Color.WHITE);
        set.setDrawValues(false); set.setLineWidth(2);
        set.setCircleRadius(6); set.setDrawCircleHole(false);
        set.setDrawCircles(false); set.setValueTextSize(9f);
        set.setDrawFilled(true);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setLabel("mm");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setHighLightColor(Color.rgb(244, 117, 117));

        return set;
    }

    private LineDataSet createGasSet(){
        LineDataSet set = new LineDataSet(null, "Gas Data");
        set.setFillAlpha(110);
        set.setFillColor(Color.parseColor("#d7e7fa"));
        set.setColor(Color.parseColor("#0B80C9"));
        set.setCircleColor(Color.parseColor("#FFA1B4DC"));
        set.setCircleHoleColor(Color.BLUE);
        set.setValueTextColor(Color.WHITE);
        set.setDrawValues(false); set.setLineWidth(2);
        set.setCircleRadius(6); set.setDrawCircleHole(false);
        set.setDrawCircles(false); set.setValueTextSize(9f);
        set.setDrawFilled(true);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setLabel("gg/m");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setHighLightColor(Color.rgb(244, 117, 117));

        return set;
    }

    private void DrawingGraph_dust(){
        if(dust_thread!=null){
            dust_thread.interrupt();
        }
        final Runnable runnable = new Runnable() {
            @Override public void run() {
                addEntry_dust();
            }
        };

        dust_thread=new Thread(new Runnable(){
            @Override
            public void run() {
                while(true){
                    runOnUiThread(runnable);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        dust_thread.start();
    }

    private void DrawingGraph_temp(){
        if(temp_thread!=null){
            temp_thread.interrupt();
        }
        final Runnable runnable = new Runnable() {
            @Override public void run() {
                addEntry_temp();
            }
        };

        temp_thread=new Thread(new Runnable(){
            @Override
            public void run() {
                while(true){
                    runOnUiThread(runnable);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        temp_thread.start();
    }

    private void DrawingGraph_humid(){
        if(humid_thread!=null){
            humid_thread.interrupt();
        }
        final Runnable runnable = new Runnable() {
            @Override public void run() {
                addEntry_humid();
            }
        };

        humid_thread=new Thread(new Runnable(){
            @Override
            public void run() {
                while(true){
                    runOnUiThread(runnable);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        humid_thread.start();
    }

    private void DrawingGraph_gas(){
        if(gas_thread!=null){
            gas_thread.interrupt();
        }
        final Runnable runnable = new Runnable() {
            @Override public void run() {
                addEntry_gas();
            }
        };

        gas_thread=new Thread(new Runnable(){
            @Override
            public void run() {
                while(true){
                    runOnUiThread(runnable);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        gas_thread.start();
    }
}



