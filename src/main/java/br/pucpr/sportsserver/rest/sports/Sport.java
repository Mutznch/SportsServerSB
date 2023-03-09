package br.pucpr.sportsserver.rest.sports;

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

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUsers() {
        users.forEach(u -> u.getSports().remove(this));
        users.clear(); }

    public int hashCode() {
        return 1;
    }
}
