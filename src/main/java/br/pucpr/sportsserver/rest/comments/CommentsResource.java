package br.pucpr.sportsserver.rest.comments;

import br.pucpr.sportsserver.rest.comments.request.CommentRequest;
import br.pucpr.sportsserver.rest.comments.response.CommentResponse;
import br.pucpr.sportsserver.rest.users.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/comments")
public class CommentsResource {
    private CommentsService commentsService;

    public CommentsResource(CommentsService service) {
        this.commentsService = service;
    }

    @GetMapping("search")
    @Transactional
    public Set<CommentResponse> searchComment(
            @Valid @RequestParam(value = "id", required = false) Long id,
            @Valid @RequestParam(value = "from", required = false) String from,
            @Valid @RequestParam(value = "to", required = false) String to
    ) { return commentsService.search(id, from, to); }

    @GetMapping("rating/{username}")
    @Transactional
    public Double searchUserRating(@Valid @PathVariable String username) {
        return commentsService.searchUserRating(username);
    }

    @GetMapping("me/rating")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public Double searchMeRating() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return commentsService.searchUserRating(user.getUsername());
    }

    @GetMapping("from/me")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public Set<CommentResponse> searchCommentsFromMe() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return searchComment(null, user.getUsername(), null);
    }

    @GetMapping("to/me")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public Set<CommentResponse> searchCommentsToMe() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return searchComment(null, null, user.getUsername());
    }

    @PostMapping("me/{username}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public CommentResponse addComment(
            @Valid @RequestBody CommentRequest reqComment,
            @Valid @PathVariable("username") String to
    ) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return commentsService.add(reqComment, user.getUsername() , to);
    }

    @DeleteMapping("me/{username}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public ResponseEntity<Void> deleteComment(@Valid @PathVariable("username") String to) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        commentsService.delete(user.getUsername(), to);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{id}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"ADMIN"})
    public ResponseEntity<Void> deleteCommentById(@Valid @PathVariable("id") Long id) {
        commentsService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("me/{username}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public CommentResponse updateComment(
            @Valid @RequestBody CommentRequest reqComment,
            @Valid @PathVariable("username") String to
    ) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return commentsService.update(reqComment, user.getUsername(), to);
    }
}
