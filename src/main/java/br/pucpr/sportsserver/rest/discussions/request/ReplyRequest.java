package br.pucpr.sportsserver.rest.discussions.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReplyRequest {
    @NotNull
    private Long discussionId;
    @NotBlank
    private String text;
}
