package br.pucpr.sportsserver.rest.comments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface CommentsRepository extends JpaRepository<Comment, Long> {
    Set<Comment> findAllByFromId(Long fromId);
    Set<Comment> findAllByToId(Long toId);
    Set<Comment> findAllByFromUsername(String username);
    Set<Comment> findAllByToUsername(String username);
    Optional<Comment> findByFromUsernameAndToUsername(String fromUsername, String toUsername);
}
