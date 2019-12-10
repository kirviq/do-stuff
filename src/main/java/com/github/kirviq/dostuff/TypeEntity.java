package com.github.kirviq.dostuff;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "types")
public class TypeEntity {
	@Id
	private String name;
	private String renderer;
	private String icon;
	private String color;
	@Column(name = "background_color")
	private String backgroundColor;
}
