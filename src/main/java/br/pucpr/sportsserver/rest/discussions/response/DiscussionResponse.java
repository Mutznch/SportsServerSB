package br.pucpr.sportsserver.rest.discussions.response;

import br.pucpr.sportsserver.rest.sports.Sport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscussionResponse {
    private Long id;
    private String user;
    private String sport;
    private String title;
    private String text;
    private Set<ReplyResponse> replies = new HashSet<>();
}
