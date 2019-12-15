package com.github.kirviq.dostuff;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface HealthDataRepository extends CrudRepository<HealthData, LocalDate> {
	List<HealthData> findHealthDataByDateBetween(LocalDate from, LocalDate to);
}
