package br.pucpr.sportsserver.rest.sports;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SportsRepository extends JpaRepository<Sport, String> {
    List<Sport> findAllByUsersId(Long id);
}
