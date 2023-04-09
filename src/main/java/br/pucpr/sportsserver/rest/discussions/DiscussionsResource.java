package br.pucpr.sportsserver.rest.discussions;

import br.pucpr.sportsserver.rest.teams.response.TeamResponse;
import br.pucpr.sportsserver.rest.users.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/discussions")
public class DiscussionsResource {
    private DiscussionsService service;

    public DiscussionsResource(DiscussionsService service) {
        this.service = service;
    }
    @GetMapping
    @Transactional
    public List<TeamResponse> searchDiscussions(
            @Valid @RequestParam(value = "userId", required = false) Long userId,
            @Valid @RequestParam(value = "username", required = false) String username,
            @Valid @RequestParam(value = "title", required = false) String title,
            @Valid @RequestParam(value = "sport", required = false) String sport
    ) {
        return service.search(userId, username, title, sport);
    }

    @GetMapping("me")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public List<TeamResponse> searchMeDiscussions() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.search(user.getId(),null,null,null);
    }

    @GetMapping("replies")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public List<TeamResponse> searchMeReplies() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.searchReplies(user.getId());
    }

    @GetMapping("{username}/replies")
    @Transactional
    public List<TeamResponse> searchRepliesByUsername(@Valid @PathVariable("username") String username) {
        return service.searchRepliesByUsername(username);
    }


}
