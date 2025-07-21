#!/bin/bash
#
#
# Start script for disqualified-officers-data-api

PORT=8080

exec java -jar -Dserver.port="${PORT}" -XX:MaxRAMPercentage=80 "disqualified-officers-data-api.jar"
