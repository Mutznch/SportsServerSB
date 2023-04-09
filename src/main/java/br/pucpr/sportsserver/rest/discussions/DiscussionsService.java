package br.pucpr.sportsserver.rest.discussions;

import br.pucpr.sportsserver.rest.sports.SportsRepository;
import br.pucpr.sportsserver.rest.users.UsersRepository;
import org.springframework.stereotype.Service;

@Service
public class DiscussionsService {
    private DiscussionsRepository discussionsRepository;
    private UsersRepository usersRepository;
    private SportsRepository sportsRepository;

    public DiscussionsService(
            DiscussionsRepository discussionsRepository,
            UsersRepository usersRepository,
            SportsRepository sportsRepository
    ) {
        this.discussionsRepository = discussionsRepository;
        this.usersRepository = usersRepository;
        this.sportsRepository = sportsRepository;
    }
}
