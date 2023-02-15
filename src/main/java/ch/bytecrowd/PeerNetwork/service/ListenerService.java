package ch.bytecrowd.PeerNetwork.service;

import ch.bytecrowd.PeerNetwork.common.JsonParser;
import ch.bytecrowd.PeerNetwork.model.Message;
import ch.bytecrowd.PeerNetwork.model.MessageType;
import ch.bytecrowd.PeerNetwork.model.Peer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;
import org.springframework.scripting.support.StaticScriptSource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

@Service
@RequiredArgsConstructor
@Log4j2
public class ListenerService implements Runnable {

    @Value("${application.listening}")
    private boolean listening;


    private final ServerSocket server;
    private final JsonParser parserService;
    private final ClientService clientService;
    private final RoutingTableManager routingTableManager;

    @Override
    public void run() {
        while (listening) {
            handleSocketConnection();
        }
    }

    void handleSocketConnection() {
        try (final Socket socket = this.server.accept();
             final OutputStream outputStream = socket.getOutputStream();
             final PrintWriter writer = new PrintWriter(outputStream);
             final InputStream inputStream = socket.getInputStream();
             final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             final BufferedReader reader = new BufferedReader(inputStreamReader);
             ){
            log.info("receiving connection from {}:{}", socket.getInetAddress().getHostAddress(), socket.getPort());

            Message response = handleRequest(socket, reader);
            String jsonResponse = parserService.toJson(response).orElse("{}");

            System.out.println(response);

            log.debug("sending response @{}:{} {}", socket.getInetAddress().getHostAddress(), socket.getPort(), jsonResponse);
            writer.println(jsonResponse);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Message handleRequest(Socket socket, BufferedReader reader) throws IOException {
        String request = reader.readLine();
        log.info("receiving request from {}:{} > {}", socket.getInetAddress().getHostAddress(), socket.getPort(), request);
        return parserService.fromJson(request, Message.class)
                .map(m -> switch (m.getType()) {
                    case JOIN -> handleJoinRequest(m, socket);
                    case ROUTING_TABLE -> handleRoutingTableRequest(m);
                    case LEAVE -> handleLeaveRequest(m);
                    case EXECUTE -> handleExecuteRequest(m);
                    default -> null;
                })
                .orElseGet(() -> Message.builder().build());
    }

    private Message handleJoinRequest(Message m, Socket socket) {
        Long nextId = routingTableManager.findNextId();
        routingTableManager.addPeer(
                Peer.builder()
                        .id(nextId)
                        .ip(socket.getInetAddress().getHostAddress())
                        .port(Integer.valueOf(m.getContent()))
                        .build()
        );
        return Message.builder()
                .type(MessageType.JOIN)
                .content(
                        parserService.toJson(
                                nextId
                        ).orElse(null)
                )
                .build();
    }

    private Message handleRoutingTableRequest(Message message) {
        return Message.builder()
                .type(MessageType.ROUTING_TABLE)
                .content(
                        parserService.toJson(
                                routingTableManager.getRoutingTable()
                        ).orElse("[]")
                )
                .build();
    }

    private Message handleLeaveRequest(Message message) {
        Long idToRemove = Long.valueOf(message.getContent());
        routingTableManager.removePeerById(idToRemove);
        return Message.builder()
                .type(MessageType.LEAVE)
                .content(
                        "bye"
                )
                .build();
    }

    private Message handleExecuteRequest(Message message) {
        StaticScriptSource scriptSource = new StaticScriptSource(message.getContent());
        String response = new GroovyScriptEvaluator().evaluate(scriptSource).toString();
        return Message.builder()
                .type(MessageType.EXECUTE)
                .content(
                        response
                )
                .build();
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }
}