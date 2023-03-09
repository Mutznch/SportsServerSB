package br.pucpr.sportsserver.rest.users;

import br.pucpr.sportsserver.lib.exception.BadRequestException;
import br.pucpr.sportsserver.lib.exception.NotFoundException;
import br.pucpr.sportsserver.rest.comments.CommentsService;
import br.pucpr.sportsserver.rest.sports.SportsRepository;
import br.pucpr.sportsserver.rest.users.request.Login;
import br.pucpr.sportsserver.rest.users.request.UserRequest;
import br.pucpr.sportsserver.rest.users.response.UserResponse;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UsersService {
    private UsersRepository usersRepository;
    private SportsRepository sportsRepository;
    private CommentsService commentsService;

    public UsersService(UsersRepository usersRepository, SportsRepository sportsRepository, CommentsService commentsService) {
        this.usersRepository = usersRepository;
        this.sportsRepository = sportsRepository;
        this.commentsService = commentsService;
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
        Set<String> sports = new HashSet<>();
        user.getSports().forEach(s -> sports.add(s.getName()));
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getCpf(),
                user.getCity(),
                user.getAge(),
                sports,
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

    public void delete(Long id){
        var user = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        try { commentsService.search(null, user.getUsername(), null)
                    .forEach(c -> commentsService.deleteById(c.getId()));
        } catch (NotFoundException e) {}
        try { commentsService.search(null, null, user.getUsername())
                    .forEach(c -> commentsService.deleteById(c.getId()));
        } catch (NotFoundException e) {}
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
