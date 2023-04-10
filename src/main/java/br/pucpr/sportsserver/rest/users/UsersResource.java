package br.pucpr.sportsserver.rest.users;

import br.pucpr.sportsserver.lib.exception.NotFoundException;
import br.pucpr.sportsserver.lib.security.JWT;
import br.pucpr.sportsserver.rest.users.request.Login;
import br.pucpr.sportsserver.rest.users.request.UserRequest;
import br.pucpr.sportsserver.rest.users.response.Token;
import br.pucpr.sportsserver.rest.users.response.UserResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/users")
public class UsersResource {
    private JWT jwt;
    private UsersService service;

    public UsersResource(JWT jwt, UsersService service) {
        this.jwt = jwt;
        this.service = service;

        this.service.createAdmin();
    }

    @PostMapping("/login")
    public ResponseEntity<Token> login(
            @Valid @RequestBody Login credentials
    ) {
        var user = service.validateUser(credentials);
        var token = jwt.createToken(user);
        return token == null ?
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).build() :
                ResponseEntity.ok(new Token(token));
    }

    @PostMapping
    @Transactional
    public UserResponse add(@Valid @RequestBody UserRequest reqUser) {
        return service.addUser(reqUser);
    }

    @GetMapping("me")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public List<UserResponse> searchMe() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return searchUser(user.getId(), null, null, null);
    }

    @GetMapping("search")
    @Transactional
    public List<UserResponse> searchUser(
            @Valid @RequestParam(value = "id", required = false) Long id,
            @Valid @RequestParam(value = "username", required = false) String username,
            @Valid @RequestParam(value = "city", required = false) String city,
            @Valid @RequestParam(value = "sport", required = false) String sport
    ) { return service.searchUser(id, username, city, sport); }

    @GetMapping("me/friendRequests")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public List<String> searchMyFriendRequests() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var friends = service.searchMyFriendRequests(user.getId());
        if (friends.isEmpty())
            throw new NotFoundException("You have no friend requests pending");
        return friends;
    }

    @GetMapping("friendRequests/me")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public List<String> searchFriendRequests() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var friends = service.searchFriendRequests(user.getId());
        if (friends.isEmpty())
            throw new NotFoundException("You have no friend requests pending");
        return friends;
    }

    @GetMapping("friends")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public List<String> searchMeFriends() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var friends = service.searchFriends(user.getId());
        if (friends.isEmpty())
            throw new NotFoundException("You have no friends yet");
        return friends.stream().toList();
    }

    @GetMapping("{username}/friends")
    @Transactional
    public List<String> searchFriends(@Valid @PathVariable("username") String username) {
        var friends = service.searchFriendsByUsername(username);
        if (friends.isEmpty())
            throw new NotFoundException(username + " has no friends yet");
        return friends.stream().toList();
    }

    @GetMapping("followers")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public List<String> searchMeFollowers() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var followers = service.searchFollowers(user.getId());
        if (followers.isEmpty())
            throw new NotFoundException("You have no followers yet");
        return followers;
    }

    @GetMapping("following")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public List<String> searchMeFollowing() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var following = service.searchFollowing(user.getId());
        if (following.isEmpty())
            throw new NotFoundException("You haven't follow anyone yet");
        return following;
    }

    @GetMapping("{username}/followers")
    @Transactional
    public List<String> searchFollowersByUsername(@Valid @PathVariable("username") String username) {
        var followers = service.searchFollowersByUsername(username);
        if (followers.isEmpty())
            throw new NotFoundException(username + " has no followers yet");
        return followers;
    }

    @GetMapping("{username}/following")
    @Transactional
    public List<String> searchFollowingByUsername(@Valid @PathVariable("username") String username) {
        var following = service.searchFollowingByUsername(username);
        if (following.isEmpty())
            throw new NotFoundException(username + " didn't follow anyone yet");
        return following;
    }

    @GetMapping("blocked")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public List<String> searchBlocked() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var blocked = service.searchBlocked(user.getId());
        if (blocked.isEmpty())
            throw new NotFoundException("You have no blocked users");
        return blocked;
    }

    @PostMapping("friends/{username}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public ResponseEntity<String> requestFriend(@Valid @PathVariable("username") String to) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok().body(service.requestFriend(user.getId(), to));
    }

    @PostMapping("follow/{username}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public ResponseEntity<Void> followUser(@Valid @PathVariable("username") String to) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        service.followUser(user.getId(), to);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("unfollow/{username}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public ResponseEntity<String> unfollowUser(@Valid @PathVariable("username") String to) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        service.unfollowUser(user.getId(), to);
        return ResponseEntity.ok().build();
    }

    @PostMapping("block/{username}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public ResponseEntity<String> blockUser(@Valid @PathVariable("username") String to) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        service.blockUser(user.getId(), to);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("unblock/{username}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public ResponseEntity<String> unblockUser(@Valid @PathVariable("username") String to) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        service.unblockUser(user.getId(), to);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("me")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public ResponseEntity<Void> deleteMe() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        service.delete(user.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{id}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"ADMIN"})
    public ResponseEntity<Void> deleteUser(@Valid @PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("friends/{username}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public ResponseEntity<Void> removeFriend(@Valid @PathVariable("username") String friend) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        service.removeFriend(user.getId(), friend);
        return ResponseEntity.ok().build();
    }

    @PutMapping("me")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public UserResponse updateUserData(
            @Valid @RequestParam(value = "username", required = false) String username,
            @Valid @RequestParam(value = "name", required = false) String name,
            @Valid @RequestParam(value = "password", required = false) String password,
            @Valid @RequestParam(value = "email", required = false) String email,
            @Valid @RequestParam(value = "cpf", required = false) String cpf,
            @Valid @RequestParam(value = "city", required = false) String city,
            @Valid @RequestParam(value = "age", required = false) Integer age
    ) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.updateData(user.getId(), username, name, password, email, cpf, city, age);
    }

    @PutMapping("me/sports")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public UserResponse updateUserSports(
            @Valid @RequestBody Set<String> sports
            ) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.updateSports(user.getId(), sports);
    }
}

