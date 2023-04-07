package br.pucpr.sportsserver.rest.users.friends;

import br.pucpr.sportsserver.rest.users.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequest {
    @Id @GeneratedValue
    private Long id;
    @NotNull
    @ManyToOne
    private User from;
    @NotNull
    @ManyToOne
    private User to;

    public FriendRequest(User from, User to) {
        this.from = from;
        this.to = to;
        from.getFriendRequestsFromUser().add(this);
        to.getFriendRequestsToUser().add(this);
    }

    public void removeUsers() {
        this.from.getFriendRequestsFromUser().remove(this);
        this.to.getFriendRequestsToUser().remove(this);
        this.from = null;
        this.to = null;
    }
}
