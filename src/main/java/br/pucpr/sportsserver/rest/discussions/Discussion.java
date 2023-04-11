package br.pucpr.sportsserver.rest.discussions;

import br.pucpr.sportsserver.rest.discussions.replies.Reply;
import br.pucpr.sportsserver.rest.sports.Sport;
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
public class Discussion {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private User user;
    @ManyToOne
    private Sport sport;
    @NotBlank
    @Length(min = 3, max = 40)
    private String title;
    @NotBlank
    private String text;
    @OneToMany(mappedBy = "discussion")
    private Set<Reply> replies = new HashSet<>();

    public Discussion(User user, Sport sport, String title, String text) {
        this.user = user;
        this.sport = sport;
        this.title = title;
        this.text = text;
    }

    public void clearDiscussion() {
        user.getDiscussions().remove(this);
        user = null;
        sport.getDiscussions().remove(this);
        sport = null;
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
