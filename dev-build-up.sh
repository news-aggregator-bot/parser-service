#!/bin/bash
docker-compose --project-name na -f docker-compose.yml -f docker-compose.dev.yml up --build --no-start parser-service
docker stack deploy --compose-file docker-compose.yml --compose-file docker-compose.dev.yml --with-registry-auth parser-service