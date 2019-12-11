package com.github.kirviq.dostuff;

import lombok.Data;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

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
}
