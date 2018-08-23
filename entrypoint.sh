#!/bin/bash
function gracefulShutdown {
  echo "Shutting down!"
  # do something..
}
trap gracefulShutdown SIGTERM
trap gracefulshutdown SIGINT
exec "$@" &
wait