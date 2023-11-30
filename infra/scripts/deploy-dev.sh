#!/bin/bash

# .env 파일 로드
if [ -f /home/ubuntu/moabam/.env ]; then
    source /home/ubuntu/moabam/.env
fi

if [ $(docker ps | grep -c "nginx") -eq 0 ]; then
  echo "### nginx 시작 ###"
  docker-compose up -d nginx
else
  echo "-------------------------------------------"
  echo "nginx 이미 실행 중 입니다."
  echo "-------------------------------------------"
fi

echo
echo

if [ $(docker ps | grep -c "redis") -eq 0 ]; then
  echo "### redis 시작 ###"
  docker-compose up -d redis
else
  echo "-------------------------------------------"
  echo "redis 이미 실행 중 입니다."
  echo "-------------------------------------------"
fi

echo
echo

if [ $(docker ps | grep -c "mysql") -eq 0 ]; then
  echo "### mysql 시작 ###"
  docker-compose up -d mysql
else
  echo "-------------------------------------------"
  echo "mysql 이미 실행 중 입니다."
  echo "-------------------------------------------"
fi

echo
echo

echo
echo "### springboot blue-green 무중단 배포 시작 ###"
echo

IS_BLUE=$(docker ps | grep ${BLUE_CONTAINER})
NGINX_CONF="/home/ubuntu/moabam/nginx/nginx.conf"
UPSTREAM_CONF="/home/ubuntu/moabam/nginx/conf.d/upstream.conf"

if [ -n "$IS_BLUE" ]; then
    echo "### BLUE => GREEN ###"
    echo "1. ${GREEN_CONTAINER} 이미지 가져오고 실행"
    docker-compose pull moabam-green
    docker-compose up -d moabam-green

    attempt=1
    while [ $attempt -le 24 ]; do
        echo "2. ${GREEN_CONTAINER} health check (Attempt: $attempt)"
        sleep 5
        REQUEST=$(docker exec nginx curl http://${GREEN_CONTAINER}:${SERVER_PORT})

        if [ -n "$REQUEST" ]; then
            echo "${GREEN_CONTAINER} health check 성공"
            sed -i "s/${BLUE_CONTAINER}/${GREEN_CONTAINER}/g" $UPSTREAM_CONF
            echo "3. nginx 설정파일 reload"
            docker exec nginx service nginx reload
            echo "4. ${BLUE_CONTAINER} 컨테이너 종료"
            docker-compose stop moabam-blue

	        echo "5. ${GREEN_CONTAINER} 배포 성공"
            break;
        fi

        if [ $attempt -eq 24 ]; then
	          echo "${GREEN_CONTAINER} 배포 실패 !!"

            docker-compose stop moabam-green

            exit 1;
        fi

        attempt=$((attempt+1))
    done;
else
    echo "### GREEN => BLUE ###"
    echo "1. ${BLUE_CONTAINER} 이미지 가져오고 실행"
    docker-compose pull moabam-blue
    docker-compose up -d moabam-blue

    attempt=1
    while [ $attempt -le 24 ]; do
        echo "2. ${BLUE_CONTAINER} health check (Attempt: $attempt)"
        sleep 5
        REQUEST=$(docker exec nginx curl http://${BLUE_CONTAINER}:${SERVER_PORT})

        if [ -n "$REQUEST" ]; then
            echo "${BLUE_CONTAINER} health check 성공"
            sed -i "s/${GREEN_CONTAINER}/${BLUE_CONTAINER}/g" $UPSTREAM_CONF
            echo "3. nginx 설정파일 reload"
            docker exec nginx service nginx reload
            echo "4. ${GREEN_CONTAINER} 컨테이너 종료"
            docker-compose stop moabam-green

	        echo "5. ${BLUE_CONTAINER} 배포 성공"
            break;
        fi

        if [ $attempt -eq 24 ]; then
	        echo "${BLUE_CONTAINER} 배포 실패 !!"

            docker-compose stop moabam-blue
            exit 1;
        fi

        attempt=$((attempt+1))
    done;
fi
