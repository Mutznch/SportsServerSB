package br.pucpr.sportsserver.rest.teams;

import br.pucpr.sportsserver.rest.sports.SportsService;
import br.pucpr.sportsserver.rest.sports.response.SportResponse;
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
@RequestMapping("/teams")
public class TeamsResource {
    private TeamsService service;

    public TeamsResource(TeamsService service) {
        this.service = service;
    }

    @GetMapping
    @Transactional
    public List<String> searchTeams(
            @Valid @RequestParam(value = "userId", required = false) Long userId,
            @Valid @RequestParam(value = "username", required = false) String username,
            @Valid @RequestParam(value = "name", required = false) String name,
            @Valid @RequestParam(value = "sport", required = false) String sport
    ) {
        return service.search(userId, username, name, sport);
    }
    /*
    @GetMapping("me")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"USER"})
    public List<String> searchMe() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.search(user.getId());
    }

    @PostMapping
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"ADMIN"})
    public SportResponse addSport(@Valid @RequestBody String sport) {
        return service.add(sport);
    }

    @DeleteMapping("{name}")
    @Transactional
    @SecurityRequirement(name = "AuthServer")
    @RolesAllowed({"ADMIN"})
    public ResponseEntity<Void> delete(@PathVariable("name") String name) {
        service.delete(name);
        return ResponseEntity.ok().build();
    }*/
}
