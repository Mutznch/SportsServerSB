package br.pucpr.sportsserver.rest.users.response;

import br.pucpr.sportsserver.rest.teams.response.TeamResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String cpf;
    private String city;
    private Integer age;
    private Set<String> sports = new HashSet<>();
    private Set<String> friends = new HashSet<>();
    private Set<String> followers = new HashSet<>();
    private Set<String> following = new HashSet<>();
    private Set<String> teams = new HashSet<>();
    private Set<String> roles = new HashSet<>();
}
