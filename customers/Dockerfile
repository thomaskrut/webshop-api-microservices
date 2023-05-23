FROM amazoncorretto:17-alpine-jdk
VOLUME /tmp
ARG JAR_FILE=build/libs/customers.jar
COPY ${JAR_FILE} app.jar
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["./entrypoint.sh"]