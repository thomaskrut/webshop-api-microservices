FROM amazoncorretto:17-alpine-jdk
VOLUME /tmp
ARG JAR_FILE=build/libs/web.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]