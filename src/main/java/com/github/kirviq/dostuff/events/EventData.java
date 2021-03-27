package com.github.kirviq.dostuff.events;

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
	private EventType type;
	private Instant timestamp;
}
