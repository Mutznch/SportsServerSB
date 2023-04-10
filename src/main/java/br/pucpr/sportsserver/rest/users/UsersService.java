package br.pucpr.sportsserver.rest.users;

import br.pucpr.sportsserver.lib.exception.BadRequestException;
import br.pucpr.sportsserver.lib.exception.NotFoundException;
import br.pucpr.sportsserver.rest.comments.CommentsService;
import br.pucpr.sportsserver.rest.discussions.DiscussionsService;
import br.pucpr.sportsserver.rest.sports.Sport;
import br.pucpr.sportsserver.rest.sports.SportsRepository;
import br.pucpr.sportsserver.rest.teams.Team;
import br.pucpr.sportsserver.rest.teams.TeamsService;
import br.pucpr.sportsserver.rest.users.friends.FriendRequest;
import br.pucpr.sportsserver.rest.users.friends.FriendRequestsRepository;
import br.pucpr.sportsserver.rest.users.request.Login;
import br.pucpr.sportsserver.rest.users.request.UserRequest;
import br.pucpr.sportsserver.rest.users.response.UserJWT;
import br.pucpr.sportsserver.rest.users.response.UserResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UsersService {
    private UsersRepository usersRepository;
    private SportsRepository sportsRepository;
    private CommentsService commentsService;
    private FriendRequestsRepository friendRequestsRepository;
    private TeamsService teamsService;
    private DiscussionsService discussionsService;

    public UsersService(
            UsersRepository usersRepository,
            SportsRepository sportsRepository,
            CommentsService commentsService,
            FriendRequestsRepository friendRequestsRepository,
            TeamsService teamsService,
            DiscussionsService discussionsService
    ) {
        this.usersRepository = usersRepository;
        this.sportsRepository = sportsRepository;
        this.commentsService = commentsService;
        this.friendRequestsRepository = friendRequestsRepository;
        this.teamsService = teamsService;
        this.discussionsService = discussionsService;
    }

    private User reqToUser(UserRequest reqUser) {
        Set<String> roles = new HashSet<>();
        roles.add("USER");

        return new User(
                reqUser.getUsername(),
                reqUser.getName(),
                reqUser.getPassword(),
                reqUser.getEmail(),
                reqUser.getCpf(),
                reqUser.getCity(),
                reqUser.getAge(),
                roles
        );
    }

    private UserResponse userToRes(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getCpf(),
                user.getCity(),
                user.getAge(),
                user.getSports().stream().map(Sport::getName).collect(Collectors.toSet()),
                user.getFriends().stream().map(User::getUsername).collect(Collectors.toSet()),
                user.getFollowers().stream().map(User::getUsername).collect(Collectors.toSet()),
                user.getFollowing().stream().map(User::getUsername).collect(Collectors.toSet()),
                user.getAllTeams().stream().map(Team::getName).collect(Collectors.toSet()),
                user.getRoles()
        );
    }

    private void userDataExists(String username, String email, String cpf) {
        if (usersRepository.existsByUsername(username))
            throw new BadRequestException("Username already in use");
        if (usersRepository.existsByEmail(email))
            throw new BadRequestException("Email already in use");
        if (usersRepository.existsByCpf(cpf))
            throw new BadRequestException("Cpf already in use");
        try { Long.parseLong(cpf); }
        catch (NumberFormatException e) { throw new BadRequestException("Invalid CPF"); }
    }

    public UserJWT validateUser(Login credentials) {
        if (!(usersRepository.existsByUsernameAndPassword(credentials.getUsername(), credentials.getPassword())))
            throw new BadRequestException("Wrong Username or Password");
        var user = usersRepository
                .findByUsername(credentials.getUsername())
                .orElseThrow(() -> new NotFoundException("User Not Found"));
        return new UserJWT(user.getId(), user.getUsername(), user.getRoles());
    }

    public UserResponse addUser(UserRequest reqUser) {
        var user = reqToUser(reqUser);
        userDataExists(user.getUsername(), user.getEmail(), user.getCpf());

        reqUser.getSports().forEach(s ->
            user.addSport(sportsRepository.findById(s)
                    .orElseThrow(() -> new BadRequestException("\"" + s + "\" is not a valid sport")))
        );

        return userToRes(usersRepository.save(user));
    }

    public List<UserResponse> searchUser(Long id, String username, String city, String sport) {
        List<UserResponse> resUsers = new ArrayList<>();
        List<User> users = new ArrayList<>();
        if (id != null)
            users.add(usersRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found")));
        else if (username != null)
            users.add(usersRepository.findByUsername(username)
                    .orElseThrow(() -> new NotFoundException("Username \"" + username + "\" Not Found")));
        else if (city != null && sport != null)
            users.addAll(usersRepository.findAllByCityAndSportsName(city, sport));
        else if (city != null)
            users.addAll(usersRepository.findAllByCity(city));
        else if (sport != null)
            users.addAll(usersRepository.findAllBySportsName(sport));
        else
            users.addAll(usersRepository.findAll());

        if (users.isEmpty()) throw new NotFoundException("No Users Found");
        users.forEach(u -> resUsers.add(userToRes(u)));
        return resUsers;
    }

    public UserResponse updateData(Long id, String username, String name, String password, String email, String cpf, String city, Integer age) {
        userDataExists(username, email, cpf);
        var user = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        if (username != null) user.setUsername(username);
        if (name != null) user.setName(name);
        if (password != null) user.setPassword(password);
        if (email != null) user.setEmail(email);
        if (cpf != null) {user.setCpf(cpf);}
        if (city != null) user.setCity(city);
        if (age != null) user.setAge(age);

        return userToRes(usersRepository.save(user));
    }

    public String requestFriend(Long fromId, String toUsername) {
        var from = usersRepository.findById(fromId)
                .orElseThrow(() -> new NotFoundException("Id \"" + fromId + "\" Not Found"));
        var to = usersRepository.findByUsername(toUsername)
                .orElseThrow(() -> new NotFoundException("Username \"" + toUsername + "\" Not Found"));
        if (from.getId() == to.getId())
            throw new BadRequestException("Can't send a friend request to yourself");
        if (from.getBlockedUsers().contains(to))
            throw new BadRequestException("Can't be friend with a user you've blocked");
        if (from.getBlockedByOthers().contains(to))
            throw new BadRequestException("Unable to send a friend request to this user");
        if (friendRequestsRepository.existsByFromIdAndToId(from.getId(), to.getId()))
            throw new BadRequestException("Friend Request already sent to that user");
        if (from.getFriends().contains(to))
            throw new BadRequestException("You and " + toUsername + " are already friends");
        if (friendRequestsRepository.existsByFromIdAndToId(to.getId(), from.getId()))
            return addFriend(from, to);
        friendRequestsRepository.save(new FriendRequest(from, to));
        return "Friend request sent to " + toUsername;
    }

    public String addFriend(User user1, User user2) {
        user1.getFriends().add(user2);
        user2.getFriends().add(user1);
        user1.getFriendsWith().add(user2);
        user2.getFriendsWith().add(user1);

        user2.getFriendRequestsFromUser().stream().filter(f -> user2.equals(f.getTo())).forEach(f -> {
            friendRequestsRepository.deleteById(f.getId());
        });

        usersRepository.save(user1);
        usersRepository.save(user2);

        return "You and " + user2 + " are now friends!";
    }

    public Set<String> searchFriends(Long id) {
        var user = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        return user.getFriends().stream().map(User::getUsername).collect(Collectors.toSet());
    }

    public Set<String> searchFriendsByUsername(String username) {
        return searchFriends(
                usersRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Username \"" + username + "\" Not Found"))
                .getId()
        );
    }

    public List<String> searchMyFriendRequests(Long id) {
        return friendRequestsRepository.findAllByFromId(id)
                .stream().map(f -> f.getTo().getUsername())
                .toList();
    }

    public List<String> searchFriendRequests(Long id) {
        return friendRequestsRepository.findAllByToId(id)
                .stream().map(f -> f.getFrom().getUsername())
                .toList();
    }

    public void removeFriend(Long user1Id, String user2Username) {
        var user1 = usersRepository.findById(user1Id)
                .orElseThrow(() -> new NotFoundException("Id \"" + user1Id + "\" Not Found"));
        if (user1.getUsername() == user2Username)
            throw new BadRequestException("Can't be friend with yourself");
        var user2 = usersRepository.findByUsername(user2Username)
                .orElseThrow(() -> new NotFoundException("Username \"" + user2Username + "\" Not Found"));
        if (!user1.getFriends().contains(user2)) {
            var friendRequest = friendRequestsRepository
                    .findAllByFromIdAndToId(user1.getId(), user2.getId());
            var friendRequestTo = friendRequestsRepository
                    .findAllByFromIdAndToId(user2.getId(), user1.getId());
            if (!friendRequest.isEmpty()) {
                friendRequest.get(0).removeUsers();
                friendRequestsRepository.deleteById(friendRequest.get(0).getId());
                return;
            }
            if (!friendRequestTo.isEmpty()) {
                friendRequestTo.get(0).removeUsers();
                friendRequestsRepository.deleteById(friendRequestTo.get(0).getId());
                return;
            }
            throw new BadRequestException("You and " + user2Username + " aren't friends");

        }
        user1.getFriends().remove(user2);
        user2.getFriends().remove(user1);
        user1.getFriendsWith().remove(user2);
        user2.getFriendsWith().remove(user1);
    }

    public List<String> searchFollowers(Long userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Id \"" + userId + "\" Not Found"))
                .getFollowers().stream().map(User::getUsername).toList();
    }

    public List<String> searchFollowersByUsername(String username) {
        return searchFollowers(
                usersRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Username \"" + username + "\" Not Found"))
                .getId()
        );
    }

    public List<String> searchFollowing(Long userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Id \"" + userId + "\" Not Found"))
                .getFollowing().stream().map(User::getUsername).toList();
    }

    public List<String> searchFollowingByUsername(String username) {
        return searchFollowing(
                usersRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Username \"" + username + "\" Not Found"))
                .getId()
        );
    }

    public List<String> searchBlocked(Long userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Id \"" + userId + "\" Not Found"))
                .getBlockedUsers().stream().map(User::getUsername).toList();
    }

    public void followUser(Long userId, String followUsername) {
        var user = usersRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Id \"" + userId + "\" Not Found"));
        var follow = usersRepository.findByUsername(followUsername)
                .orElseThrow(() -> new NotFoundException("Username \"" + followUsername + "\" Not Found"));
        if (follow.getId() == userId)
            throw new BadRequestException("Can't follow yourself");
        if (user.getBlockedUsers().contains(follow))
            throw new BadRequestException("Can't follow a user you've blocked");
        if (user.getBlockedByOthers().contains(follow))
            throw new BadRequestException("Unable follow this user");
        if (user.getFollowing().contains(follow))
            throw new BadRequestException("You already followed this user");
        user.getFollowing().add(follow);
        follow.getFollowers().add(user);
        usersRepository.save(user);
        usersRepository.save(follow);
    }

    public void unfollowUser(Long userId, String followUsername) {
        var user = usersRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Id \"" + userId + "\" Not Found"));
        var follow = usersRepository.findByUsername(followUsername)
                .orElseThrow(() -> new NotFoundException("Username \"" + followUsername + "\" Not Found"));
        if (!user.getFollowing().contains(follow))
            throw new BadRequestException("You don't follow this user");
        user.getFollowing().remove(follow);
        follow.getFollowers().remove(user);
        usersRepository.save(user);
        usersRepository.save(follow);
    }

    public void blockUser(Long userId, String blockUsername) {
        var user = usersRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Id \"" + userId + "\" Not Found"));
        var block = usersRepository.findByUsername(blockUsername)
                .orElseThrow(() -> new NotFoundException("Username \"" + blockUsername + "\" Not Found"));
        if (block.getId() == userId)
            throw new BadRequestException("Can't block yourself");
        if (user.getBlockedUsers().contains(block))
            throw new BadRequestException("You've already blocked this user");
        user.getBlockedUsers().add(block);
        block.getBlockedByOthers().add(user);

        user.getFollowers().remove(block);
        user.getFollowing().remove(block);
        block.getFollowers().remove(user);
        block.getFollowing().remove(user);

        user.getFriends().remove(block);
        block.getFriends().remove(user);
        user.getFriendsWith().remove(block);
        block.getFriendsWith().remove(user);

        usersRepository.save(user);
        usersRepository.save(block);
    }

    public void unblockUser(Long userId, String blockUsername) {
        var user = usersRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Id \"" + userId + "\" Not Found"));
        var block = usersRepository.findByUsername(blockUsername)
                .orElseThrow(() -> new NotFoundException("Username \"" + blockUsername + "\" Not Found"));
        if (!user.getBlockedUsers().contains(block))
            throw new BadRequestException("You didn't block this user");
        user.getBlockedUsers().remove(block);
        block.getBlockedByOthers().remove(user);
        usersRepository.save(user);
        usersRepository.save(block);
    }

    public void delete(Long id){
        var user = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        user.getCommentsFromUser().forEach(c -> commentsService.deleteById(c.getId()));
        user.getCommentsToUser().forEach(c -> commentsService.deleteById(c.getId()));

        user.getFriends().forEach(f -> f.getFriendsWith().remove(user));
        user.getFriendsWith().forEach(f -> f.getFriends().remove(user));
        user.getFriends().clear();
        user.getFriendsWith().clear();

        user.getSports().forEach(Sport::removeUsers);

        friendRequestsRepository.findAllByFromId(id).forEach(f -> {
            f.removeUsers();
            friendRequestsRepository.deleteById(f.getId());
        });
        friendRequestsRepository.findAllByToId(id).forEach(f -> {
            f.removeUsers();
            friendRequestsRepository.deleteById(f.getId());
        });
        user.getOwnedTeams().forEach(t -> teamsService.deleteTeam(id, t.getName()));
        user.getAllTeams().forEach(t -> teamsService.exitTeam(id, t.getName()));

        user.getDiscussions().forEach(d -> discussionsService.deleteDiscussion(id, d.getId()));
        user.getReplies().forEach(r -> discussionsService.deleteReply(id, r.getId()));

        usersRepository.deleteById(id);
    }

    public UserResponse updateSports(Long id, Set<String> sports) {
        if (sports.isEmpty()) throw new BadRequestException("List can't be empty");
        var user = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        user.setSports(new HashSet<>());
        sports.forEach(s ->
                user.addSport(sportsRepository.findById(s)
                        .orElseThrow(() -> new BadRequestException("\"" + s + "\" is not a valid sport")))
        );
        return userToRes(usersRepository.save(user));
    }

    public void createAdmin() {
        if (!(usersRepository.existsByUsernameAndPassword("admin", "admin"))){
            Set<String> roles = new HashSet<>();
            roles.add("USER");
            roles.add("ADMIN");
            var admin = new User(
                    "admin",
                    "admin",
                    "admin",
                    "admin@gmail.com",
                    "12345678910",
                    "Curitiba",
                    25,
                    roles
            );
            usersRepository.save(admin);
        }
    }
}
