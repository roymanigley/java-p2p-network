package ch.bytecrowd.PeerNetwork.factory;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Component
@Log4j2
public class SocketFactory {

    public ServerSocket createServerSocket(Integer port) throws IOException {
        log.debug("Instantiating ServerSocket for port: {}", port);
        return new ServerSocket(port);
    }

    public Socket createClientSocket(String ip, Integer port) throws IOException {
        log.debug("Instantiating ClientSocket for ip: {} port: {}", ip, port);
        return new Socket(ip, port);
    }
}
