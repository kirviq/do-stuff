package com.github.kirviq.dostuff.goals;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

import lombok.Value;

import com.github.kirviq.dostuff.healthData.HealthData;

@Value
public class Day {
	LocalDate date;
	HealthData data;
	Map<String, Collection<Object>> events;
}
