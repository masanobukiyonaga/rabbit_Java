#!/bin/bash
echo "Stopping services..."
sudo systemctl stop nginx || true
sudo systemctl disable nginx || true
sudo systemctl stop httpd || true
sudo systemctl disable httpd || true
sudo systemctl stop gunicorn || true
sudo systemctl disable gunicorn || true

echo "Force killing any process on port 80..."
# Try fuser first
sudo fuser -k -9 80/tcp || true
# Try lsof/netstat to find PID and kill it
pid=$(sudo lsof -t -i:80)
if [ -n "$pid" ]; then
    echo "Found lingering process $pid on port 80. Killing..."
    sudo kill -9 $pid || true
fi

echo "Removing old container..."
sudo docker rm -f rabbit-container || true
sudo docker rm -f rabbit-container-ver2 || true

echo "Checking if port 80 is free..."
if sudo lsof -i:80; then
    echo "CRITICAL: Port 80 is STILL in use. Aborting docker run to prevent failure."
    exit 1
else
    echo "Port 80 is free. Proceeding."
fi

echo "Starting new container..."
sudo docker run -d -p 80:8080 --name rabbit-container --env-file .env rabbit-app

echo "Checking status..."
sleep 5
sudo docker ps
sudo docker logs --tail 20 rabbit-container
