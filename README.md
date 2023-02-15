# Peer 2 Peer Network
> a simple p2p network to execute Groovy code on all peers

    ./mvnw clean package
    cp target/PeerNetwork-0.0.1-SNAPSHOT.jar src/main/docker/build/app.jar
    docker-compose -f src/main/docker/docker-compose.yml up --build

then wait until routing tables are loaded and execute

    java -Dspring.profiles.active=execute -Dapplication.listening.port=4242 -jar target/PeerNetwork-0.0.1-SNAPSHOT.jar

then exec a Groovy Script like

    return "id".execute().text