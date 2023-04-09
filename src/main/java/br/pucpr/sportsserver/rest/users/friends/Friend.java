package br.pucpr.sportsserver.rest.users.friends;

import br.pucpr.sportsserver.rest.users.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Data
@AllArgsConstructor
@NoArgsConstructor
public class Friend {
    @Id @GeneratedValue
    private Long id;
    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    private User user;
    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    private User friend;

    public Friend(User user, User friend) {
        this.user = user;
        this.friend = friend;
    }
}
