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

    @GetMapping("followers")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public List<UserResponse> searchMeFollowers() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.searchFollowers(user.getId());
    }

    @GetMapping("following")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public List<UserResponse> searchMeFollowing() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.searchFollowing(user.getId());
    }

    @GetMapping("{username}/followers")
    @Transactional
    public List<UserResponse> searchFollowersByUsername(@Valid @PathVariable("username") String username) {
        return service.searchFollowersByUsername(username);
    }

    @GetMapping("{username}/following")
    @Transactional
    public List<UserResponse> searchFollowingByUsername(@Valid @PathVariable("username") String username) {
        return service.searchFollowingByUsername(username);
    }

    @GetMapping("blocked")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public List<String> searchBlocked() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.searchBlocked(user.getId());
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

