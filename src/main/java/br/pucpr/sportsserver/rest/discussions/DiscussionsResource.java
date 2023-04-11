package br.pucpr.sportsserver.rest.discussions;

import br.pucpr.sportsserver.rest.discussions.request.DiscussionRequest;
import br.pucpr.sportsserver.rest.discussions.response.DiscussionResponse;
import br.pucpr.sportsserver.rest.discussions.response.ReplyResponse;
import br.pucpr.sportsserver.rest.users.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
    public List<DiscussionResponse> searchDiscussions(
            @Valid @RequestParam(value = "userId", required = false) Long userId,
            @Valid @RequestParam(value = "username", required = false) String username,
            @Valid @RequestParam(value = "title", required = false) String title,
            @Valid @RequestParam(value = "sport", required = false) String sport
    ) {
        return service.searchDiscussions(userId, username, title, sport);
    }

    @GetMapping("me")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public List<DiscussionResponse> searchMeDiscussions() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.searchDiscussions(user.getId(),null,null,null);
    }

    @GetMapping("replies")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public List<ReplyResponse> searchMeReplies() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.searchReplies(user.getId());
    }

    @GetMapping("{username}/replies")
    @Transactional
    public List<ReplyResponse> searchRepliesByUsername(@Valid @PathVariable("username") String username) {
        return service.searchRepliesByUsername(username);
    }

    @PostMapping
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public DiscussionResponse addDiscussion(@Valid @RequestBody DiscussionRequest reqDiscussion) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.addDiscussion(user.getId(), reqDiscussion);
    }

    @PostMapping("{discussionId}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public ReplyResponse addReply(
            @Valid @PathVariable("discussionId") Long discussionId,
            @Valid @RequestBody String text
    ) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.addReply(user.getId(), discussionId, text);
    }

    @DeleteMapping("{discussionId}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public ResponseEntity<Void> deleteDiscussion(@Valid @PathVariable("discussionId") Long discussionId) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        service.deleteDiscussion(user.getId(),discussionId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("replies/{replyId}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public ResponseEntity<Void> deleteReply(@Valid @PathVariable("replyId") Long replyId) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        service.deleteReply(user.getId(),replyId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("{discussionId}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public DiscussionResponse updateDiscussion(
            @Valid @PathVariable("discussionId") Long discussionId,
            @Valid @RequestParam(value = "title", required = false) String title,
            @Valid @RequestParam(value = "text", required = false) String text,
            @Valid @RequestParam(value = "sport", required = false) String sport
    ) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.updateDiscussion(user.getId(), discussionId, title, text, sport);
    }

    @PutMapping("replies/{replyId}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public ReplyResponse updateReply(
            @Valid @PathVariable("replyId") Long replyId,
            @Valid @RequestBody String text
    ) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.updateReply(user.getId(), replyId, text);
    }

}
