#!/usr/bin/env bash

set +e

cd $(dirname $0)/../

cmd="docker-compose -f scenarios/docker-compose.yml up -d"
echo "========================================================================================================================================"
echo "==================  🔥 APM Server started: ${cmd}  ============================"
echo "========================================================================================================================================"
eval ${cmd}

if [[ $? -eq 0 ]]; then
  
  sbt "zio-scenario/stage"

  nohup scenarios/zio-scenario/target/universal/stage/bin/hello-world-client \
  -Dskywalking.collector.backend_service=localhost:11800 \
  -Dskywalking.agent.service_name=hello-client \
  -J-javaagent:scenarios/skywalking-agent/skywalking-agent.jar > /dev/null 2>&1 &
    
  nohup scenarios/zio-scenario/target/universal/stage/bin/zio-scenario \
  -Dskywalking.collector.backend_service=localhost:11800 \
  -Dskywalking.agent.service_name=hello-server \
  -J-javaagent:scenarios/skywalking-agent/skywalking-agent.jar > /dev/null 2>&1 &
  
fi  

# curl http://localhost:8090/hello
# ps -ef | grep hello | awk {'print $2'} | xargs kill -9