package ch.bytecrowd.PeerNetwork.factory;

import lombok.extern.log4j.Log4j2;
import org.mockito.Mockito;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.net.ServerSocket;
import java.net.Socket;

@Primary
@Component
@Log4j2
public class SocketTestFactory {

    public ServerSocket createServerSocket(Integer port) {
        log.debug("Mocking ServerSocket for port: {}", port);
        return Mockito.mock(ServerSocket.class);
    }

    public Socket createClientSocket(String ip, Integer port) {
        log.debug("Mocking ClientSocket for ip: {} port: {}", ip, port);
        return Mockito.mock(Socket.class);
    }
}
