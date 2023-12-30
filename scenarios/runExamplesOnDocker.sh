#!/usr/bin/env bash

cd $(dirname $0)/../

#cmd="docker-compose -f scenarios/docker-compose.yml up -d"
#echo "========================================================================================================================================"
#echo "==================  🔥 APM Server started: ${cmd}  ============================"
#echo "========================================================================================================================================"
#eval ${cmd}

if [[ $? -eq 0 ]]; then
  
  sbt "zio-grpc-scenario/stage"

  nohup scenarios/zio-grpc-scenario/target/universal/stage/bin/hello-world-client \
  -Dskywalking.collector.backend_service=localhost:11800 \
  -Dskywalking.agent.service_name=hello-client \
  -J-javaagent:scenarios/skywalking-agent/skywalking-agent.jar > /dev/null 2>&1
    
  nohup scenarios/zio-grpc-scenario/target/universal/stage/bin/zio-grpc-scenario \
  -Dskywalking.collector.backend_service=localhost:11800 \
  -Dskywalking.agent.service_name=hello-server \
  -J-javaagent:scenarios/skywalking-agent/skywalking-agent.jar > /dev/null 2>&1
  
  sleep 5
    
  curl http://localhost:8090/text
  
fi  