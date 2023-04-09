package br.pucpr.sportsserver.rest.discussions.replies;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyRepository extends JpaRepository<Long, Reply> {
}
