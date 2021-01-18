package com.github.kirviq.dostuff.db;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "health_data")
public class HealthData {
	@Id
	private LocalDate date;
	private BigDecimal weight;
	private int bloodsugar;
	@Column(name = "bp_systolic")
	private int bpSystolic;
	@Column(name = "bp_diastolic")
	private int bpDiastolic;
	private int pulse;
}
