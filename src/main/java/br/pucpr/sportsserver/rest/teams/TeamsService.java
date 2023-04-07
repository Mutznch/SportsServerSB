package br.pucpr.sportsserver.rest.teams;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TeamsService {
    public List<String> search(Long userId, String username, String name, String sport) {
        return new ArrayList<>();
    }
}
