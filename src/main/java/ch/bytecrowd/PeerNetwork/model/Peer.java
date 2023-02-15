package ch.bytecrowd.PeerNetwork.model;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Peer {

    public static final TypeReference<Set<Peer>> SET_TYPE_REFERENCE = new TypeReference<>() {};

    private Long id;
    private String ip;
    private int port;


}
