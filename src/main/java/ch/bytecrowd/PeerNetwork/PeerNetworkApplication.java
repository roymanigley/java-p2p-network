package ch.bytecrowd.PeerNetwork;

import ch.bytecrowd.PeerNetwork.model.Peer;
import ch.bytecrowd.PeerNetwork.service.ClientService;
import ch.bytecrowd.PeerNetwork.service.ListenerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@Log4j2
@RequiredArgsConstructor
public class PeerNetworkApplication {

	private final ListenerService listenerService;
	private final ClientService clientService;

	ExecutorService threadPool = Executors.newFixedThreadPool(4);

	public static void main(String[] args) {
		SpringApplication.run(PeerNetworkApplication.class, args);
	}

	@Profile("!execute")
	@Bean
	ApplicationRunner runner() {
		return args -> {
			threadPool.submit(listenerService);
			threadPool.submit(clientService);
			threadPool.shutdown();
			while (!threadPool.awaitTermination(5, TimeUnit.SECONDS)){
				log.debug("socket still running");
			};
		};
	}
	@Profile("execute")
	@Bean
	ApplicationRunner runnerForExecution() {
		return args -> {
			log.info("Execution mode");
			Peer bootPeer = Peer.builder()
					.ip("localhost")
					.port(4040)
					.build();

			clientService.getRoutingTable(bootPeer);

			try (Scanner scanner = new Scanner(System.in)) {
				String command;
				System.out.print("> ");
				while (!(command = scanner.nextLine()).toLowerCase().equals("exit")) {
					if (command.equals("renew")) {
						clientService.getRoutingTable(bootPeer);
					} else {
						clientService.execute(command);
					}
					System.out.print("> ");
				}
			} catch (Exception e) {
				log.error("error on execution: {}", e.getMessage());
			}
		};
	}
}
