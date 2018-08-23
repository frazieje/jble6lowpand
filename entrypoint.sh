#!/bin/sh

/usr/sbin/radvd

hciconfig hci0 reset

bin/jble6lowpand -configFile /app/jble6lowpand/jble6lowpand.conf

echo "running"
read
echo "stopping"

kill $(cat /var/run/radvd.pid)

pkill jble6lowpand
