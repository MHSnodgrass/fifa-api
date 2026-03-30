package com.snodgrass.fifa_api.repository;

import com.snodgrass.fifa_api.model.Team;
import com.snodgrass.fifa_api.model.enums.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByGroupLetter(Group group);
}
