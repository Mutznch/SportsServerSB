package br.pucpr.sportsserver.rest.comments.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private String from;
    private String to;
    private Integer rating;
    private String text;
}
