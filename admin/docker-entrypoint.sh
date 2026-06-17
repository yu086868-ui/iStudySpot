#!/bin/sh
set -eu

: "${ADMIN_BACKEND_TARGET:=http://backend:8080}"

export ADMIN_BACKEND_TARGET
envsubst '${ADMIN_BACKEND_TARGET}' < /etc/nginx/conf.d/default.conf > /tmp/default.conf
mv /tmp/default.conf /etc/nginx/conf.d/default.conf
