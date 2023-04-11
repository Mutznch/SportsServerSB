package br.pucpr.sportsserver.rest.discussions.replies;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepliesRepository extends JpaRepository<Reply, Long> {
}
