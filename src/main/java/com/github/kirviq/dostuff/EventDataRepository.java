package com.github.kirviq.dostuff;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface EventDataRepository extends CrudRepository<EventData, Long> {
	List<EventData> findEventsByTimestampBetweenOrderByIdAsc(Instant from, Instant to);
	@Query(nativeQuery = true, value =
			"SELECT " +
			"       MAX(YEAR(e.timestamp)) as year, " +
			"       MAX(WEEK(e.timestamp, 3)) as week, " +
			"       MAX(COALESCE(t.counts_as, e.type_name)) as type, " +
			"       COUNT(1) as cnt " +
			"FROM events e " +
			"JOIN types t on t.name = e.type_name " +
			"WHERE coalesce(t.counts_as, t.name) IN (:types) " +
			"  AND YEAR(e.timestamp) >= :startYear AND YEAR(e.timestamp) <= :endYear " +
			"  AND WEEK(e.timestamp, 3) >= :startWeek AND WEEK(e.timestamp, 3) <= :endWeek " +
			"GROUP BY coalesce(t.counts_as, t.name), YEAR(e.timestamp), WEEK(e.timestamp, 3)")
	List<EventCount> getEventCountsPerWeekInTimeRage(int startYear, int startWeek, int endYear, int endWeek, Set<String> types);
	
	interface EventCount {
		int getWeek();
		int getYear();
		String getType();
		int getCnt();
	}
}
