package br.pucpr.sportsserver.rest.sports;

import br.pucpr.sportsserver.lib.exception.BadRequestException;
import br.pucpr.sportsserver.lib.exception.NotFoundException;
import br.pucpr.sportsserver.rest.sports.response.SportResponse;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class SportsService {
    private SportsRepository sportsRepository;

    public SportsService(SportsRepository repository) {
        this.sportsRepository = repository;
    }

    public List<String> search(Long userId) {
        List<String> sports = new ArrayList<>();
        if (userId == null) {
        sportsRepository
                .findAll(Sort.by(Sort.Order.asc("name")))
                .forEach(s -> sports.add(s.getName()));
        } else sportsRepository
                .findAllByUsersId(userId)
                .forEach(s -> sports.add(s.getName()));
        return sports;
    }

    public SportResponse add(String name) {
        if (name == null)
            throw new BadRequestException("Sport cannot be null!");
        if (sportsRepository.existsById(name)) return new SportResponse(name);
        var sport = sportsRepository.save(new Sport(name));
        return new SportResponse(sport.getName());
    }

    public void delete(String name) {
        var sport = sportsRepository.findById(name)
                .orElseThrow(() -> new NotFoundException("Sport Not Found"));
        sport.clearSport();
        sportsRepository.save(sport);
        sportsRepository.deleteById(name);
    }

    public void createSports() {
        add("basketball");
        add("football");
        add("soccer");
        add("baseball");
        add("tennis");
        add("golf");
        add("ice hockey");
        add("volleyball");
        add("boxing");
        add("cricket");
        add("table tennis");
        add("bowling");
        add("archery");
        add("handball");
        add("softball");
        add("fencing");
        add("curling");
        add("running");
        add("cycling");
        add("swimming");
    }
}
