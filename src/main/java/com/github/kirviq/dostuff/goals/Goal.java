package com.github.kirviq.dostuff.goals;

import java.time.LocalDate;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "goals")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@EqualsAndHashCode(of = "name")
public abstract class Goal {
	@Id
	private String name;
	private LocalDate start;
	private LocalDate end;
	private int order;
	
	public abstract String getIcon();
	public abstract WeekJudgement judge(Week week);
}
