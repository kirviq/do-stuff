package com.github.kirviq.dostuff;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface EventTypeRepository extends CrudRepository<EventType, String> {
	@Query("SELECT t " +
			"FROM EventType t " +
			"WHERE t.requiredMaxPerWeek > -1 or t.requiredMinPerWeek > -1 or t.desiredMaxPerWeek > -1 or t.desiredMinPerWeek > -1 " +
			"ORDER BY t.order")
	List<EventType> getTypesWithGoals();
}
