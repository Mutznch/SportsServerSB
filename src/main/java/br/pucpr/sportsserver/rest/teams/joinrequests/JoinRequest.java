package br.pucpr.sportsserver.rest.teams.joinrequests;

import br.pucpr.sportsserver.rest.teams.Team;
import br.pucpr.sportsserver.rest.users.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Data
@AllArgsConstructor
@NoArgsConstructor
public class JoinRequest {
    @Id
    @GeneratedValue
    private Long id;
    @NotNull
    @ManyToOne(cascade = CascadeType.ALL)
    private User user;
    @NotNull
    @ManyToOne(cascade = CascadeType.ALL)
    private Team team;
    @NotNull
    private Boolean invited;

    public JoinRequest(User user, Team team, Boolean invited) {
        this.user = user;
        this.team = team;
        this.invited = invited;
        user.getTeamJoinRequests().add(this);
        team.getJoinRequests().add(this);
    }

    public void removeUsers() {
        this.user.getTeamJoinRequests().remove(this);
        this.team.getJoinRequests().remove(this);
        this.user = null;
        this.team = null;
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
