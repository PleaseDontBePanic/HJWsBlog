#!/bin/bash
 
# 修改APP_NAME为云效上的应用名
APP_NAME=HJWsBlog
VERSION_NAME=0.0.1
 
PROG_NAME=$0
ACTION=$1
APP_START_TIMEOUT=50    # 等待应用启动的时间
APP_PORT=80         # 应用端口
HEALTH_CHECK_URL=http://127.0.0.1:${APP_PORT}  # 应用健康检查URL
HEALTH_CHECK_FILE_DIR=/opt/${APP_NAME}/status   # 脚本会在这个目录下生成nginx-status文件
APP_HOME=/opt/${APP_NAME} # 从package.tgz中解压出来的jar包放到这个目录下
JAR_NAME=${APP_HOME}/${APP_NAME}-${VERSION_NAME}.jar # jar包的名字
JAVA_OUT=${APP_HOME}/logs/start.log  #应用的启动日志
 
# 创建出相关目录
mkdir -p ${HEALTH_CHECK_FILE_DIR}
mkdir -p ${APP_HOME}
mkdir -p ${APP_HOME}/logs
usage() {
    echo "Usage: $PROG_NAME {start|stop|restart}"
    exit 2
}
 
health_check() {
    echo "check ${HEALTH_CHECK_URL} success"
}
start_application() {
    echo "starting java process"
    nohup java -jar -Dspring.profiles.active=test1230 ${APP_NAME}-${VERSION_NAME}.jar >/dev/null 2>&1 &
    echo "started java process"
}
 
stop_application() {
   checkjavapid=`ps -ef | grep java | grep ${APP_NAME}-${VERSION_NAME}.jar | grep -v grep |grep -v 'deploy1230.sh'| awk '{print$2}'`
   
   if [[ ! $checkjavapid ]];then
      echo -e "\rno java process"
      return
   fi
 
   echo "stop java process"
   times=60
   for e in $(seq 60)
   do
        sleep 1
        COSTTIME=$(($times - $e ))
        checkjavapid=`ps -ef | grep java | grep ${APP_NAME}-${VERSION_NAME}.jar | grep -v grep |grep -v 'deploy1230.sh'| awk '{print$2}'`
        if [[ $checkjavapid ]];then
            kill -9 $checkjavapid
            echo -e  "\r        -- stopping java lasts `expr $COSTTIME` seconds."
        else
            echo -e "\rjava process has exited"
            break;
        fi
   done
   echo ""
}
start() {
    start_application
    health_check
}
stop() {
    stop_application
}
case "$ACTION" in
    start)
        start
    ;;
    stop)
        stop
    ;;
    restart)
        stop
        start
    ;;
    *)
        usage
    ;;
esac
————————————————
版权声明：本文为CSDN博主「renkai721」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/renkai721/article/details/126462836
