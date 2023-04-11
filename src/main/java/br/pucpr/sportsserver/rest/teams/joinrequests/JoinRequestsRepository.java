package br.pucpr.sportsserver.rest.teams.joinrequests;

import br.pucpr.sportsserver.rest.teams.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JoinRequestsRepository extends JpaRepository<JoinRequest, Long> {
    List<JoinRequest> findAllByTeamName(String teamName);
    List<JoinRequest> findAllByUserIdAndTeamName(Long userId, String teamName);
}
