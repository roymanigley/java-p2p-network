package ch.bytecrowd.PeerNetwork.service;

import ch.bytecrowd.PeerNetwork.common.JsonParser;
import ch.bytecrowd.PeerNetwork.model.MessageType;
import ch.bytecrowd.PeerNetwork.model.Peer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListenerServiceTest {

    @Mock
    ServerSocket serverSocket;
    @Mock
    ClientService clientService;
    RoutingTableManager routingTableManager = new RoutingTableManager();
    JsonParser jsonParser = new JsonParser(new ObjectMapper());

    @Test
    void testJoinRequestWithEmptyRoutingTable() throws IOException {
        // GIVEN
        var socketOutputStream = new ByteArrayOutputStream();
        var message = String.format("{\"type\":\"%s\", \"content\": \"%d\"}", MessageType.JOIN.name(), 4040);
        var socketInputStream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));

        initSocket(socketOutputStream, socketInputStream);

        // WHEN
        new ListenerService(
                serverSocket,
                jsonParser,
                clientService,
                routingTableManager
        ).handleSocketConnection();

        // THEN
        assertThat(socketOutputStream.toString(StandardCharsets.UTF_8))
                .isEqualTo("{\"type\":\"JOIN\",\"content\":\"0\"}\n");
        assertThat(routingTableManager.getRoutingTable()).hasSize(1);
    }

    @Test
    void testJoinRequestWithNotEmptyRoutingTable() throws IOException {
        // GIVEN
        var socket = mock(Socket.class);
        var inetAddress = mock(InetAddress.class);
        var socketOutputStream = new ByteArrayOutputStream();
        var message = String.format("{\"type\":\"%s\", \"content\": \"%d\"}", MessageType.JOIN.name(), 4040);
        var socketInputStream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
        routingTableManager
                .addPeer(
                        Peer.builder().id(1L).ip("10.0.0.1").port(4040).build()
                ).addPeer(
                        Peer.builder().id(2L).ip("10.0.0.2").port(4040).build()
                ).addPeer(
                        Peer.builder().id(3L).ip("10.0.0.3").port(4040).build()
                );

        initSocket(socketOutputStream, socketInputStream);

        // WHEN
        new ListenerService(
                serverSocket,
                jsonParser,
                clientService,
                routingTableManager
        ).handleSocketConnection();

        // THEN
        assertThat(socketOutputStream.toString(StandardCharsets.UTF_8))
                .isEqualTo("{\"type\":\"JOIN\",\"content\":\"4\"}\n");
        assertThat(routingTableManager.getRoutingTable()).hasSize(4);
    }

    @Test
    void testRoutingTableRequestWithEmptyRoutingTable() throws IOException {
        // GIVEN
        var socket = mock(Socket.class);
        var inetAddress = mock(InetAddress.class);
        var socketOutputStream = new ByteArrayOutputStream();
        var message = String.format("{\"type\":\"%s\", \"content\": \"%d\"}", MessageType.ROUTING_TABLE.name(), 4040);
        var socketInputStream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));

        initSocket(socketOutputStream, socketInputStream);

        // WHEN
        new ListenerService(
                serverSocket,
                jsonParser,
                clientService,
                routingTableManager
        ).handleSocketConnection();

        // THEN
        assertThat(socketOutputStream.toString(StandardCharsets.UTF_8))
                .isEqualTo("{\"type\":\"ROUTING_TABLE\",\"content\":\"[]\"}\n");
    }


    @Test
    void testRoutingTableRequestWithNotEmptyRoutingTable() throws IOException {
        // GIVEN
        var socket = mock(Socket.class);
        var inetAddress = mock(InetAddress.class);
        var socketOutputStream = new ByteArrayOutputStream();
        var message = String.format("{\"type\":\"%s\", \"content\": \"%d\"}", MessageType.ROUTING_TABLE.name(), 4040);
        var socketInputStream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
        routingTableManager
                .addPeer(
                        Peer.builder().id(1L).ip("10.0.0.1").port(4040).build()
                ).addPeer(
                        Peer.builder().id(2L).ip("10.0.0.2").port(4040).build()
                ).addPeer(
                        Peer.builder().id(3L).ip("10.0.0.3").port(4040).build()
                );

        initSocket(socketOutputStream, socketInputStream);

        // WHEN
        new ListenerService(
                serverSocket,
                jsonParser,
                clientService,
                routingTableManager
        ).handleSocketConnection();

        // THEN
        assertThat(socketOutputStream.toString(StandardCharsets.UTF_8))
                .isEqualTo("{" +
                        "\"type\":\"ROUTING_TABLE\"," +
                        "\"content\":\"[" +
                            "{\\\"id\\\":1,\\\"ip\\\":\\\"10.0.0.1\\\",\\\"port\\\":4040}," +
                            "{\\\"id\\\":2,\\\"ip\\\":\\\"10.0.0.2\\\",\\\"port\\\":4040}," +
                            "{\\\"id\\\":3,\\\"ip\\\":\\\"10.0.0.3\\\",\\\"port\\\":4040}" +
                        "]\"}\n");
    }

    @Test
    void testLeaveRequestWithNotEmptyRoutingTable() throws IOException {
        // GIVEN
        var idForLeave = 3;
        var socket = mock(Socket.class);
        var inetAddress = mock(InetAddress.class);
        var socketOutputStream = new ByteArrayOutputStream();
        var message = String.format("{\"type\":\"%s\", \"content\": \"%d\"}", MessageType.LEAVE.name(), idForLeave);
        var socketInputStream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
        routingTableManager
                .addPeer(
                        Peer.builder().id(1L).ip("10.0.0.1").port(4040).build()
                ).addPeer(
                        Peer.builder().id(2L).ip("10.0.0.2").port(4040).build()
                ).addPeer(
                        Peer.builder().id(3L).ip("10.0.0.3").port(4040).build()
                );

        initSocket(socketOutputStream, socketInputStream);

        // WHEN
        new ListenerService(
                serverSocket,
                jsonParser,
                clientService,
                routingTableManager
        ).handleSocketConnection();

        // THEN
        assertThat(socketOutputStream.toString(StandardCharsets.UTF_8))
                .isEqualTo("{\"type\":\"LEAVE\",\"content\":\"bye\"}\n");

        assertThat(routingTableManager.getRoutingTable()).hasSize(2);
        assertThat(
                routingTableManager.getRoutingTable().stream()
                        .filter(peer ->
                                peer.getId().equals(idForLeave)
                        ).findAny()
        ).isEmpty();
    }

    private Socket initSocket(ByteArrayOutputStream socketOutputStream, ByteArrayInputStream socketInputStream) throws IOException {
        var socket = mock(Socket.class);
        var inetAddress = mock(InetAddress.class);

        when(socket.getOutputStream()).thenReturn(socketOutputStream);
        when(socket.getInputStream()).thenReturn(socketInputStream);
        when(inetAddress.getHostAddress()).thenReturn("10.0.0.12");
        when(socket.getInetAddress()).thenReturn(inetAddress);
        when(socket.getPort()).thenReturn(4040);
        when(serverSocket.accept()).thenReturn(socket);

        return socket;
    }
}
