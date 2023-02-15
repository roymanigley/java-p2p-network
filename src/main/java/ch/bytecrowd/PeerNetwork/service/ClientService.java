package ch.bytecrowd.PeerNetwork.service;

import ch.bytecrowd.PeerNetwork.common.JsonParser;
import ch.bytecrowd.PeerNetwork.factory.SocketFactory;
import ch.bytecrowd.PeerNetwork.model.Message;
import ch.bytecrowd.PeerNetwork.model.MessageType;
import ch.bytecrowd.PeerNetwork.model.Peer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.BiConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.Socket;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Log4j2
public class ClientService implements Runnable {

    @Value("${application.listening}")
    private boolean listening;
    @Value("${application.boot.peer.host}")
    String bootPeerHost;
    @Value("${application.boot.peer.port}")
    Integer bootPeerPort;
    @Value("${application.listening.port}")
    Integer listeningPort;
    private final RoutingTableManager routingTableManager;
    private final JsonParser jsonParser;
    private final SocketFactory socketFactory;

    @SneakyThrows
    @Override
    public void run() {
        log.info("client started");
        Peer bootPeer = Peer.builder()
                .ip(bootPeerHost)
                .port(bootPeerPort)
                .build();
        while (listening && routingTableManager.getLocalPeerId() < 0) {
            log.info("client joining to peer: {}", bootPeer);
            this.join(bootPeer);
            Thread.sleep(30_000);
        }
        while (listening) {
            try {
                log.info("client coping RoutingTable from peer: {}", bootPeer);
                this.getRoutingTable(bootPeer);
                Thread.sleep(10_000);
            } catch (Exception e) {
                log.error("Could not connect to bootPeer: {}", bootPeer);
            }
        }
    }

    public Peer getPeerForId(Peer peer, Long id) {
        return null;
    }

    // WHEN ROUTING TABLE
    private Set<Long> provideRoutingTableForId(Long id) {
        Long highestId = routingTableManager.getHighestId().orElse(0L);
        Long prev = id + 1;
        Set<Long> ids = new LinkedHashSet<>();
        for (int i = 0; i < Math.sqrt(highestId) + 1; i++) {
            Long peer = (id + prev + prev) % highestId+1;
            prev = peer;
            ids.add(peer);
        }
        return ids;
    }

    private void getPeerForId(Set<Peer> routingTable, Long id) {
        Optional<Peer> peerById = routingTable
                .stream().filter(peer -> peer.getId() == id)
                .findFirst();

        if (peerById.isPresent()) {
            return; // first.get();
        }

        Optional<Peer> peerToAsk = routingTable
                .stream().sorted((peer1, peer2) -> peer2.getId().compareTo(peer1.getId()))
                .filter(peer -> peer.getId() < id)
                .findFirst()
                .map(peer -> this.getPeerForId(peer, id));

    }

    public void join(Peer bootPeer) {
        log.info("joining to Peer: {}", bootPeer);
        withSocket(bootPeer, (writer, reader) -> {
            Message message = Message.builder()
                    .content(listeningPort + "")
                    .type(MessageType.JOIN)
                    .build();
            jsonParser.toJson(
                    message
            ).ifPresentOrElse(
                    json -> {
                        try {
                            writer.println(json);
                            writer.flush();
                            String response = reader.readLine();
                            log.info("response from Peer: {} > {}", bootPeer, response);
                            Long newLocalPeerId = jsonParser.fromJson(response, Message.class)
                                    .map(m -> Long.valueOf(m.getContent()))
                                    .orElseThrow(() -> new RuntimeException("no id returned from the JOIN response"));

                            routingTableManager.setLocalPeerId(newLocalPeerId);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    () -> log.error("failed to parse the Message: {} to a JSON string", message)
            );
        });
    }
    public void getRoutingTable(Peer bootPeer) {
        log.info("get routing table for Peer: {}", bootPeer);
        withSocket(bootPeer, (writer, reader) -> {
            Message message = Message.builder()
                    .type(MessageType.ROUTING_TABLE)
                    .build();
            jsonParser.toJson(
                    message
            ).ifPresentOrElse(
                    json -> {
                        try {
                            writer.println(json);
                            writer.flush();
                            String response = reader.readLine();
                            log.info("response from Peer: {} > {}", bootPeer, response);
                            Set<Peer> copiedRoutingTable = jsonParser.fromJson(response, Message.class)
                                    .flatMap(m -> jsonParser.fromJson(m.getContent(), Peer.SET_TYPE_REFERENCE))
                                    .orElseThrow(() -> new RuntimeException("no RoutingTable returned from the ROUTING_TABLE response"));

                            copiedRoutingTable.forEach(routingTableManager::addPeer);
                            // routingTableManager.addPeer(bootPeer);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    () -> log.error("failed to parse the Message: {} to a JSON string", message)
            );
        });
    }

    public void execute(String command) {
        log.info("Broadcasting command to execute: {}", command);
        routingTableManager.getRoutingTable().forEach(peer -> {
            withSocket(peer, (writer, reader) -> {
                Message message = Message.builder()
                        .content(command)
                        .type(MessageType.EXECUTE)
                        .build();
                jsonParser.toJson(
                        message
                ).ifPresentOrElse(
                        json -> {
                            try {
                                writer.println(json);
                                writer.flush();
                                String response = reader.readLine();
                                String content = jsonParser.fromJson(response, Message.class)
                                        .map(m -> m.getContent())
                                        .orElseThrow(() -> new RuntimeException("no content returned from the EXECUTE response"));
                                System.out.println(
                                        String.format("%s:%d > %s", peer.getIp(), peer.getPort(), content)
                                );
                            } catch (IOException e) {
                                log.error("Error while reading EXECUTE response: {}", e.getMessage());
                            }
                        },
                        () -> log.error("failed to parse the Message: {} to a JSON string", message)
                );
            });
        });
    }

    public void withSocket(Peer bootPeer, BiConsumer<PrintWriter, BufferedReader> consumer) {
        try (
            final Socket socket = socketFactory.createClientSocket(bootPeer.getIp(), bootPeer.getPort());
            final OutputStream outputStream = socket.getOutputStream();
            final PrintWriter writer = new PrintWriter(outputStream);
            final InputStream inputStream = socket.getInputStream();
            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            final BufferedReader reader = new BufferedReader(inputStreamReader);
        ) {
            consumer.accept(writer, reader);
        } catch (IOException e) {
            log.error("socket communication to peer: {} failed: {}", bootPeer, e.getMessage());
        }
    }
}
