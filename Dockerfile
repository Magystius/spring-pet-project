FROM openjdk:9-jre

LABEL version="1.0"
LABEL service="spring-pet-project"

#TODO: this has be get better
USER java-runner

CMD mkdir /var/opt/spring-pet-project

ADD ./target/spring-pet-project-0.0.0.jar /var/opt/spring-pet-project/spring-pet-project-0.0.0.jar

WORKDIR /var/opt/spring-pet-project

HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -q http://localhost:8080 || exit 1

EXPOSE 8080
ENTRYPOINT java -jar spring-pet-project-0.0.0.jar