package br.pucpr.sportsserver.rest.users.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.br.CPF;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    @NotBlank
    @Length(min = 3, max = 30)
    private String username;
    @NotBlank
    @Length(min = 3, max = 30)
    private String name;
    @NotBlank
    @Length(min = 3, max = 30)
    private String password;
    @NotBlank @Email
    private String email;
    @NotBlank
    @Length(min = 11, max = 11)
    private String cpf;
    @NotBlank
    private String city;
    @NotNull
    @Min(13) @Max(120)
    private Integer age;
    @NotEmpty
    private Set<String> sports = new HashSet<>();
}
