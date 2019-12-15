package com.github.kirviq.dostuff;

import com.google.common.base.Strings;
import com.google.common.collect.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class IndexController {
	private static final ZoneId EUROPE_BERLIN = ZoneId.of("Europe/Berlin");
	private static final WeekFields WEEK_FIELDS = WeekFields.of(Locale.GERMANY);

	private final EventDataRepository events;
	private final EventTypeRepository types;
	private final EventGroupRepository groups;
	private final HealthDataRepository healthData;

	@GetMapping("/")
	public String index(@RequestParam(required = false) Integer weeksPast, Model model) {
		LocalDate start = LocalDate.now().with(ChronoField.DAY_OF_WEEK, 1);
		if (weeksPast != null) {
			start = start.minus(weeksPast, ChronoUnit.WEEKS);
		}
		Map<LocalDate, Multimap<String, EventData>> eventsByDayAndType = new HashMap<>();
		Instant startOfWeek = start.atStartOfDay().toInstant(EUROPE_BERLIN.getRules().getOffset(Instant.now()));
		Instant endOfWeek = start.plusWeeks(1).atStartOfDay().toInstant(EUROPE_BERLIN.getRules().getOffset(Instant.now()));
		List<EventData> eventsThisWeek = events.findEventsByTimestampBetweenOrderByTimestampAsc(startOfWeek, endOfWeek);
		for (EventData event : eventsThisWeek) {
			Multimap<String, EventData> eventsAtThatDay = eventsByDayAndType.computeIfAbsent(event.getTimestamp().atZone(EUROPE_BERLIN).toLocalDate(), day -> LinkedHashMultimap.create());
			eventsAtThatDay.put(event.getType().getGroup().getName(), event);
		}
		List<EventGroup> eventGroups = Lists.newArrayList(this.groups.findAll());
		model.addAttribute("groups", eventGroups);

		HashMultimap<String, EventData> eventsThisWeekByType = eventsThisWeek.stream()
				.collect(Multimaps.toMultimap(event -> event.getType().getName(), Function.identity(), HashMultimap::create));

		List<EventStats> stats = eventGroups.stream()
				.flatMap(group -> group.getTypes().stream())
				.map(type -> new EventStats(
						type,
						Optional.ofNullable(eventsThisWeekByType.asMap().get(type.getName())).map(Collection::size).orElse(0)))
				.peek(this::setStatusAndWarning)
				.collect(Collectors.toList());

		model.addAttribute("week", new Week(
				start.get(WEEK_FIELDS.weekOfYear()),
				start, start.plusDays(6),
				stats));

		Map<LocalDate, HealthData> healthDataThisWeek = healthData.findHealthDataByDateBetween(start, start.plusDays(6)).stream()
				.collect(Collectors.toMap(HealthData::getDate, Function.identity()));
		model.addAttribute("days", IntStream.of(0, 1, 2, 3, 4, 5, 6)
				.mapToObj(start::plusDays)
				.map(date -> new Day(
						date,
						healthDataThisWeek.get(date),
						eventsByDayAndType.computeIfAbsent(date, ignored -> HashMultimap.create()).asMap()
				))
				.toArray());

		model.addAttribute("today", LocalDate.now());

		return "index";
	}

	private void setStatusAndWarning(EventStats stat) {
		if (stat.type.getRequiredMinPerWeek() < 0 && stat.type.getDesiredMinPerWeek() < 0 && stat.type.getDesiredMaxPerWeek() < 0 && stat.type.getRequiredMaxPerWeek() < 0) {
			stat.status = "NONE";
		} else if (stat.type.getRequiredMinPerWeek() >= 0 && stat.count < stat.type.getRequiredMinPerWeek()) {
			stat.status = "TROUBLE";
			stat.setWarning("(< " + stat.type.getRequiredMinPerWeek() + ")");
		} else if (stat.type.getDesiredMinPerWeek() >= 0&& stat.count < stat.type.getDesiredMinPerWeek()) {
			stat.status = "WARN";
			stat.setWarning("(< " + stat.type.getDesiredMinPerWeek() + ")");
		} else if (stat.type.getRequiredMaxPerWeek() >= 0 && stat.count > stat.type.getRequiredMaxPerWeek()) {
			stat.status = "TROUBLE";
			stat.setWarning("(> " + stat.type.getRequiredMaxPerWeek() + ")");
		} else if (stat.type.getDesiredMaxPerWeek() >= 0 && stat.count > stat.type.getDesiredMaxPerWeek()) {
			stat.status = "WARN";
			stat.setWarning("(> " + stat.type.getDesiredMaxPerWeek() + ")");
		} else {
			stat.status = "OK";
		}
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
		return getRedirect(event.getTimestamp().atZone(EUROPE_BERLIN).toLocalDate());
	}

	@PostMapping("/add-data")
	public String addData(@RequestParam String day, @RequestParam String weight, @RequestParam String sugar, @RequestParam String bpsys, @RequestParam String bpdia, @RequestParam String pulse) {
		HealthData data = new HealthData();
		data.setBloodsugar(asInt(sugar));
		data.setBpDiastolic(asInt(bpdia));
		data.setBpSystolic(asInt(bpsys));
		data.setDate(LocalDate.parse(day));
		data.setPulse(asInt(pulse));
		data.setWeight(Strings.isNullOrEmpty(weight) ? BigDecimal.ZERO : new BigDecimal(weight));
		healthData.save(data);
		return getRedirect(data.getDate());
	}

	private int asInt(String string) {
		if (Strings.isNullOrEmpty(string)) {
			return 0;
		}
		return Integer.parseInt(string);
	}

	@PostMapping("/remove-event")
	public String addEvent(@RequestParam(name = "id") Long id) {
		EventData event = events.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "event " + id + " not found"));
		events.delete(event);
		return getRedirect(event.getTimestamp().atZone(EUROPE_BERLIN).toLocalDate());
	}

	private static String getRedirect(LocalDate date) {
		int weeksPast = LocalDate.now().get(WEEK_FIELDS.weekOfYear()) - date.get(WEEK_FIELDS.weekOfYear());
		return "redirect:/" + (weeksPast == 0 ? "" : ("?weeksPast=" + weeksPast));
	}

	@Value
	private static class Week {
		int number;
		LocalDate start;
		LocalDate end;
		List<EventStats> stats;
	}
	@Data
	@RequiredArgsConstructor
	private static class EventStats {
		final EventType type;
		final int count;
		String status;
		String warning;
	}
	@Value
	private static class Day {
		LocalDate date;
		HealthData data;
		Map<String, Collection<EventData>> events;
	}

}
