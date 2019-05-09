FROM maven:3.3-jdk-8 as mvn
COPY . /tmp
WORKDIR /tmp
RUN mvn install

FROM openjdk:8-alpine
ARG VER=0.1
ENV FOLDER=/tmp/target
ENV APP=nb-tb-connector-${VER}.jar
RUN apk update && apk add curl && rm -rf /var/cache/apk/*
COPY --from=mvn ${FOLDER}/${APP} /tmp/target/
WORKDIR /tmp/target/
CMD [ "sh", "-c", "java -jar ${APP}" ]
