package br.pucpr.sportsserver.rest.teams;

import br.pucpr.sportsserver.rest.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamsRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByName(String name);
    List<Team> findAllBySportName(String sportName);
    boolean existsByName(String name);
}
