import pyrebase
import serial
from pyfcm import FCMNotification
APIKEY="AAAAalMeKts:APA91bEiB12GcGeo5W0MmzOjjmcDiR9LwrVgUxmspbWpI4eZz0LjuFIuTVxnfCbqd_IoeMjVkqJt5BGe9V77gvzFLmfSj5utQtj_0C0B0Y3LYM9nFytYpgDA_RV4HouwU-Qp7t8RwWMd"
ser=serial.Serial("/dev/ttyUSB0",9600)
push_service = FCMNotification(api_key=APIKEY)
config={
	"apiKey":"kk4ks2sHVBKCA5TExxZPjEiqlNmJOdUywZN4At5g",
	"authDomain": "project-8965d.firebaseapp.com",
	"databaseURL": "https://project-8965d-default-rtdb.firebaseio.com",
	"storageBucket": "gs://project-8965d.appspot.com"
}
firebase=pyrebase.initialize_app(config)
db=firebase.database()
while True:
	q=ser.readline()
	val=ser.readline().strip()
	val=val.decode()
	a=list(val.split())

	dust=float(a[0])
	gas=int(a[1])
	temp=float(a[2])
	humid=float(a[3])
	
	data={
		"dust":dust,
		"gas":gas,
		"temp":temp,
		"humid":humid
	}
	db.child("data_a").child("1-set").set(dust)
	db.child("data_a").child("2-push").push(dust)

	db.child("data_b").child("1-set").set(gas)
	db.child("data_b").child("2-push").push(gas)

	db.child("data_c").child("1-set").set(temp)
	db.child("data_c").child("2-push").push(temp)

	db.child("data_d").child("1-set").set(humid)
	db.child("data_d").child("2-push").push(humid)
	
	print(dust)
	print(gas)
	print(temp)
	print(humid)
