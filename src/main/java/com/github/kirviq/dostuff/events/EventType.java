package com.github.kirviq.dostuff.events;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "types")
public class EventType {
	@Id
	private String name;
	@ManyToOne
	@JoinColumn(name = "counts_as")
	private EventType countsAs;
	@ManyToOne
	private EventTypeGroup group;
	private String icon;
	@Column(name = "order_col")
	private int order;
	private String color;
	@Column(name = "background_color")
	private String backgroundColor;

	@Override
	public String toString() {
		return "EventType{" +
				"name='" + name + '\'' +
				", group=" + group.getName() +
				", icon='" + icon + '\'' +
				", color='" + color + '\'' +
				", backgroundColor='" + backgroundColor + '\'' +
				'}';
	}
}
