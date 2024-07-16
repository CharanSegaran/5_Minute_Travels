#Build the angular part
FROM node:22 AS ngbuild

WORKDIR /Frontend

#Install Angular
RUN npm i -g @angular/cli@17.3.8

COPY Frontend/5_Minute_Holiday_Plan/angular.json .
COPY Frontend/5_Minute_Holiday_Plan/package*.json .
COPY Frontend/5_Minute_Holiday_Plan/tsconfig*.json .
COPY Frontend/5_Minute_Holiday_Plan/manifest.json .
COPY Frontend/5_Minute_Holiday_Plan/src src

#Install the modules
RUN npm ci
RUN ng build

#Build the Spring Boot
FROM openjdk:21 AS javabuild

WORKDIR /Backend

COPY Backend/5_minute_travels/mvnw . 
COPY Backend/5_minute_travels/pom.xml .
COPY Backend/5_minute_travels/.mvn .mvn
COPY Backend/5_minute_travels/src src

#COpy Angular files to Spring Boot
COPY --from=ngbuild /Frontend/dist/5-minute-holiday-plan/browser/ src/main/resources/static

#Produce target target/5_minute_travels-0.0.1-SNAPSHOT.jar
RUN ./mvnw package -Dmaven.test.skip=true

#Run container
FROM eclipse-temurin:21

WORKDIR /app

COPY --from=javabuild /Backend/target/5_minute_travels-0.0.1-SNAPSHOT.jar 5_minute_travels.jar
COPY --from=javabuild /Backend/src/main/resources/data.txt data.txt

#Install Python
RUN apt-get update && apt-get install -y python3 python3-pip

#Copy the requirements.txt
COPY requirements.txt /app/requirements.txt

#Install the Python dependencies
RUN pip install --default-timeout=900 --no-cache-dir -r /app/requirements.txt

ENV PORT=8080
EXPOSE ${PORT}

ENTRYPOINT SERVER_PORT=${PORT} java -jar 5_minute_travels.jar

