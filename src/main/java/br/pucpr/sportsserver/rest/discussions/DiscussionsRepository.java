package br.pucpr.sportsserver.rest.discussions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscussionsRepository  extends JpaRepository<Discussion, Long> {
    List<Discussion> findAllByTitle(String title);
    List<Discussion> findAllByTitleAndSportName(String title, String sportName);
}
