package br.pucpr.sportsserver.rest.teams.request;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamRequest {
    @NotBlank
    @Length(min = 3, max = 30)
    @Column(unique = true)
    private String name;
    @NotBlank
    private String sport;
}
