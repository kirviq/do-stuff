package com.github.kirviq.dostuff;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.kirviq.dostuff.events.EventDataRepository;
import com.github.kirviq.dostuff.events.EventTypeRepository;
import com.github.kirviq.dostuff.goals.Goal;
import com.github.kirviq.dostuff.goals.GoalRepository;
import com.github.kirviq.dostuff.goals.Week;
import com.github.kirviq.dostuff.goals.WeekJudgement;
import com.github.kirviq.dostuff.healthData.HealthData;
import com.github.kirviq.dostuff.healthData.HealthDataRepository;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StatsController {
	private static final Pattern WEEK_PATTERN = Pattern.compile("(?<year>\\d{4})-W(?<week>\\d+)");
	private static final ZoneId GERMAN_TIMEZONE = ZoneId.of("Europe/Berlin");
	
	private final EventDataRepository events;
	private final EventTypeRepository eventTypes;
	private final HealthDataRepository healthData;
	private final GoalRepository goals;
	
	@GetMapping("/stats")
	private void stats(
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(required = false, defaultValue = "false") boolean flatten,
			Model model) {
		
		YearAndWeek startTime = getStartTime(from);
		YearAndWeek endTime = getEndTime(to);
		model.addAttribute("from", startTime);
		model.addAttribute("to", endTime);
		
		log.info("showing stats from {}, to {}", startTime, endTime);
		model.addAttribute("report", generateEventsReport(startTime, endTime));
		
		List<HealthData> healthValues = healthData.findHealthDataByDateBetween(startTime.getMonday(), endTime.getSunday());
		
		Consumer<List<Object[]>> flattener = flatten ? this::getAverage : ignored -> { /* do nothing */};
		model.addAttribute("weightData", getHealthData(healthValues, HealthData::getWeight, flattener));
		model.addAttribute("sugarData", getHealthData(healthValues, HealthData::getBloodsugar, flattener));
		model.addAttribute("bpSysData", getHealthData(healthValues, HealthData::getBpSystolic, flattener));
		model.addAttribute("bpDiaData", getHealthData(healthValues, HealthData::getBpDiastolic, flattener));
		model.addAttribute("pulseData", getHealthData(healthValues, HealthData::getPulse, flattener));
	}
	
	private void getAverage(List<Object[]> data) {
		for (int i = 0; i < data.size(); i++) {
			if (i < 2 || i > data.size() - 3) {
				continue;
			}
			Object[] current = data.get(i);
			current[1] = (getVal(data.get(i - 2)) + getVal(data.get(i - 1)) + getVal(current) + getVal(data.get(i + 1)) + getVal(data.get(i + 2))) / 5;
		}
	}
	private float getVal(Object[] point) {
		return ((Number) point[1]).floatValue();
	}
	
	private List<Object[]> getHealthData(List<HealthData> values, Function<HealthData, Number> extractor, Consumer<List<Object[]>> flattener) {
		List<Object[]> data = values.stream()
				.filter(value -> extractor.apply(value) != null && extractor.apply(value).floatValue() > 0)
				.map(value -> new Object[]{getMidday(value.getDate()), extractor.apply(value)})
				.collect(Collectors.toList());
		flattener.accept(data);
		return data.isEmpty() ? null : data;
	}
	
	private List<ReportRow> generateEventsReport(YearAndWeek startTime, YearAndWeek endTime) {
		Multimap<Goal, WeekJudgement> judgements = ArrayListMultimap.create();
		List<Week> weeks = getWeeks(startTime, endTime);
		for (Week week : weeks) {
			List<Goal> goalsThisWeek = goals.findGoalsInRange(week.getStart(), week.getEnd());
			goalsThisWeek.stream().map(g -> g.judge(week)).forEach(j -> judgements.put(j.getGoal(), j));
		}
		return judgements.keySet().stream()
				.sorted(Comparator.comparing(Goal::getOrder))
				.map(g -> {
					int failed = 0, almost = 0, succeeded = 0;
					for (WeekJudgement j : judgements.get(g)) {
						switch (j.getStatus()) {
							case GOOD:
								succeeded++;
								break;
							case WARN:
								almost++;
								break;
							case TROUBLE:
								failed++;
								break;
							default:
								// do nothing
						}
					}
					return new ReportRow(g, failed, almost, succeeded);
				})
				.collect(Collectors.toList());
	}
	
	private static final WeekFields WEEK_FIELDS = WeekFields.of(Locale.GERMANY);
	private List<Week> getWeeks(YearAndWeek startTime, YearAndWeek endTime) {
		List<Week> weeks = new ArrayList<>();
		LocalDate end = endTime.getSunday();
		for (LocalDate currentStart = startTime.getMonday(); currentStart.isBefore(end); currentStart = currentStart.plusWeeks(1)) {
			weeks.add(new Week(currentStart.get(WEEK_FIELDS.weekOfYear()), currentStart, currentStart.plusDays(6), Collections.emptyList()));
		}
		return weeks;
	}
	
	
	@Value
	public static class ReportRow {
		Goal goal;
		int failed;
		int almost;
		int succeded;
	}
	
	@RequiredArgsConstructor
	private static class YearAndWeek {
		private final int year;
		
		private final int week;
		private LocalDate getMonday() {
			return LocalDate.of(year, 2, 1)
					.with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
					.with(ChronoField.DAY_OF_WEEK, DayOfWeek.MONDAY.getValue());
//			return LocalDate.now()
//					.with(TemporalAdjusters.previous(DayOfWeek.MONDAY))
//					.withYear(year)
//					.with(ChronoField.ALIGNED_WEEK_OF_YEAR, week);
		}
		private LocalDate getSunday() {
			return LocalDate.of(year, 2, 1)
					.with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
					.with(ChronoField.DAY_OF_WEEK, DayOfWeek.SUNDAY.getValue());
//			return LocalDate.now()
//					.with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
//					.withYear(year)
//					.with(ChronoField.ALIGNED_WEEK_OF_YEAR, week);
		}
		
		@Override
		public String toString() {
			return year + "-W" + (week < 10 ? ("0" + week) : week);
		}
	}
	
	private static long getMidday(LocalDate date) {
		return date.atTime(LocalTime.of(12, 0)).atZone(GERMAN_TIMEZONE).toEpochSecond() * 1000;
	}
	
	private YearAndWeek getStartTime(String weekString) {
		if (weekString == null) {
			LocalDate start = LocalDate.now(GERMAN_TIMEZONE)
					.minus(4, ChronoUnit.WEEKS);
			return new YearAndWeek(start.getYear(), start.get(ChronoField.ALIGNED_WEEK_OF_YEAR));
		}
		return parseWeekString(weekString);
	}
	
	private YearAndWeek getEndTime(String weekString) {
		if (weekString == null) {
			LocalDate end = LocalDate.now(GERMAN_TIMEZONE);
			return new YearAndWeek(end.getYear(), end.get(ChronoField.ALIGNED_WEEK_OF_YEAR));
		}
		return parseWeekString(weekString);
	}
	
	private YearAndWeek parseWeekString(String weekString) {
		Matcher matcher = WEEK_PATTERN.matcher(weekString);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("unparsable week string: " + weekString + ", expected something like 2020-W25");
		}
		return new YearAndWeek(Integer.parseInt(matcher.group("year")), Integer.parseInt(matcher.group("week")));
	}

}

