#!/bin/bash

# Check if minimum arguments were passed
if [ "$#" -lt 3 ]; then
    echo "Usage: $0 <image-name> <container-name> <port-mapping> [network-name] [ENV_VAR=value ...]"
    exit 1
fi

image_name=$1
container_name=$2
port_mapping=$3
shift 3  # Shift the first three arguments to process the network name and environment variables

# Check for optional network name
network_name=""
if [[ "$1" != *=* ]]; then
    network_name=$1
    shift
fi

# Prepare environment variables for docker run command
env_vars=()
while [ "$#" -gt 0 ]; do
    env_vars+=("-e" "$1")
    shift
done

# Function to run or recreate the container with environment variables, port mappings, and network settings
run_container() {
    echo "Running container $container_name with image $image_name..."
    network_cmd=""
    if [ ! -z "$network_name" ]; then
        network_cmd="--network $network_name"
    fi
    docker run -d --name "$container_name" -p "$port_mapping" $network_cmd "${env_vars[@]}" "$image_name"
}

# Check if the image exists
if docker images --format '{{.Repository}}:{{.Tag}}' | grep -q "^$image_name$"; then
    echo "Image $image_name found."

    # Check if the specific container is already running or has exited
    existing_container=$(docker ps -a --filter "name=$container_name" --format '{{.Names}}' | grep -x "$container_name")
    if [ "$existing_container" ]; then
        echo "Container $existing_container found (running or exited). Deleting it..."
        docker rm -f "$existing_container"
    fi

    # Create or recreate the container
    run_container
else
    echo "Image $image_name not found. Building it..."

    # Assuming a Dockerfile in the current directory or modify accordingly
    docker build -t "$image_name" .

    # Create container with new image
    run_container
fi
