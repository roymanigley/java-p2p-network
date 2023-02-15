FROM openjdk:17-alpine
COPY ./app.jar /opt/app.jar
CMD java -Dapplication.boot.peer.host=$BOOT_PEER_HOST -jar /opt/app.jar