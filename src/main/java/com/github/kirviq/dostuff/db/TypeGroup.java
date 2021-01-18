package com.github.kirviq.dostuff.db;

import lombok.Data;

import javax.persistence.*;

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
	private boolean groupable;
	@Column(name = "group_icon")
	private String groupIcon;

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
