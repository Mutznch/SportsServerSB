package br.pucpr.sportsserver.rest.teams;

import br.pucpr.sportsserver.rest.sports.SportsService;
import br.pucpr.sportsserver.rest.sports.response.SportResponse;
import br.pucpr.sportsserver.rest.teams.request.TeamRequest;
import br.pucpr.sportsserver.rest.teams.response.TeamResponse;
import br.pucpr.sportsserver.rest.users.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
public class TeamsResource {
    private TeamsService service;

    public TeamsResource(TeamsService service) {
        this.service = service;
    }

    @GetMapping
    @Transactional
    public List<TeamResponse> searchTeams(
            @Valid @RequestParam(value = "userId", required = false) Long userId,
            @Valid @RequestParam(value = "username", required = false) String username,
            @Valid @RequestParam(value = "name", required = false) String name,
            @Valid @RequestParam(value = "sport", required = false) String sport
    ) {
        return service.search(userId, username, name, sport);
    }

    @GetMapping("me")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public List<TeamResponse> searchMyTeams() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.search(user.getId(),null,null,null);
    }

    @GetMapping("{name}/requests")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public List<String> searchJoinRequests(@Valid @PathVariable("name") String name) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.searchTeamJoinRequests(user.getId(),name,false);
    }

    @GetMapping("{name}/invites")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public List<String> searchJoinInvites(@Valid @PathVariable("name") String name) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.searchTeamJoinRequests(user.getId(),name,true);
    }
    @GetMapping("requests")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public List<TeamResponse> searchMeJoinRequests() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.searchUserJoinRequests(user.getId(),false);
    }

    @GetMapping("invites")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public List<TeamResponse> searchMeJoinInvites() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.searchUserJoinRequests(user.getId(),true);
    }

    @PostMapping
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public TeamResponse addTeam(@Valid @RequestBody TeamRequest team) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.addTeam(user.getId(), team);
    }

    @PostMapping("{name}/join")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public ResponseEntity<Void> joinTeam(@Valid @PathVariable("name") String name) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        service.joinTeam(user.getId(),name);
        return ResponseEntity.ok().build();
    }

    @PostMapping("{name}/invite/{username}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public ResponseEntity<Void> inviteMember(
            @Valid @PathVariable("name") String name,
            @Valid @PathVariable("username") String username
    ) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        service.inviteJoin(user.getId(),username,name);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{name}/remove/{username}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public ResponseEntity<Void> removeMember(
            @Valid @PathVariable("name") String name,
            @Valid @PathVariable("username") String username
    ) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        service.removeMember(user.getId(),username,name);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{name}/exit")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public ResponseEntity<Void> exitTeam(@Valid @PathVariable("name") String name) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        service.exitTeam(user.getId(),name);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{name}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public ResponseEntity<Void> deleteTeam(@Valid @PathVariable("name") String name) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        service.deleteTeam(user.getId(),name);
        return ResponseEntity.ok().build();
    }

    @PutMapping("{currentName}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public TeamResponse changeTeam(
            @Valid @PathVariable(value = "currentName", required = false) String currentName,
            @Valid @RequestParam(value = "name", required = false) String newName,
            @Valid @RequestParam(value = "leader", required = false) String newLeader,
            @Valid @RequestParam(value = "sport", required = false) String sport
    ) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.updateTeam(user.getId(),currentName,newName,newLeader,sport);
    }
}
