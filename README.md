# rpi-smart-car
자율주행/원격주행이 가능하며 실시간으로 대기환경 데이터를 수집하고, 센서와 카메라로 실시간 화재감지를 할 수 있는 라즈베리파이 자동차입니다.
센서는 아두이노에 연결하여 시리얼 통신을 통해 라즈베리파이로 데이터를 전송하고 라즈베리파이에서 데이터를 수신하여 파이어베이스 DB에 센서 데이터를 저장합니다.
데이터는 Realtime DB에 저장되고 화재를 감시하여 촬영한 사진은 Firebase Storage에 저장됩니다. 
Flask 웹서버를 이용하여 안드로이드 앱에서 원격제어/자율제어가 가능하며 Firebase Storage에 저장된 사진을 조회할 수 있습니다.
또한 Realtime DB에 저장되어있는 데이터를 MPAndroidChart를 사용하여 실시간 그래프로 시각화하여 표현했습니다.

<br>
 <h3> 하드웨어(보드)
 1. Raspberry pi 3B
 2. Arduino nano v3
 3. PWM/Servo Hat
 
- <h3>센서 및 기타 
 1. DHT11(온/습도)
 2. Arduino flame sensor(불꽃)
 3. Piezo Buzzer(부저)
 4. 3color LED
 5. PPD42NS(미세먼지)
 6. MQ-2(가스)
 7. MG90 Servo
 8. 6v 직렬 건전지, 10000mAh 보조배터리

 
 - <h3>기술
  1. Rpi - Arduino 시리얼 통신(센서 데이터)
  2. Firebase Realtime DB (센서 데이터 저장을 위한 데이터베이스)
  3. Firebase Storage (사진 저장을 위한 스토리지)
  4. Firebase FCM (앱 알림 경보를 위한 푸쉬 메세지)
  5. Flask (앱에서 Rpi를 원격제어하기 위한 웹서버)
  6. Mjpg-streamer (실시간 스트리밍을 위한 스트리밍 서버)
  7. opencv, Haarcascade Algorithm (이미지 처리 라이브러리 + 불꽃 감지를 위한 알고리즘)
  8. MPandroidchart (앱에서 센서 데이터를 시각화 하기위한 라이브러리)
  9. Twilio Messaging (문자로 알림 서비스를 제공하기 위해 사용 -> 삭제)
  10. MultiThreading (데이터 송수신, 화재감지, 알람 경보 등을 동시에 수행하기 위한 쓰레드)
