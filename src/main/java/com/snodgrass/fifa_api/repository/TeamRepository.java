package com.snodgrass.fifa_api.repository;

import com.snodgrass.fifa_api.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
