package com.github.kirviq.dostuff;

import lombok.Data;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "event_groups")
@Data
public class EventGroup {
	@Id
	private String name;

	private String icon;
	private String color;
	@Column(name = "background_color")
	private String backgroundColor;

	@OneToMany
	@JoinColumn(name = "group_name")
	private List<EventType> types;

	@Override
	public String toString() {
		return "EventGroup{" +
				"name='" + name + '\'' +
				", icon='" + icon + '\'' +
				", color='" + color + '\'' +
				", backgroundColor='" + backgroundColor + '\'' +
				", types=" + types.stream().map(EventType::getName).collect(Collectors.joining(", ")) +
				'}';
	}
}
