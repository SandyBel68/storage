FROM openjdk:11
COPY ./build/libs/search-0.0.1-SNAPSHOT.jar /usr/app/
WORKDIR /usr/app
EXPOSE 10002
CMD ["java", "-jar", "search-0.0.1-SNAPSHOT.jar"]