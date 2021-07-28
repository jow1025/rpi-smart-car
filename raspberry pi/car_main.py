
import datetime
import firebase_admin
from firebase_admin import credentials
from firebase_admin import storage
import cv2 as cv
from uuid import uuid4
from pyfcm import FCMNotification
from flask import Flask
from gpiozero import Robot,DistanceSensor
from time import sleep
import random
import threading
APIKEY="AAAAalMeKts:APA91bEiB12GcGeo5W0MmzOjjmcDiR9LwrVgUxmspbWpI4eZz0LjuFIuTVxnfCbqd_IoeMjVkqJt5BGe9V77gvzFLmfSj5utQtj_0C0B0Y3LYM9nFytYpgDA_RV4HouwU-Qp7t8RwWMd"
TOKEN="dfjVL2OCTOOZDLE0VSBV1Q:APA91bEaotIIYMcGb3SYu8_UZn9z3MLg9oQ8eOE6OINuBP6EyT7SXq9WbFGH5GRKLIRbR-cyAV_qV2c1vLRT_i_QI7IzxgUFFkBxxl--PjpimTxc0DJTV-y7APm59nDCGpghWzTaVO3C"

push_service = FCMNotification(api_key=APIKEY)
PROJECT_ID = "project-8965d"
cred = credentials.Certificate(
    "/home/pi/Downloads/project-8965d-firebase-adminsdk-bkl6z-29507732d7.json")
default_app = firebase_admin.initialize_app(cred, {'storageBucket': f"{PROJECT_ID}.appspot.com"})
# 버킷은 바이너리 객체의 상위 컨테이너. 버킷은 Storage에서 데이터를 보관하는 기본 컨테이너.
bucket = storage.bucket()  # 기본 버킷 사용
fireStatus = False
fireCascade = cv.CascadeClassifier('/home/pi/fire.xml')
capture = cv.VideoCapture("http://192.168.0.33:8091/?action=stream")
picture_directory = "/home/pi/image_store/"
video_directory="/home/pi/video_store/"
capture_time = datetime.datetime.now()

distance_sensor = DistanceSensor(echo=18, trigger=17)
robot = Robot(left=(19,16), right=(20,21))

#initial distance val=100
dis=100

def go_foreward():
    global dis
    print("now distance is: ",dis,'cm')
    if dis<15:
        return
    robot.forward(0.6)
    sleep(1.4)
    robot.stop()
def go_backward():
    robot.backward(0.6)
    sleep(1.4)
    robot.stop()
def turn_right():
    robot.right(0.8)
    sleep(1.8)
    robot.stop()
def turn_left():
    robot.left(0.8)
    sleep(1.8)
    robot.stop()
def cal_distance(a,b):
    global dis
    while True:
        val=distance_sensor.distance*100
        sleep(0.0001)
        #distsensor val maximum 100
        if val<101:
            dis=val
        #print("first o: ",dis,"cm") 
def distance_act(a,b):
    
    global dis
    #print("zzz",dis)
    global thread_auto_on
    while True:
        distance=dis
        # dis=7 == stop => remote control
        if distance <7 or thread_auto_on==False:
        # dis< 15 ==random act without foreward
            break
        if distance >15 and distance <100:
            val=random.randint(0,2)
            if val ==0:
                turn_left()
            if val==1:
                turn_right()
            if val==2:
                go_backward()
        else:
            go_foreward()
            
    robot.stop()
    #thread end
    auto_start=False
    print("end thread!!!!")
def observe():
    print('불꽃 감시 중..')
    global fireStatus, capture_time
    # 키 입력이 없을 시 33ms마다 프레임 받아와서 출력
    while cv.waitKey(33) < 0:
        ret, frame = capture.read()
        fire = fireCascade.detectMultiScale(frame, 1.2, 5)
        cv.imshow("LiveCam", frame)
        for (x, y, w, h) in fire:
            now = datetime.datetime.now()
            
            print('불꽃 감지!! (' + now.strftime('%Y-%m-%d %H:%M:%S') + ')')
            sendMessage(str(now.strftime('%Y-%m-%d %H:%M:%S')))
            
            # 탐지한 불꽃에 사각형으로 표시
            cv.rectangle(frame, (x, y), (x + w, y + h), (0, 0, 255), 2)
            fireStatus = False
            capture_time = savePhoto(now, frame)
            filename = str(capture_time.strftime('%Y-%m-%d %H:%M:%S')) + '.jpg'
            uploadPhoto(filename)
            break
        if fireStatus == True:
            break


def savePhoto(now, frame):
    cv.imwrite(picture_directory + str(now.strftime('%Y-%m-%d %H:%M:%S')) + ".jpg", frame)
    print('사진 저장 완료 (' + str(now.strftime('%Y-%m-%d %H:%M:%S')) + ".jpg)")
    return now


def saveVideo():
    print('녹화 시작')
    global fireStatus
    start_time = datetime.datetime.now()
    while (capture.isOpened()):
        now = datetime.datetime.now()
        ret, frame = capture.read()
        out.write(frame)
        if start_time + datetime.timedelta(seconds=5) <= now:
            fireStatus = False
            print('녹화 종료')
            print('영상 저장 완료 (' + capture_time.strftime('%Y-%m-%d %H:%M:%S') + ".avi)")
            break


def uploadPhoto(file):
    blob = bucket.blob('detect_history/pictures/' + file)
    # new token, metadata 설정
    new_token = uuid4()
    metadata = {"firebaseStorageDownloadTokens": new_token}  # access token 필요
    blob.metadata = metadata

    blob.upload_from_filename(filename='/home/pi/image_store/' + file)
    print("사진 업로드 완료")

def uploadVideo(file):
    blob = bucket.blob('detect_history/videos/' + file)
    # new token, metadata 설정
    new_token = uuid4()
    metadata = {"firebaseStorageDownloadTokens": new_token}  # access token 필요
    blob.metadata = metadata

    blob.upload_from_filename(filename='/home/pi/video_store/' + file)
    print("영상 업로드 완료")
    
def fire_detect(a,b):
    while True:
        if fireStatus == False:
            observe()

        else:
            out = cv.VideoWriter(video_directory + capture_time.strftime('%Y-%m-%d %H:%M:%S') + '.avi', fourcc, 20.0,
                             (640, 480))
            saveVideo()
            uploadVideo(capture_time.strftime('%Y-%m-%d %H:%M:%S') + '.avi')
    
    capture.release()
    out.release()
    cv.destroyAllWindows()
def trans_data():
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

thread_auto_on=False
thread_distance_act=threading.Thread(target=distance_act,args=(0,0))

thread_cal_distance=threading.Thread(target=cal_distance,args=(0,0))
thread_cal_distance.start()

thread_fire_detect=threading.Thread(target=fire_detect,args=(0,0))
thread_fire_detect.start()

thread_serial_data=threading.Thread(target=trans_data,args=(0,0))
thread_serial_data.start()

app = Flask(__name__)
@app.route('/')
def index():
    return 'Hello world'

@app.route('/r_left')
def robot_left():
    turn_left()
    return 'robot left'
@app.route('/r_right')
def robot_right():
    turn_right()
    return 'robot right'
@app.route('/r_forward')
def robot_forward():
    go_foreward()
    return 'robot forward'
@app.route('/r_backward')
def robot_backward():
    go_backward()
    return 'robot backward'
@app.route('/auto_on')
def robot_auto_on():
    global thread_auto_on
    global thread_distance_act
    if thread_auto_on==True:
        return 'thread start'
    thread_distance_act=threading.Thread(target=distance_act,args=(0,0))
    print ('start auto thread')
    
    thread_auto_on=True
    thread_distance_act.start()
    return 'thread start'

@app.route('/auto_off')
def robot_auto_off():
    global thread_auto_on
    global thread_distance_act
    if thread_auto_on==False:
        return 'thread stop'
    thread_auto_on=False
    thread_distance_act.join()
    return "thread stop"
    print ('stop auto thread')
#@app.route('/r_stop')
#def robot_stop():
 #       robot.stop()
        #return 'robot stop'
if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')
