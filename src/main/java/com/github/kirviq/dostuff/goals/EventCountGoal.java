package com.github.kirviq.dostuff.goals;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.github.kirviq.dostuff.IndexController;
import com.github.kirviq.dostuff.events.EventData;
import com.github.kirviq.dostuff.events.EventType;

@Data
@Entity
@DiscriminatorValue("event_count")
@EqualsAndHashCode(callSuper = true)
public class EventCountGoal extends Goal {
	
	@ManyToMany
	@JoinTable(
			name = "event_count_goal_types",
			joinColumns = @JoinColumn(name = "goal_name"),
			inverseJoinColumns = @JoinColumn(name = "type_name")
	)
	@OrderBy("order")
	private List<EventType> types;
	
	@Column(name = "required_min_per_week")
	private Integer requiredMinPerWeek;
	@Column(name = "desired_min_per_week")
	private Integer desiredMinPerWeek;
	@Column(name = "desired_max_per_week")
	private Integer desiredMaxPerWeek;
	@Column(name = "required_max_per_week")
	private Integer requiredMaxPerWeek;
	
	
	@Override
	public String getIcon() {
		return types.get(0).getIcon();
	}
	
	@Override
	public WeekJudgement judge(Week week) {
		EventType primaryType = types.get(0);
		Set<String> typeNames = types.stream().map(EventType::getName).collect(Collectors.toSet());
		int count = (int) week.getDays().stream()
				.flatMap(day -> day.getEvents().values().stream().flatMap(Collection::stream).flatMap(o -> o instanceof EventData ? Stream.of(((EventData) o)) : ((IndexController.EventGroup) o).getParts().stream().map(IndexController.Part::getEvent)))
				.filter(e -> typeNames.contains(e.getType().getName()))
				.count();
		
		if (requiredMinPerWeek == null && desiredMinPerWeek == null && desiredMaxPerWeek == null && requiredMaxPerWeek == null) {
			return new WeekJudgement(this, WeekJudgement.Status.NONE, "");
		} else if (requiredMinPerWeek != null && count < requiredMinPerWeek) {
			return new WeekJudgement(this, WeekJudgement.Status.TROUBLE, count + " (<" + requiredMinPerWeek + ")");
		} else if (desiredMinPerWeek != null && count < desiredMinPerWeek) {
			return new WeekJudgement(this, WeekJudgement.Status.WARN, count + " (<" + desiredMinPerWeek + ")");
		} else if (requiredMaxPerWeek != null && count > requiredMaxPerWeek) {
			return new WeekJudgement(this, WeekJudgement.Status.TROUBLE, count + " (<" + requiredMaxPerWeek + ")");
		} else if (desiredMaxPerWeek != null && count > desiredMaxPerWeek) {
			return new WeekJudgement(this, WeekJudgement.Status.WARN, count + " (<" + requiredMaxPerWeek + ")");
		} else {
			return new WeekJudgement(this, WeekJudgement.Status.GOOD, String.valueOf(count));
		}
	}
}
