package com.github.kirviq.dostuff;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class IndexController {
	private static final ZoneId EUROPE_BERLIN = ZoneId.of("Europe/Berlin");
	private final EventDataRepository events;
	private final EventTypeRepository types;
	private final EventGroupRepository groups;
	private final Environment env;

	@GetMapping("/")
	public String index(@RequestParam(required = false) Integer weeksPast, Model model) {
		LocalDate start = LocalDate.now().with(ChronoField.DAY_OF_WEEK, 1);
		if (weeksPast != null) {
			start = start.minus(weeksPast, ChronoUnit.WEEKS);
		}
		Map<LocalDate, Multimap<String, EventData>> eventsThisWeek = new HashMap<>();
		Instant startOfWeek = start.atStartOfDay().toInstant(EUROPE_BERLIN.getRules().getOffset(Instant.now()));
		Instant endOfWeek = start.plusWeeks(1).atStartOfDay().toInstant(EUROPE_BERLIN.getRules().getOffset(Instant.now()));
		for (EventData event : events.findEventsByTimestampBetweenOrderByTimestampAsc(startOfWeek, endOfWeek)) {
			Multimap<String, EventData> eventsAtThatDay = eventsThisWeek.computeIfAbsent(event.getTimestamp().atZone(EUROPE_BERLIN).toLocalDate(), day -> LinkedHashMultimap.create());
			eventsAtThatDay.put(event.getType().getGroup().getName(), event);
		}
		model.addAttribute("week", new Week(start.get(ChronoField.ALIGNED_WEEK_OF_YEAR) + 1, start, start.plusDays(6)));
		model.addAttribute("days", IntStream.of(0, 1, 2, 3, 4, 5, 6)
				.mapToObj(start::plusDays)
				.map(date -> new Day(date, eventsThisWeek.computeIfAbsent(date, ignored -> HashMultimap.create()).asMap()))
				.toArray());
		model.addAttribute("groups", groups.findAll());
		return "index";
	}

	@PostMapping("/add-event")
	public String addEvent(@RequestParam String type, @RequestParam(name = "date", required = false) String date) {
		EventData event = new EventData();
		event.setTimestamp(Instant.now());
		event.setType(types.findById(type).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "type " + type + " not known")));
		Instant timestamp = Optional
				.ofNullable(Strings.emptyToNull(date))
				.map(string -> LocalDate.parse(string).atTime(23, 59, 59).atZone(EUROPE_BERLIN).toInstant())
				.orElse(Instant.now());
		event.setTimestamp(timestamp);
		events.save(event);
		return "redirect:/";
	}

	@PostMapping("/remove-event")
	public String addEvent(@RequestParam(name = "id") Long id) {
		EventData event = events.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "event " + id + " not found"));
		events.delete(event);
		return "redirect:/";
	}

	@Value
	private static class Week {
		int number;
		LocalDate start;
		LocalDate end;
	}
	@Value
	private static class Day {
		LocalDate date;
		Map<String, Collection<EventData>> events;
	}

}
