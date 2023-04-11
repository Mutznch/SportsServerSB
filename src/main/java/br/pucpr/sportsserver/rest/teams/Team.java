package br.pucpr.sportsserver.rest.teams;

import br.pucpr.sportsserver.rest.sports.Sport;
import br.pucpr.sportsserver.rest.teams.joinrequests.JoinRequest;
import br.pucpr.sportsserver.rest.users.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.HashSet;
import java.util.Set;

@Entity @Data
@AllArgsConstructor
@NoArgsConstructor
public class Team {
    @Id @GeneratedValue
    private Long id;
    @NotBlank
    @Length(min = 3, max = 30)
    @Column(unique = true)
    private String name;
    @ManyToOne
    private Sport sport;
    @ManyToOne
    private User leader;
    @ManyToMany(mappedBy = "allTeams")
    private Set<User> members = new HashSet<>();
    @OneToMany(mappedBy = "team")
    private Set<JoinRequest> joinRequests = new HashSet<>();

    public Team(String name, Sport sport, User user) {
        this.name = name;
        this.sport = sport;
        this.leader = user;
    }

    public void addMember(User user) {
        members.add(user);
        user.addTeam(this);
    }

    public void removeMember(User member) {
        member.getAllTeams().remove(this);
        members.remove(member);
    }

    public void clearTeam() {
        leader.getOwnedTeams().remove(this);
        members.forEach(m -> m.getAllTeams().remove(this));
        leader = null;
        members.clear();
        sport.getTeams().remove(this);
        sport = null;
    }

    public void changeLeader(User newLeader) {
        leader.getOwnedTeams().remove(this);
        newLeader.getOwnedTeams().add(this);
        leader = newLeader;
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
