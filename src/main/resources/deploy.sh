#!/bin/bash
# 修改APP_NAME为云效上的应用名
APP_NAME=HJWsBlog
PROG_NAME=$0
ACTION=$1
APP_START_TIMEOUT=20 # 等待应用启动的时间
APP_PORT=80 # 应用端口
HEALTH_CHECK_URL=http://49.233.44.71:${APP_PORT} # 应用健康检查URL
HEALTH_CHECK_FILE_DIR=/home/admin/app/status # 脚本会在这个目录下生成nginx-status文件
APP_HOME=/home/admin/app # 从package.tgz中解压出来的jar包放到这个目录下
JAR_NAME=${APP_HOME}/${APP_NAME}.jar # jar包的名字
JAVA_OUT=${APP_HOME}/logs/start.log #应用的启动日志
 
 
# 创建出相关目录
mkdir -p ${HEALTH_CHECK_FILE_DIR}
mkdir -p ${APP_HOME}
mkdir -p ${APP_HOME}/logs
usage() {
echo "Usage: $PROG_NAME {start|stop|restart}"
exit 2
}
 
health_check() {
exptime=0
echo "checking ${HEALTH_CHECK_URL}"
while true
do
status_code=`/usr/bin/curl -L -o /dev/null --connect-timeout 5 -s -w %{http_code} ${HEALTH_CHECK_URL}`
if [ "$?" != "0" ]; then
echo -n -e "\rapplication not started"
else
echo "code is $status_code"
if [ "$status_code" == "200" ];then
break
fi
fi
sleep 1
((exptime++))
 
echo -e "\rWait app to pass health check: $exptime..."
 
if [ $exptime -gt ${APP_START_TIMEOUT} ]; then
echo 'app start failed'
exit 1
fi
done
echo "check ${HEALTH_CHECK_URL} success"
}
start_application() {
echo "starting java process"
nohup java -jar ${JAR_NAME} > ${JAVA_OUT} 2>&1 &
echo "started java process"
}
 
stop_application() {
checkjavapid=`ps -ef | grep java | grep ${APP_NAME} | grep -v grep |grep -v 'deploy.sh'| awk '{print$2}'`
 
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
checkjavapid=`ps -ef | grep java | grep ${APP_NAME} | grep -v grep |grep -v 'deploy.sh'| awk '{print$2}'`
if [[ $checkjavapid ]];then
kill -9 $checkjavapid
echo -e "\r -- stopping java lasts `expr $COSTTIME` seconds."
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
