package br.pucpr.sportsserver.rest.sports;

import br.pucpr.sportsserver.rest.discussions.Discussion;
import br.pucpr.sportsserver.rest.teams.Team;
import br.pucpr.sportsserver.rest.users.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sport {
    @NotBlank @Id
    private String name;
    @ManyToMany(mappedBy = "sports")
    private Set<User> users = new HashSet<>();
    @OneToMany(mappedBy = "sport")
    private Set<Team> teams = new HashSet<>();
    @OneToMany(mappedBy = "sport")
    private Set<Discussion> discussions = new HashSet<>();

    public Sport(String name) {
        this.name = name;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void clearSport() {
        users.forEach(u -> u.getSports().remove(this));
        users.clear();
        teams.forEach(t -> t.setSport(null));
        users.clear();
        discussions.forEach(d -> d.setSport(null));
        users.clear();
    }

    public int hashCode() {
        return 1;
    }
}
