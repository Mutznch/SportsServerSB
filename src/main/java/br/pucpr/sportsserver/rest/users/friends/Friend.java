package br.pucpr.sportsserver.rest.users.friends;

import br.pucpr.sportsserver.rest.users.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Friend {
    @Id @GeneratedValue
    private Long id;
    @NotNull
    @OneToOne
    private User user;
    @NotNull
    @OneToOne
    private User friend;

    public Friend(User user, User friend) {
        this.user = user;
        this.friend = friend;
    }
}
