FROM openjdk:11
COPY ./build/libs/storage-0.0.1-SNAPSHOT.jar /usr/app/
WORKDIR /usr/app
EXPOSE 10001
CMD ["java", "-jar", "storage-0.0.1-SNAPSHOT.jar"]