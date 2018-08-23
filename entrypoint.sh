#!/bin/bash
function gracefulShutdown {
  echo "Shutting down! xx"
  # do something..
}
trap gracefulShutdown SIGTERM
trap gracefulshutdown SIGINT

/usr/sbin/radvd

hciconfig hci0 reset

exec "$@" &
wait