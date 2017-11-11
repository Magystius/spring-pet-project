FROM openjdk:9-jre

LABEL version="1.0"
LABEL service="spring-pet-project"

#TODO: this has be get better
USER java-runner

CMD mkdir /root/.embeddedmongo && \
    mkdir /root/.embeddedmongo/extracted && \
    apk update && \
    apk add openssl && \
    cd /root/.embeddedmongo && \
    wget -O mongo.tgz https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-3.2.2.tgz && \
    tar -xzf mongo.tgz -C /root/.embeddedmongo/extracted

CMD mkdir /var/opt/spring-pet-project

ADD ./target/spring-pet-project-0.0.0.jar /var/opt/spring-pet-project/spring-pet-project-0.0.0.jar

WORKDIR /var/opt/spring-pet-project

HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -q http://localhost:8080 || exit 1

EXPOSE 8080
ENTRYPOINT java -jar spring-pet-project-0.0.0.jar