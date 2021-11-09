wget -q $1 -O docker-stack-parser-service.yml
echo "Deploying new parser-service stack"
docker stack rm parser-service
docker stack deploy --compose-file docker-stack-parser-service.yml --with-registry-auth parser-service