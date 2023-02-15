package ch.bytecrowd.PeerNetwork.conf;

import ch.bytecrowd.PeerNetwork.factory.SocketFactory;
import ch.bytecrowd.PeerNetwork.factory.SocketTestFactory;
import jakarta.annotation.PreDestroy;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.net.ServerSocket;

@Configuration
@Log4j2
public class ServerSocketTestConfig {

    private final Integer port;
    private final SocketTestFactory socketFactory;
    private ServerSocket serverSocket;

    public ServerSocketTestConfig(@Value("${application.listening.port}") Integer port, SocketTestFactory socketFactory) {
        this.port = port;
        this.socketFactory = socketFactory;
    }


    @Bean
    @Primary
    @SneakyThrows
    ServerSocket createTestServerSocket() {
        serverSocket = socketFactory.createServerSocket(port);
        return serverSocket;
    }
}
