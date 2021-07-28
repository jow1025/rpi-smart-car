# rpi-smart-car
자율주행, 원격주행, 대기환경 데이터 수집, 화재감시를 할 수 있는 라즈베리파이 자동차
<br>
- <h3> 하드웨어(보드)
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
