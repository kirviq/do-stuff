package com.github.kirviq.dostuff;

import lombok.Data;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "type_groups")
@Data
public class TypeGroup {
	@Id
	private String name;

	private String icon;
	private String color;
	@Column(name = "background_color")
	private String backgroundColor;

	@OneToMany(fetch = FetchType.EAGER)
	@JoinColumn(name = "group_name")
	@OrderBy("order")
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
