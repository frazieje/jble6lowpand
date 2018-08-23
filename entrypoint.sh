#!/bin/bash
function gracefulShutdown {
  echo "Shutting down! xx"
  # do something..
}
trap gracefulShutdown SIGTERM
trap gracefulshutdown SIGINT

/usr/sbin/radvd

hciconfig hci0 reset

bin/jble6lowpand -configFile /app/jble6lowpand/jble6lowpand.conf

read

kill $(cat /var/run/radvd.pid)

pkill jble6lowpand