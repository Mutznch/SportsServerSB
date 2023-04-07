package br.pucpr.sportsserver.rest.users.friends;

import br.pucpr.sportsserver.rest.users.friends.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendsRepository extends JpaRepository<Friend, Long> {
    boolean existsByUserIdAndFriendId(Long userId, Long friendId);
    List<Friend> findAllByUserIdAndFriendId(Long userId, Long friendId);
    List<Friend> findAllByUserId(Long userId);
    List<Friend> findAllByFriendId(Long friendId);
}
