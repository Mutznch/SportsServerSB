package br.pucpr.sportsserver.rest.discussions.replies;

import br.pucpr.sportsserver.rest.discussions.Discussion;
import br.pucpr.sportsserver.rest.users.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Data
@AllArgsConstructor
@NoArgsConstructor
public class Reply {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne(cascade = CascadeType.ALL)
    private Discussion discussion;
    @ManyToOne(cascade = CascadeType.ALL)
    private User user;
    @NotBlank
    private String text;

    public Reply(Discussion discussion, User user, String text) {
        this.discussion = discussion;
        this.user = user;
        this.text = text;
    }

    public void clearReply() {
        discussion.getReplies().remove(this);
        discussion = null;
        user.getReplies().remove(this);
        user = null;
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
