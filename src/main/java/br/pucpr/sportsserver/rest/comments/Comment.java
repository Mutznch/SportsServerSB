package br.pucpr.sportsserver.rest.comments;

import br.pucpr.sportsserver.rest.users.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    @Id @GeneratedValue
    private Long id;
    private String text;
    @NotNull
    @Min(1) @Max(10)
    private Integer rating;
    @ManyToOne
    private User from;
    @ManyToOne
    private User to;

    public Comment(String text, Integer rating) {
        this.text = text;
        this.rating = rating;
    }

    public void addUsers(User from, User to) {
        this.from = from;
        this.to = to;
        from.addCommentFrom(this);
        to.addCommentTo(this);
    }

    public void removeUsers() {
        this.from.getCommentsFromUser().remove(this);
        this.to.getCommentsToUser().remove(this);
        this.from = null;
        this.to = null;
    }

    public int hashCode() {
        return 1;
    }
}
