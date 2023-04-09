package br.pucpr.sportsserver.rest.discussions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussionsRepository  extends JpaRepository<Long, Discussion> {
}
