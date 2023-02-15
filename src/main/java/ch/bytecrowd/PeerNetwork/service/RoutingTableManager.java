package ch.bytecrowd.PeerNetwork.service;

import ch.bytecrowd.PeerNetwork.model.Peer;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class RoutingTableManager {

    private Set<Peer> peers = new LinkedHashSet<>();
    private Long localPeerId = -1L;

    RoutingTableManager addPeer(Peer peer) {
        boolean noMatch = peers.stream()
                .noneMatch(p ->
                    p.getPort() == peer.getPort()
                    && p.getIp().equals(peer.getIp())
                );
        //if (noMatch) {
            peers.add(peer);
        //}
        return this;
    }

    void removePeerById(Long id) {
        peers.removeIf(peer -> peer.getId().equals(id));
    }

    public Set<Peer> getRoutingTable() {
        return peers;
    }

    public void setRoutingTable(Set<Peer> peers) {
        this.peers = peers;
    }

    public Long findNextId() {
        Long highestId = getHighestId().orElse(localPeerId);
        return highestId + 1L;
    }

    public Optional<Long> getHighestId() {
        return this.getRoutingTable()
                .stream().sorted((peer1, peer2) -> peer2.getId().compareTo(peer1.getId()))
                .findFirst()
                .map(Peer::getId);
    }

    public void setLocalPeerId(Long localPeerId) {
        this.localPeerId = localPeerId;
    }

    public Long getLocalPeerId() {
        return this.localPeerId;
    }
}
