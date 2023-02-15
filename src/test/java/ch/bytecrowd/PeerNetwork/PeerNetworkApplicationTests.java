package ch.bytecrowd.PeerNetwork;

import ch.bytecrowd.PeerNetwork.conf.ServerSocketTestConfig;
import ch.bytecrowd.PeerNetwork.service.ListenerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(ServerSocketTestConfig.class)
class PeerNetworkApplicationTests {

	@Autowired
	ListenerService listenerService;

	@Test
	void contextLoads() {

	}

}
