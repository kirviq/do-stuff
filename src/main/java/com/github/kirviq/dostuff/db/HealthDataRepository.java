package com.github.kirviq.dostuff.db;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

import com.github.kirviq.dostuff.db.HealthData;

public interface HealthDataRepository extends CrudRepository<HealthData, LocalDate> {
	List<HealthData> findHealthDataByDateBetween(LocalDate from, LocalDate to);
}
