#!/bin/sh

/usr/sbin/radvd

hciconfig hci0 reset

bin/jble6lowpand -configFile /app/jble6lowpand/jble6lowpand.conf

read

kill $(cat /var/run/radvd.pid)

pkill jble6lowpand
