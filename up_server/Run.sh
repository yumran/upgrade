#!/bin/bash
Pid=`jps -l|grep 'up.jar' |awk '{print $1}'`;
echo "upgrade server pid is" $Pid;
if [ $Pid ];then
 echo "kill pid is:" $Pid;
 kill -9 $Pid;
else
 echo "Pid is null";
fi
echo "cp to up.jar";
cp upgrade-1.0.0-SNAPSHOT.jar up.jar
nohup java -jar up.jar -Dspring.config.location=.\config\application.yml > ./logs/out.log 2>&1 &
echo "end";
