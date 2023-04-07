package br.pucpr.sportsserver.rest.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsernameAndPassword(String username, String password);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
    List<User> findAllByCity(String city);
    List<User> findAllBySportsName(String sportName);
    List<User> findAllByCityAndSportsName(String city, String sportName);
}
