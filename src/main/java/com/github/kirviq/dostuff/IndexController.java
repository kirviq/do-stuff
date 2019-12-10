package com.github.kirviq.dostuff;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
	private final TypeRepository types;
	private final Environment env;

	@GetMapping("/")
	public String index(@RequestParam(required = false) Integer weeksPast, Model model) {
		LocalDate start = LocalDate.now().with(ChronoField.DAY_OF_WEEK, 1);
		if (weeksPast != null) {
			start = start.minus(weeksPast, ChronoUnit.WEEKS);
		}
		Map<LocalDate, Multimap<String, EventData>> eventsThisWeek = new HashMap<>();
		for (EventData event : events.findEventsByTimestampBetween(
				start.atStartOfDay().toInstant(EUROPE_BERLIN.getRules().getOffset(Instant.now())),
				start.plusWeeks(1).atStartOfDay().toInstant(EUROPE_BERLIN.getRules().getOffset(Instant.now())))) {
			Multimap<String, EventData> eventsAtThatDay = eventsThisWeek.computeIfAbsent(event.getTimestamp().atZone(EUROPE_BERLIN).toLocalDate(), day -> HashMultimap.create());
			eventsAtThatDay.put(event.getType().getName(), event);
		}
		model.addAttribute("week", new Week(start.get(ChronoField.ALIGNED_WEEK_OF_YEAR) + 1, start, start.plusDays(6)));
		model.addAttribute("days", IntStream.of(0, 1, 2, 3, 4, 5, 6)
				.mapToObj(start::plusDays)
				.map(day -> new Day(day, eventsThisWeek.computeIfAbsent(day, ignored -> HashMultimap.create()).asMap()))
				.toArray());
		model.addAttribute("types", types.findAll());
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
		LocalDate day;
		Map<String, Collection<EventData>> events;
	}

	@ModelAttribute("env")
	public Map<String, Object> getEnv() {
		return new Map<String, Object>() {
			@Override
			public int size() {
				throw new UnsupportedOperationException("nah");
			}

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public boolean containsKey(Object o) {
				return env.getProperty(String.valueOf(o)) != null;
			}

			@Override
			public boolean containsValue(Object o) {
				throw new UnsupportedOperationException("nah");
			}

			@Override
			public Object get(Object o) {
				return env.getRequiredProperty(String.valueOf(o));
			}

			@Override
			public Object put(String s, Object o) {
				throw new UnsupportedOperationException("nah");
			}

			@Override
			public Object remove(Object o) {
				throw new UnsupportedOperationException("nah");
			}

			@Override
			public void putAll(Map<? extends String, ?> map) {
				throw new UnsupportedOperationException("nah");
			}

			@Override
			public void clear() {
				throw new UnsupportedOperationException("nah");
			}

			@Override
			public Set<String> keySet() {
				throw new UnsupportedOperationException("nah");
			}

			@Override
			public Collection<Object> values() {
				throw new UnsupportedOperationException("nah");
			}

			@Override
			public Set<Entry<String, Object>> entrySet() {
				throw new UnsupportedOperationException("nah");
			}
		};
	}
}
