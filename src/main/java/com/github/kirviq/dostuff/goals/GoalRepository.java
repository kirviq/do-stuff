package com.github.kirviq.dostuff.goals;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface GoalRepository extends CrudRepository<Goal, String> {
	@Query("FROM Goal AS g WHERE (g.start IS NULL OR g.start <= :start) AND (g.end IS NULL OR g.end >= :end) ORDER BY g.order")
	List<Goal> findGoalsInRange(LocalDate start, LocalDate end);
}
