#!/bin/bash

set -e

hostport="$1"
shift

host=${hostport%:*}
port=${hostport#*:}

until nc -zv $host $port; do
  >&2 echo "Postgres is unavailable - sleeping"
  sleep 1
done

>&2 echo "Postgres is up - executing command"
exec "$@"
