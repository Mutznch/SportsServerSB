package br.pucpr.sportsserver.rest.discussions.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscussionRequest {
    @Nullable
    private String sport;
    @NotBlank
    @Length(min = 3, max = 40)
    private String title;
    @NotBlank
    private String text;
}
