FROM eclipse-temurin:21
COPY target/apus-*.jar /usr/app/app.jar
RUN useradd -m apus
USER apus
RUN mkdir /home/apus/.apus
EXPOSE 8080
CMD ["java", "-jar", "/usr/app/app.jar"]
HEALTHCHECK CMD curl --fail --silent localhost:8080/actuator/health | grep UP || exit 1
