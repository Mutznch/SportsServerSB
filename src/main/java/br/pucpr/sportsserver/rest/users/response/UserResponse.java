package br.pucpr.sportsserver.rest.users.response;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    private Set<String> roles = new HashSet<>();
}
