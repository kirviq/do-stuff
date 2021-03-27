package com.github.kirviq.dostuff.goals;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class Week {
	final int number;
	final LocalDate start;
	final LocalDate end;
	final List<Day> days;
	List<WeekJudgement> judgements;
}
