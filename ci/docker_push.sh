#!/usr/bin/env bash
docker login -u "$DOCKER_USERNAME" -p "$DOCKER_PASSWORD"
docker push tdekarz/spring-pet-project:latest
