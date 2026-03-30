package com.snodgrass.fifa_api.repository;

import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.enums.Group;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
public class TeamRepositoryTests {
    @Autowired
    private TeamRepository teamRepository;

    @Test
    public void shouldLoadTeams() {
        List<Team> teams = teamRepository.findAll();
        assertThat(teams).isNotEmpty();
    }

    @Test
    void findByGroupLetter_returnsTeamsInGroup() {
        List<Team> teams = teamRepository.findByGroupLetter(Group.A);
        assertThat(teams).isNotEmpty();
        assertThat(teams).allMatch(t -> t.getGroupLetter() == Group.A);
    }
}
