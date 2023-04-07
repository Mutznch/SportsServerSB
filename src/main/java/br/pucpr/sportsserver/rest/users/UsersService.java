package br.pucpr.sportsserver.rest.users;

import br.pucpr.sportsserver.lib.exception.BadRequestException;
import br.pucpr.sportsserver.lib.exception.NotFoundException;
import br.pucpr.sportsserver.rest.comments.CommentsService;
import br.pucpr.sportsserver.rest.sports.SportsRepository;
import br.pucpr.sportsserver.rest.users.friends.Friend;
import br.pucpr.sportsserver.rest.users.friends.FriendRequest;
import br.pucpr.sportsserver.rest.users.friends.FriendRequestsRepository;
import br.pucpr.sportsserver.rest.users.friends.FriendsRepository;
import br.pucpr.sportsserver.rest.users.request.Login;
import br.pucpr.sportsserver.rest.users.request.UserRequest;
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
    private FriendsRepository friendsRepository;

    public UsersService(
            UsersRepository usersRepository,
            SportsRepository sportsRepository,
            CommentsService commentsService,
            FriendRequestsRepository friendRequestsRepository,
            FriendsRepository friendsRepository
    ) {
        this.usersRepository = usersRepository;
        this.sportsRepository = sportsRepository;
        this.commentsService = commentsService;
        this.friendRequestsRepository = friendRequestsRepository;
        this.friendsRepository = friendsRepository;
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
                user.getSports().stream().map(f -> f.getName()).collect(Collectors.toSet()),
                searchFriends(user.getId()),
                user.getFollowers().stream().map(f -> f.getUsername()).collect(Collectors.toSet()),
                user.getFollowing().stream().map(f -> f.getUsername()).collect(Collectors.toSet()),
                user.getAllTeams().stream().map(t -> t.getName()).collect(Collectors.toSet()),
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

    public UserResponse validateUser(Login credentials) {
        if (!(usersRepository.existsByUsernameAndPassword(credentials.getUsername(), credentials.getPassword())))
            throw new BadRequestException("Wrong Username or Password");
        return userToRes(
                usersRepository
                .findByUsername(credentials.getUsername())
                .orElseThrow(() -> new NotFoundException("User Not Found"))
        );
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

    public String requestFriend(User from, String toUsername) {
        if (from.getUsername() == toUsername)
            throw new BadRequestException("Can't send a friend request to yourself");
        var to = usersRepository.findByUsername(toUsername)
                .orElseThrow(() -> new NotFoundException("Username \"" + toUsername + "\" Not Found"));
        if (friendRequestsRepository.existsByFromIdAndToId(from.getId(), to.getId()))
            throw new BadRequestException("Friend Request already sent to that user");
        if (friendsRepository.existsByUserIdAndFriendId(from.getId(),to.getId()))
            throw new BadRequestException("You and " + toUsername + " are already friends");
        if (friendRequestsRepository.existsByFromIdAndToId(to.getId(), from.getId()))
            return addFriend(from, to);
        friendRequestsRepository.save(new FriendRequest(from, to));
        return "Friend request sent to " + toUsername;
    }

    public String addFriend(User user1, User user2) {
        friendsRepository.save(new Friend(user1, user2));
        friendsRepository.save(new Friend(user2, user1));

        friendRequestsRepository.deleteById(
                friendRequestsRepository
                        .findAllByFromIdAndToId(user2.getId(), user1.getId())
                        .get(0).getId()
        );
        return "You and " + user2 + " are now friends!";
    }

    public Set<String> searchFriends(Long userId) {
        return friendsRepository.findAllByUserId(userId)
                .stream().map(f -> f.getFriend().getUsername())
                .collect(Collectors.toSet());
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
                .stream().map(f -> f.getTo().getUsername())
                .toList();
    }

    public void removeFriend(User user1, String user2Username) {
        if (user1.getUsername() == user2Username)
            throw new BadRequestException("Can't be friend with yourself");
        var user2 = usersRepository.findByUsername(user2Username)
                .orElseThrow(() -> new NotFoundException("Username \"" + user2Username + "\" Not Found"));
        if (!friendsRepository.existsByUserIdAndFriendId(user1.getId(), user2.getId())) {
            List<FriendRequest> friendRequest = friendRequestsRepository
                    .findAllByFromIdAndToId(user1.getId(), user2.getId());
            if (friendRequest.isEmpty())
                throw new BadRequestException("You and " + user2Username + " aren't friends");
            friendRequestsRepository.deleteById(friendRequest.get(0).getId());
            return;
        }
        friendsRepository.findAllByUserIdAndFriendId(user1.getId(), user2.getId())
                .forEach(f -> friendsRepository.deleteById(f.getId()));
        friendsRepository.findAllByUserIdAndFriendId(user2.getId(), user1.getId())
                .forEach(f -> friendsRepository.deleteById(f.getId()));
    }

    public void delete(Long id){
        var user = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        try { commentsService.search(null, user.getUsername(), null)
                    .forEach(c -> commentsService.deleteById(c.getId()));
        } catch (NotFoundException e) {}
        try { commentsService.search(null, null, user.getUsername())
                    .forEach(c -> commentsService.deleteById(c.getId()));
        } catch (NotFoundException e) {}

        friendsRepository.findAllByUserId(id).forEach(f -> friendsRepository.deleteById(f.getId()));
        friendsRepository.findAllByFriendId(id).forEach(f -> friendsRepository.deleteById(f.getId()));
        friendRequestsRepository.findAllByFromId(id).forEach(f -> friendsRepository.deleteById(f.getId()));
        friendRequestsRepository.findAllByToId(id).forEach(f -> friendsRepository.deleteById(f.getId()));

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
