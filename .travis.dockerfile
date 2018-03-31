FROM openjdk:10-jre-slim

LABEL version="1.0"
LABEL service="spring-pet-project"

#TODO: this has be get better
USER root

#TODO: download mongo on build-time
#RUN mkdir -p /root/.embedmongo && \
#    mkdir -p /root/.embedmongo/linux && \
#    mkdir -p /var/opt/spring-pet-project && \
#    cd /root/.embedmongo/linux && \
#    wget -O mongodb-linux-x86_64-3.2.2.tgz https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-3.2.2.tgz && \
#    tar -xzf mongodb-linux-x86_64-3.2.2.tgz bin/mongod -C /root/.embedmongo/extracted

RUN mkdir -p /var/opt/spring-pet-project && \
    apt-get update && \
    apt-get install -y wget

ADD ./target/spring-pet-project-0.0.0.jar /var/opt/spring-pet-project/spring-pet-project-0.0.0.jar

WORKDIR /var/opt/spring-pet-project

HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -q http://localhost:8080 || exit 1

EXPOSE 8080
ENTRYPOINT java -jar spring-pet-project-0.0.0.jar
