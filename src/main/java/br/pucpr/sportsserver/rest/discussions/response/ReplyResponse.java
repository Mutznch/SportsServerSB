package br.pucpr.sportsserver.rest.discussions.response;

import br.pucpr.sportsserver.rest.users.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReplyResponse {
    private Long id;
    private Long discussionId;
    private String user;
    private String text;
}
