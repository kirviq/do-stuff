package com.github.kirviq.dostuff.db;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
@DiscriminatorValue("event_count")
public class EventCountGoal extends Goal{
	
	@ManyToMany
	@JoinTable(
			name = "event_count_goal_types",
			joinColumns = @JoinColumn(name = "event_name"),
			inverseJoinColumns = @JoinColumn(name = "type_name")
	)
	private Set<EventType> types;
	
	@Column(name = "required_min_per_week")
	private Integer requiredMinPerWeek;
	@Column(name = "desired_min_per_week")
	private Integer desiredMinPerWeek;
	@Column(name = "desired_max_per_week")
	private Integer desiredMaxPerWeek;
	@Column(name = "required_max_per_week")
	private Integer requiredMaxPerWeek;
}
