package com.github.kirviq.dostuff;

import lombok.Data;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "events")
@Data
public class EventData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "type_name")
	private TypeEntity type;
	@Column(name = "float_value_1")
	private double floatValue1;
	@Column(name = "float_value_2")
	private double floatValue2;
	@Column(name = "float_value_3")
	private double floatValue3;
	@Column(name = "float_value_4")
	private double floatValue4;
	@Column(name = "float_value_5")
	private double floatValue5;
	@Column(name = "string_value_1")
	private String stringValue1;
	private Instant timestamp;
}
