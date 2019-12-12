package com.github.kirviq.dostuff;

import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;

public interface EventDataRepository extends CrudRepository<EventData, Long> {
	List<EventData> findEventsByTimestampBetweenOrderByTimestampAsc(Instant from, Instant to);
}
