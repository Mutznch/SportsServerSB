package br.pucpr.sportsserver.rest.sports;

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
@RequestMapping("/sports")
public class SportsResource {
    private SportsService service;

    public SportsResource(SportsService service) {
        this.service = service;
        this.service.createSports();
    }

    @GetMapping
    @Transactional
    public List<String> searchSport(@Valid @RequestParam(value = "userId", required = false) Long userId) {
        return service.search(userId);
    }

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
    }
}
