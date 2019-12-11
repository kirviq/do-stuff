package com.github.kirviq.dostuff;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "types")
public class EventType {
	@Id
	private String name;
	@ManyToOne
	private EventGroup group;
	private String icon;
	private String color;
	@Column(name = "background_color")
	private String backgroundColor;
}
