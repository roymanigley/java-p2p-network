package ch.bytecrowd.PeerNetwork.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    private MessageType type;
    private String content;
}
