package br.pucpr.sportsserver.rest.users;

import br.pucpr.sportsserver.rest.comments.Comment;
import br.pucpr.sportsserver.rest.discussions.Discussion;
import br.pucpr.sportsserver.rest.discussions.replies.Reply;
import br.pucpr.sportsserver.rest.sports.Sport;
import br.pucpr.sportsserver.rest.teams.Team;
import br.pucpr.sportsserver.rest.teams.joinrequests.JoinRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.HashSet;
import java.util.Set;

@Entity @Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id @GeneratedValue
    private Long id;
    @NotBlank
    @Length(min = 3, max = 30)
    @Column(unique = true)
    private String username;
    @NotBlank
    @Length(min = 3, max = 30)
    private String name;
    @NotBlank
    @Length(min = 3, max = 30)
    private String password;
    @NotBlank @Email
    @Column(unique = true)
    private String email;
    @NotBlank
    @Length(min = 11, max = 11)
    @Column(unique = true)
    private String cpf;
    @NotBlank
    private String city;
    @NotNull
    @Min(13) @Max(120)
    private Integer age;
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_sport",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "sport_name"))
    private Set<Sport> sports = new HashSet<>();
    @OneToMany(mappedBy = "from")
    private Set<Comment> commentsFromUser;
    @OneToMany(mappedBy = "to")
    private Set<Comment> commentsToUser;
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name="blocked_users",
            joinColumns={@JoinColumn(name="user_id")},
            inverseJoinColumns={@JoinColumn(name="blocked_user_id")})
    private Set<User> blockedUsers = new HashSet<>();
    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "blockedUsers")
    private Set<User> blockedByOthers = new HashSet<>();
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name="user_followers",
            joinColumns={@JoinColumn(name="user_id")},
            inverseJoinColumns={@JoinColumn(name="following_user_id")})
    private Set<User> followers = new HashSet<>();
    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "followers")
    private Set<User> following = new HashSet<>();
    @OneToMany(mappedBy = "leader")
    private Set<Team> ownedTeams = new HashSet<>();
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_team",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "team_id"))
    private Set<Team> allTeams = new HashSet<>();
    @OneToMany(mappedBy = "user")
    private Set<JoinRequest> teamJoinRequests = new HashSet<>();
    @OneToMany(mappedBy = "user")
    private Set<Discussion> discussions = new HashSet<>();
    @OneToMany(mappedBy = "user")
    private Set<Reply> replies = new HashSet<>();
    private Set<String> roles = new HashSet<>();

    public User(String username, String name, String password, String email, String cpf, String city, Integer age, Set<String> roles) {
        this.username = username;
        this.name = name;
        this.password = password;
        this.email = email;
        this.cpf = cpf;
        this.city = city;
        this.age = age;
        this.roles = roles;
    }

    public void addSport(Sport sport) {
        sports.add(sport);
        sport.addUser(this);
    }

    public void addCommentFrom(Comment comment) {
        commentsFromUser.add(comment);
    }

    public void addCommentTo(Comment comment) {
        commentsToUser.add(comment);
    }

    public void addTeam(Team team) {
        allTeams.add(team);
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
