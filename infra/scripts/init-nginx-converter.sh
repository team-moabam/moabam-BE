#!/bin/bash

# .env 파일 로드
if [ -f /home/ubuntu/moabam/infra/.env ]; then
    source /home/ubuntu/moabam/infra/.env
fi

export SERVER_DOMAIN=${SERVER_DOMAIN}
export SERVER_PORT=${SERVER_PORT}
export BLUE_CONTAINER=${BLUE_CONTAINER}

#envsubst '$SERVER_DOMAIN' < /home/ubuntu/moabam/infra/nginx/templates/http-server.template > /home/ubuntu/moabam/infra/nginx/conf.d/http-server.conf
#envsubst '$SERVER_DOMAIN' < /home/ubuntu/moabam/infra/nginx/templates/ssl-server.template > /home/ubuntu/moabam/infra/nginx/conf.d/ssl-server.conf
envsubst '$SERVER_DOMAIN' < /home/ubuntu/moabam/infra/nginx/templates/http-server-notssl.template > /home/ubuntu/moabam/infra/nginx/conf.d/http-server-notssl.conf
envsubst '$BLUE_CONTAINER $SERVER_PORT' < /home/ubuntu/moabam/infra/nginx/templates/upstream.template > /home/ubuntu/moabam/infra/nginx/conf.d/upstream.conf
