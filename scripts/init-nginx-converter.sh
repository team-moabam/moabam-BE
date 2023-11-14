#!/bin/bash

# .env 파일 로드
if [ -f /home/ubuntu/moabam/.env ]; then
    source /home/ubuntu/moabam/.env
fi

export SERVER_DOMAIN=${SERVER_DOMAIN}
export SERVER_PORT=${SERVER_PORT}
export BLUE_CONTAINER=${BLUE_CONTAINER}

envsubst '$SERVER_DOMAIN' < /home/ubuntu/moabam/nginx/templates/http-server.template > /home/ubuntu/moabam/nginx/conf.d/http-server.conf
envsubst '$SERVER_DOMAIN' < /home/ubuntu/moabam/nginx/templates/ssl-server.template > /home/ubuntu/moabam/nginx/conf.d/ssl-server.conf
envsubst '$BLUE_CONTAINER $SERVER_PORT' < /home/ubuntu/moabam/nginx/templates/upstream.template > /home/ubuntu/moabam/nginx/conf.d/upstream.conf
