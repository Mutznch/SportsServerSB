package br.pucpr.sportsserver.rest.teams;

import br.pucpr.sportsserver.lib.exception.BadRequestException;
import br.pucpr.sportsserver.lib.exception.ForbiddenException;
import br.pucpr.sportsserver.lib.exception.NotFoundException;
import br.pucpr.sportsserver.rest.sports.Sport;
import br.pucpr.sportsserver.rest.sports.SportsRepository;
import br.pucpr.sportsserver.rest.teams.joinrequests.JoinRequest;
import br.pucpr.sportsserver.rest.teams.joinrequests.JoinRequestsRepository;
import br.pucpr.sportsserver.rest.teams.request.TeamRequest;
import br.pucpr.sportsserver.rest.teams.response.TeamResponse;
import br.pucpr.sportsserver.rest.users.User;
import br.pucpr.sportsserver.rest.users.UsersRepository;
import br.pucpr.sportsserver.rest.users.request.UserRequest;
import br.pucpr.sportsserver.rest.users.response.UserResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeamsService {
    private TeamsRepository teamsRepository;
    private JoinRequestsRepository joinRequestsRepository;
    private UsersRepository usersRepository;
    private SportsRepository sportsRepository;


    public TeamsService(
            TeamsRepository teamsRepository,
            JoinRequestsRepository joinRequestsRepository,
            UsersRepository usersRepository,
            SportsRepository sportsRepository

    ) {
        this.teamsRepository = teamsRepository;
        this.joinRequestsRepository = joinRequestsRepository;
        this.usersRepository = usersRepository;
        this.sportsRepository = sportsRepository;

    }

    private Team reqToTeam(TeamRequest reqTeam, User leader) {
        return new Team(
                reqTeam.getName(),
                sportsRepository.findById(reqTeam.getSport())
                        .orElseThrow(() -> new BadRequestException("Invalid Sport Name")),
                leader
        );
    }

    private TeamResponse teamToRes(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getSport().getName(),
                team.getLeader().getUsername(),
                team.getMembers().stream()
                        .map(User::getUsername)
                        .collect(Collectors.toSet())
        );
    }

    public List<TeamResponse> search(Long userId, String username, String name, String sport) {
        if (userId != null)
            return usersRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("Id \"" + userId + "\" Not Found"))
                    .getAllTeams().stream().map(this::teamToRes).toList();
        if (username != null)
            return usersRepository.findByUsername(username)
                    .orElseThrow(() -> new NotFoundException("Username \"" + username + "\" Not Found"))
                    .getAllTeams().stream().map(this::teamToRes).toList();
        if (name != null)
            return List.of(teamToRes(teamsRepository.findByName(name)
                    .orElseThrow(() -> new NotFoundException("Team \"" + name + "\" Not Found"))));
        if (sport != null)
            return teamsRepository.findAllBySportName(sport)
                    .stream().map(this::teamToRes).toList();
        return teamsRepository.findAll()
                .stream().map(this::teamToRes).toList();
    }

    public List<String> searchTeamJoinRequests(Long id, String name, boolean invited) {
        var team = teamsRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Team \"" + name + "\" Not Found"));
        var req = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        if (team.getLeader().getId() != id && !req.getRoles().contains("ADMIN"))
            throw new ForbiddenException("Only team leader can access this route");
        return invited ?
                team.getJoinRequests().stream()
                    .filter(JoinRequest::getInvited)
                    .map(j -> j.getUser().getUsername())
                    .toList() :
                team.getJoinRequests().stream()
                        .filter(j -> !j.getInvited())
                        .map(j -> j.getUser().getUsername())
                        .toList();
    }

    public List<TeamResponse> searchUserJoinRequests(Long id, String name, boolean invited) {
        var user = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        if (!teamsRepository.existsByName(name))
            throw new NotFoundException("Team \"" + name + "\" Not Found");
        return invited ?
                user.getTeamJoinRequests().stream()
                        .filter(JoinRequest::getInvited)
                        .map(j -> teamToRes(j.getTeam()))
                        .toList() :
                user.getTeamJoinRequests().stream()
                        .filter(j -> !j.getInvited())
                        .map(j -> teamToRes(j.getTeam()))
                        .toList();
    }

    public TeamResponse addTeam(Long id, TeamRequest teamReq) {
        var user = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        if (teamsRepository.existsByName(teamReq.getName()))
            throw new BadRequestException("Team name already in use");
        var team = reqToTeam(teamReq, user);
        user.getOwnedTeams().add(team);
        return teamToRes(teamsRepository.save(team));
    }

    public void joinTeam(Long id, String name) {
        var user = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        var team = teamsRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Team \"" + name + "\" Not Found"));
        if (team.getMembers().contains(user))
            throw new BadRequestException("You already are a member of this team");
        var joinRequests = joinRequestsRepository.findAllByUserIdAndTeamName(id, name);
        if (!joinRequests.isEmpty()) {
            if (joinRequests.get(0).getInvited())
                addMember(user, team, joinRequests.get(0));
            else throw new BadRequestException("You have already send a join request to this team");
            return;
        }
        var joinRequest = new JoinRequest(user, team, false);
        user.getTeamJoinRequests().add(joinRequest);
        team.getJoinRequests().add(joinRequest);
        joinRequestsRepository.save(joinRequest);
    }

    public void inviteJoin(Long id, String username, String name) {
        var user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Username \"" + username + "\" Not Found"));
        var team = teamsRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Team \"" + name + "\" Not Found"));
        var req = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        if (team.getLeader().getId() != id && !req.getRoles().contains("ADMIN"))
            throw new ForbiddenException("Only team leader can access this route");
        if (team.getMembers().contains(user))
            throw new BadRequestException(username + " already is a member of this team");
        var joinRequests = joinRequestsRepository.findAllByUserIdAndTeamName(user.getId(), name);
        if (!joinRequests.isEmpty()) {
            if (!joinRequests.get(0).getInvited())
                addMember(user, team, joinRequests.get(0));
            else throw new BadRequestException("You have already send a join request to this user");
            return;
        }
        var joinRequest = new JoinRequest(user, team, true);
        user.getTeamJoinRequests().add(joinRequest);
        team.getJoinRequests().add(joinRequest);
        joinRequestsRepository.save(joinRequest);
    }

    public void addMember(User user, Team team, JoinRequest joinRequest) {
        user.getAllTeams().add(team);
        team.getMembers().add(user);
        joinRequest.removeUsers();
        joinRequestsRepository.deleteById(joinRequest.getId());
        usersRepository.save(user);
        teamsRepository.save(team);
    }

    public void removeMember(Long id, String username, String name) {
        var user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Username \"" + username + "\" Not Found"));
        var team = teamsRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Team \"" + name + "\" Not Found"));
        var req = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        if (team.getLeader().getId() != id && !req.getRoles().contains("ADMIN"))
            throw new ForbiddenException("Only team leader can access this route");
        exitTeam(user.getId(), name);
    }

    public void exitTeam(Long id, String name) {
        var user = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        var team = teamsRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Team \"" + name + "\" Not Found"));
        var joinRequests = joinRequestsRepository.findAllByUserIdAndTeamName(user.getId(), name);
        if (!joinRequests.isEmpty()) {
            joinRequests.get(0).removeUsers();
            joinRequestsRepository.deleteById(joinRequests.get(0).getId());
            return;
        }
        if (!team.getMembers().contains(user))
            throw new BadRequestException("User isn't a member of this team");
        if (id == user.getId())
            throw new BadRequestException("Team leader can't be removed");
        team.removeMember(user);
        teamsRepository.save(team);
    }

    public void deleteTeam(Long id, String name) {
        var team = teamsRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Team \"" + name + "\" Not Found"));
        var req = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        if (team.getLeader().getId() != id && !req.getRoles().contains("ADMIN"))
            throw new ForbiddenException("Only team leader can access this route");
        var joinRequests = joinRequestsRepository.findAllByTeamName(name);
        if (!joinRequests.isEmpty()) {
            joinRequests.forEach(j -> {
                j.removeUsers();
                joinRequestsRepository.deleteById(j.getId());
            });
        }
        team.removeMembers();
        teamsRepository.deleteById(team.getId());
    }

    public TeamResponse updateTeam(Long id, String currentName, String newName, String newLeader, String sport) {
        var team = teamsRepository.findByName(currentName)
                .orElseThrow(() -> new NotFoundException("Team \"" + currentName + "\" Not Found"));
        var req = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        if (team.getLeader().getId() != id && !req.getRoles().contains("ADMIN"))
            throw new ForbiddenException("Only team leader can access this route");
        if (newName != null) {
            if (teamsRepository.existsByName(newName))
                throw new BadRequestException("Team name already in use");
            team.setName(newName);
        }
        if (newLeader != null) {
            var leader = usersRepository.findByUsername(newLeader)
                    .orElseThrow(() -> new NotFoundException("Username \"" + newLeader + "\" Not Found"));
            team.changeLeader(leader);
        }
        if (sport != null)
            team.setSport(sportsRepository.findById(sport)
                    .orElseThrow(() -> new BadRequestException("Invalid Sport Name")));
        return teamToRes(teamsRepository.save(team));
    }
}
