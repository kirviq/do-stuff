package com.github.kirviq.dostuff;

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
	private TypeGroup group;
	private String icon;
	@Column(name = "order_col")
	private int order;
	private String color;
	@Column(name = "background_color")
	private String backgroundColor;

	@Column(name = "required_min_per_week")
	private int requiredMinPerWeek;
	@Column(name = "desired_min_per_week")
	private int desiredMinPerWeek;
	@Column(name = "desired_max_per_week")
	private int desiredMaxPerWeek;
	@Column(name = "required_max_per_week")
	private int requiredMaxPerWeek;

	@Override
	public String toString() {
		return "EventType{" +
				"name='" + name + '\'' +
				", group=" + group.getName() +
				", icon='" + icon + '\'' +
				", color='" + color + '\'' +
				", backgroundColor='" + backgroundColor + '\'' +
				", requiredMinPerWeek=" + requiredMinPerWeek +
				", desiredMinPerWeek=" + desiredMinPerWeek +
				", desiredMaxPerWeek=" + desiredMaxPerWeek +
				", requiredMaxPerWeek=" + requiredMaxPerWeek +
				'}';
	}
}
