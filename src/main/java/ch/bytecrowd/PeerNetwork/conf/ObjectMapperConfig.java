package ch.bytecrowd.PeerNetwork.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Log4j2
public class ObjectMapperConfig {

    @Bean
    ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }
}
