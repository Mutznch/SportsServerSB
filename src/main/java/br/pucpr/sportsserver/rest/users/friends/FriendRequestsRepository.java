package br.pucpr.sportsserver.rest.users.friends;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRequestsRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findAllByFromId(Long fromId);
    List<FriendRequest> findAllByToId(Long toId);
    List<FriendRequest> findAllByFromIdAndToId(Long fromId, Long toId);
    boolean existsByFromIdAndToId(Long fromId, Long toId);
}
