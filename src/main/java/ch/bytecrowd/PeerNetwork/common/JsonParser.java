package ch.bytecrowd.PeerNetwork.common;

import ch.bytecrowd.PeerNetwork.model.Peer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Log4j2
public class JsonParser {

    private final ObjectMapper jsonMapper;

    public Optional<String> toJson(Object object) {
        try {
            return Optional.of(
                    jsonMapper.writeValueAsString(object)
            );
        } catch (JsonProcessingException e) {
            log.error("JsonProcessing failed for: {}", object, e);
            return Optional.empty();
        }
    }

    public <T> Optional<T> fromJson(String json, Class<T> type) {
        try {
            return Optional.of(
                    jsonMapper.readValue(json, type)
            );
        } catch (JsonProcessingException e) {
            log.error("JsonProcessing failed for: {} > {}", type, json, e);
            return Optional.empty();
        }
    }
    public <T> Optional<T> fromJson(String json, TypeReference<T> type) {
        try {
            return Optional.of(
                    jsonMapper.readValue(json, type)
            );
        } catch (JsonProcessingException e) {
            log.error("JsonProcessing failed for: {} > {}", type, json, e);
            return Optional.empty();
        }
    }
}
