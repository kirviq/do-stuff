package com.github.kirviq.dostuff.goals;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import com.github.kirviq.dostuff.events.EventType;

@Value
@RequiredArgsConstructor
public class WeekJudgement {
	public enum Status {
		NONE, GOOD, WARN, TROUBLE
	}
	
	Goal goal;
	Status status;
	String message;
}
