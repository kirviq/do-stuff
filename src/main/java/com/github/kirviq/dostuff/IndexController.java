package com.github.kirviq.dostuff;

import com.github.kirviq.dostuff.events.EventData;
import com.github.kirviq.dostuff.events.EventDataRepository;
import com.github.kirviq.dostuff.events.EventGroupRepository;
import com.github.kirviq.dostuff.events.EventType;
import com.github.kirviq.dostuff.events.EventTypeRepository;
import com.github.kirviq.dostuff.goals.GoalRepository;
import com.github.kirviq.dostuff.healthData.HealthData;
import com.github.kirviq.dostuff.healthData.HealthDataRepository;
import com.github.kirviq.dostuff.events.EventTypeGroup;
import com.github.kirviq.dostuff.goals.Day;
import com.github.kirviq.dostuff.goals.Week;
import com.github.kirviq.dostuff.goals.WeekJudgement;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@Controller
@RequiredArgsConstructor
public class IndexController {
	private static final ZoneId EUROPE_BERLIN = ZoneId.of("Europe/Berlin");
	private static final WeekFields WEEK_FIELDS = WeekFields.of(Locale.GERMANY);

	private final EventDataRepository events;
	private final EventTypeRepository types;
	private final EventGroupRepository groups;
	private final GoalRepository goals;
	private final HealthDataRepository healthData;

	@Setter(onMethod = @__(@org.springframework.beans.factory.annotation.Value("${meals.max-minutes}")))
	private int mealMinutes;
	@Setter(onMethod = @__(@org.springframework.beans.factory.annotation.Value("${layout.button.size}")))
	private double buttonSize;

	@Data
	public class EventGroup {
		private Instant start;
		private Instant end;
		private final String icon;
		private final List<Part> parts;
		
		public EventGroup(EventData event) {
			this.icon = event.getType().getGroup().getGroupIcon();
			this.parts = new ArrayList<>();
			this.parts.add(new Part(event));
			end = start = event.getTimestamp();
		}
		
		public void add(EventData event) {
			parts.add(new Part(event));
			parts.sort(Comparator.comparing(p -> p.event.getType().getName()));
			for (int i = 0; i < parts.size(); i++) {
				Part part = parts.get(i);
				double angle = 2 * Math.PI / parts.size() * i;
				part.x = buttonSize / 4 * (1 + Math.cos(angle));
				part.y = buttonSize / 4 * (1 + Math.sin(angle));
			}
		}
	}

	@Data
	@RequiredArgsConstructor
	public static class Part {
		private double x;
		private double y;
		private final EventData event;
	}

	@GetMapping("/")
	public String index(@RequestParam(required = false) Integer weeksPast, Model model) {
		LocalDate start = LocalDate.now(EUROPE_BERLIN).with(ChronoField.DAY_OF_WEEK, 1);
		if (weeksPast != null) {
			start = start.minus(weeksPast, ChronoUnit.WEEKS);
		}
		Instant startOfWeek = start.atStartOfDay().toInstant(EUROPE_BERLIN.getRules().getOffset(Instant.now()));
		Instant endOfWeek = start.plusWeeks(1).atStartOfDay().toInstant(EUROPE_BERLIN.getRules().getOffset(Instant.now()));
		List<EventData> eventsThisWeek = events.findEventsByTimestampBetweenOrderByIdAsc(startOfWeek, endOfWeek);
		Map<LocalDate, Multimap<String, Object>> eventsByDayAndType = groupEventsByDay(eventsThisWeek);
		
		addGroups(model);
		
		List<Day> days = addDays(model, start, eventsByDayAndType);
		
		addWeek(model, days);
		
		addToday(model);
		
		return "index";
	}
	
	private void addToday(Model model) {
		model.addAttribute("today", LocalDate.now(EUROPE_BERLIN));
	}
	
	private void addWeek(Model model, List<Day> days) {
		LocalDate start = days.get(0).getDate();
		
		Week week = new Week(start.get(WEEK_FIELDS.weekOfYear()), start, start.plusDays(6), days);
		
		List<WeekJudgement> judgements = goals.findGoalsInRange(week.getStart(), week.getEnd()).stream()
				.map(g -> g.judge(week))
				.collect(Collectors.toList());
		week.setJudgements(judgements);
		
		model.addAttribute("week", week);
	}
	
	private List<Day> addDays(Model model, LocalDate start, Map<LocalDate, Multimap<String, Object>> eventsByDayAndType) {
		Map<LocalDate, HealthData> healthDataThisWeek = healthData.findHealthDataByDateBetween(start, start.plusDays(6)).stream()
				.collect(Collectors.toMap(HealthData::getDate, Function.identity()));
		List<Day> days = IntStream.of(0, 1, 2, 3, 4, 5, 6)
				.mapToObj(start::plusDays)
				.map(date -> new Day(
						date,
						healthDataThisWeek.get(date),
						eventsByDayAndType.computeIfAbsent(date, ignored -> HashMultimap.create()).asMap()
				))
				.collect(Collectors.toList());
		model.addAttribute("days", days);
		return days;
	}
	
	private void addGroups(Model model) {
		List<EventTypeGroup> eventGroups = StreamSupport.stream(types.findAll().spliterator(), false)
				.map(EventType::getGroup).distinct()
				.sorted(Comparator.comparingInt(group -> group.getTypes().get(0).getOrder()))
				.collect(Collectors.toList());
		model.addAttribute("groups", eventGroups);
	}
	
	private Map<LocalDate, Multimap<String, Object>> groupEventsByDay(List<EventData> eventsThisWeek) {
		Map<LocalDate, Multimap<String, Object>> eventsByDayAndType = new HashMap<>();
		for (EventData event : eventsThisWeek) {
			LocalDate localDateOfEvent = event.getTimestamp().atZone(EUROPE_BERLIN).toLocalDate();
			Multimap<String, Object> eventsAtThatDay = eventsByDayAndType.computeIfAbsent(localDateOfEvent, day -> LinkedHashMultimap.create());
			String group = event.getType().getGroup().getName();
			LocalDateTime minutesToMidnight = localDateOfEvent.atTime(22, 0, 0);
			if (minutesToMidnight.toInstant(EUROPE_BERLIN.getRules().getOffset(minutesToMidnight)).isAfter(event.getTimestamp())) {
				Collection<Object> todayOfThisGroup = eventsAtThatDay.get(group);
				if (!todayOfThisGroup.isEmpty()) {
					Object lastItemFromThisGroupToday = lastOf(todayOfThisGroup);
					if (lastItemFromThisGroupToday instanceof EventGroup) {
						EventGroup g = (EventGroup) lastItemFromThisGroupToday;
						if (g.end.plus(mealMinutes, ChronoUnit.MINUTES).isAfter(event.getTimestamp())) {
							g.add(event);
							g.end = event.getTimestamp();
							continue;
						}
					}
					if (lastItemFromThisGroupToday instanceof EventData) {
						EventData lastEvent = (EventData) lastItemFromThisGroupToday;
						if (lastEvent.getTimestamp().plus(mealMinutes, ChronoUnit.MINUTES).isAfter(event.getTimestamp())) {
							EventGroup eventGroup = new EventGroup(lastEvent);
							eventGroup.add(event);
							todayOfThisGroup.remove(lastEvent);
							todayOfThisGroup.add(eventGroup);
							continue;
						}
					}
				}
			}
			eventsAtThatDay.put(group, event);
		}
		return eventsByDayAndType;
	}
	
	private static <T> T lastOf(Iterable<T> items) {
		T last = null;
		for (T current : items) {
			last = current;
		}
		return last;
	}
	

	@PostMapping("/add-event")
	public String addEvent(@RequestParam String type, @RequestParam(name = "date") String dateString) {
		EventData event = new EventData();
		event.setTimestamp(Instant.now());
		event.setType(types.findById(type).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "type " + type + " not known")));
		LocalDate date = LocalDate.parse(dateString);
		boolean eventIsToday = LocalDate.now(EUROPE_BERLIN).equals(date);
		Instant timestamp = date.atTime(eventIsToday
				? LocalTime.now(EUROPE_BERLIN)
				: LocalTime.of(23, 59, 59, 999000)
		).atZone(EUROPE_BERLIN).toInstant();
		event.setTimestamp(timestamp);
		events.save(event);
		return getRedirect(event.getTimestamp().atZone(EUROPE_BERLIN).toLocalDate());
	}
	@PostMapping("/add-events")
	public String addEvents(@RequestParam(name = "type") List<String> types, @RequestParam(name = "date") String dateString) {
		types.forEach(type -> addEvent(type, dateString));
		return getRedirect(Instant.now().atZone(EUROPE_BERLIN).toLocalDate());
	}

	@PostMapping("/remove-event")
	public String removeEvent(@RequestParam(name = "id") Long id) {
		EventData event = events.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "event " + id + " not found"));
		events.delete(event);
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

	private static String getRedirect(LocalDate date) {
		int weeksPast = LocalDate.now(EUROPE_BERLIN).get(WEEK_FIELDS.weekOfYear()) - date.get(WEEK_FIELDS.weekOfYear());
		return "redirect:/" + (weeksPast == 0 ? "" : ("?weeksPast=" + weeksPast));
	}
	
}
