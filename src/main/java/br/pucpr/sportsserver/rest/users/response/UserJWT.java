package br.pucpr.sportsserver.rest.users.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserJWT {
    private Long id;
    private String username;
    private Set<String> roles;
}
