package com.snodgrass.fifa_api.repository;

import com.snodgrass.fifa_api.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
