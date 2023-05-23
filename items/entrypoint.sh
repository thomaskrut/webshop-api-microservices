#!/bin/sh

set -e

echo "Waiting for database..."
while ! nc -z "${MYSQL_HOST}" 3306; do
    sleep 1
done

echo "Database is up. Starting the application..."
java -jar app.jar

exec "$@"