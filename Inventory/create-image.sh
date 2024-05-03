#!/bin/bash

# This script takes an image name as a parameter.
# It checks if the image exists, and if so, it deletes all containers using that image and rebuilds the image.
# If the image does not exist, it builds the image.

# Ensure the image name is provided
if [ -z "$1" ]; then
    echo "Usage: $0 <image_name>"
    exit 1
fi

image_name=$1

# Check if the image exists
if docker images --format '{{.Repository}}:{{.Tag}}' | grep -q "^$image_name$"; then
    echo "Image $image_name exists. Deleting containers and rebuilding the image."

    # Find all containers using the image and stop them
    containers=$(docker ps -a --filter "ancestor=$image_name" --format "{{.ID}}")
    if [ ! -z "$containers" ]; then
        echo "Stopping and removing containers using image $image_name..."
        docker rm $(docker stop $containers)
    else
        echo "No running containers using image $image_name."
    fi

    # Build the image
    docker build -t $image_name .
else
    echo "Image $image_name does not exist. Building the image."
    docker build -t $image_name .
fi
