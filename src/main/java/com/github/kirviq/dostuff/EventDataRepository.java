package com.github.kirviq.dostuff;

import org.springframework.data.repository.CrudRepository;

import java.time.Instant;

public interface EventDataRepository extends CrudRepository<EventData, Long> {
	Iterable<EventData> findEventsByTimestampBetweenOrderByTimestampAsc(Instant from, Instant to);
}
