#!/bin/bash

# .env 파일 로드
if [ -f /home/ubuntu/moabam/.env ]; then
    source /home/ubuntu/moabam/.env
fi

export SERVER_DOMAIN=${SERVER_DOMAIN}
export SERVER_PORT=${SERVER_PORT}
export RESOLVER_IP=${RESOLVER_IP}
export BLUE_CONTAINER=${BLUE_CONTAINER}

envsubst '$SERVER_DOMAIN $SERVER_PORT $RESOLVER_IP $BLUE_CONTAINER' < /home/ubuntu/moabam/nginx/nginx.template > /home/ubuntu/moabam/nginx/nginx.conf
