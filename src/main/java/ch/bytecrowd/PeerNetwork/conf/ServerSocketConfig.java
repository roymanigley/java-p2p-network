package ch.bytecrowd.PeerNetwork.conf;

import ch.bytecrowd.PeerNetwork.factory.SocketFactory;
import jakarta.annotation.PreDestroy;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.ServerSocket;

@Configuration
@Log4j2
public class ServerSocketConfig {

    private final Integer port;
    private final SocketFactory socketFactory;
    private ServerSocket serverSocket;

    public ServerSocketConfig(@Value("${application.listening.port}") Integer port, SocketFactory socketFactory) {
        this.port = port;
        this.socketFactory = socketFactory;
    }


    @Bean
    @SneakyThrows
    ServerSocket createServerSocket() {
        serverSocket = socketFactory.createServerSocket(port);
        return serverSocket;
    }

    @PreDestroy
    @SneakyThrows
    void destroyServerSocket() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            log.debug("closing ServerSocket");
            serverSocket.close();
        }
    }
}
