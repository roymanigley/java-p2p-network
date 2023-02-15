package ch.bytecrowd.PeerNetwork.service;

import ch.bytecrowd.PeerNetwork.common.JsonParser;
import ch.bytecrowd.PeerNetwork.factory.SocketFactory;
import ch.bytecrowd.PeerNetwork.model.Peer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    RoutingTableManager routingTableManager;
    @Mock
    SocketFactory socketFactory;

    JsonParser jsonParser = new JsonParser(new ObjectMapper());

    @Test
    void testJoinAndAssignmentOfPeerId() throws IOException {

        var socket = mock(Socket.class);
        var socketOutputStream = new ByteArrayOutputStream();
        var assignedPeerId = 4L;
        var message = String.format("{\"type\":\"JOIN\", \"content\":\"%d\"}", assignedPeerId);
        var socketInputStream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));

        when(socket.getOutputStream()).thenReturn(socketOutputStream);
        when(socket.getInputStream()).thenReturn(socketInputStream);

        when(socketFactory.createClientSocket(anyString(), anyInt())).thenReturn(socket);

        ClientService clientService = new ClientService(
                routingTableManager,
                jsonParser,
                socketFactory
        );
        clientService.listeningPort = 99;
        clientService.join(
                Peer.builder()
                        .ip("10.0.0.1")
                        .port(4040)
                        .build()
        );

        assertThat(socketOutputStream.toString(StandardCharsets.UTF_8)).isEqualTo("{\"type\":\"JOIN\",\"content\":\"99\"}\n");
        verify(routingTableManager).setLocalPeerId(assignedPeerId);
    }

    @Test
    void testRoutingTableRequest() throws IOException {
        // GIVEN
        var socket = mock(Socket.class);
        var socketOutputStream = new ByteArrayOutputStream();
        var message = String.format("{\"type\":\"ROUTING_TABLE\"," +
                "\"content\":\"[" +
                "{\\\"id\\\":1,\\\"ip\\\":\\\"10.0.0.1\\\",\\\"port\\\":4040}," +
                "{\\\"id\\\":2,\\\"ip\\\":\\\"10.0.0.2\\\",\\\"port\\\":4040}," +
                "{\\\"id\\\":3,\\\"ip\\\":\\\"10.0.0.3\\\",\\\"port\\\":4040}" +
                "]\"}\n");
        var socketInputStream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));

        when(socket.getOutputStream()).thenReturn(socketOutputStream);
        when(socket.getInputStream()).thenReturn(socketInputStream);
        when(socketFactory.createClientSocket(anyString(), anyInt())).thenReturn(socket);

        ClientService clientService = new ClientService(
                routingTableManager,
                jsonParser,
                socketFactory
        );
        clientService.listeningPort = 99;

        // WHEN
        clientService.getRoutingTable(
                Peer.builder()
                        .ip("10.0.0.1")
                        .port(4040)
                        .build()
        );

        ArgumentCaptor<Peer> routingTableCaptor = ArgumentCaptor.forClass(Peer.class);
        verify(routingTableManager, times(3)).addPeer(routingTableCaptor.capture());
        List<Peer> peers = routingTableCaptor.getAllValues();
        assertThat(peers).hasSize(3);

        assertThat(peers).contains(
                Peer.builder().id(1L).ip("10.0.0.1").port(4040).build(),
                Peer.builder().id(2L).ip("10.0.0.2").port(4040).build(),
                Peer.builder().id(3L).ip("10.0.0.3").port(4040).build());

    }
}

