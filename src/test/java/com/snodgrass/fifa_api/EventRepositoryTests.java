package com.snodgrass.fifa_api;

import com.snodgrass.fifa_api.model.Event;
import com.snodgrass.fifa_api.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
public class EventRepositoryTests {
    @Autowired
    private EventRepository eventRepository;

    @Test
    void shouldLoadEvents() {
        List<Event> events = eventRepository.findAll();
        assertFalse(events.isEmpty());
        System.out.println(events.getFirst().getMatchDate());
    }
}
