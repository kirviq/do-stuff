package com.github.kirviq.dostuff;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StatsController {
	private static final Pattern WEEK_PATTERN = Pattern.compile("(?<year>\\d{4})-W(?<week>\\d+)");
	private static final TemporalField WEEKDAYS = WeekFields.of(Locale.GERMANY).dayOfWeek();
	public static final ZoneId GERMAN_TIMEZONE = ZoneId.of("Europe/Berlin");
	
	private final EventDataRepository dataRepository;
	private final EventTypeRepository typeRepository;
	
	@GetMapping("/stats")
	private void stats(@RequestParam(required = false) String from, @RequestParam(required = false) String to, Model model) {
		YearAndWeek startTime = getStartTime(from);
		YearAndWeek endTime = getEndTime(to);
		model.addAttribute("from", startTime);
		model.addAttribute("to", endTime);
		long weeksToReport = ChronoUnit.WEEKS.between(startTime.getMonday(), endTime.getMonday()) + 1;
		
		log.info("showing stats from {}, to {}", startTime, endTime);
		List<EventType> types = typeRepository.getTypesWithGoals();
		List<EventDataRepository.EventCount> eventCounts = dataRepository.getEventCountsPerWeekInTimeRage(startTime.year, startTime.week, endTime.year, endTime.week, types.stream().map(EventType::getName).collect(Collectors.toSet()));
		log.info("found {} entries", eventCounts.size());
		
		Multimap<String, EventDataRepository.EventCount> countsByType = eventCounts.stream().collect(Multimaps.toMultimap(EventDataRepository.EventCount::getType, Function.identity(), HashMultimap::create));
		List<ReportRow> report = types.stream()
				.map(type -> {
					ReportRow row = new ReportRow();
					row.type = type;
					AtomicLong weeksMissing = new AtomicLong(weeksToReport);
					countsByType.get(type.getName()).forEach(count -> {
						weeksMissing.decrementAndGet();
						countCount(type, row, count.getCnt());
					});
					IntStream.range(0, weeksMissing.intValue()).forEach(ignored -> countCount(type, row, 0));
					return row;
				})
				.collect(Collectors.toList());
		model.addAttribute("report", report);
	}
	
	private static void countCount(EventType type, ReportRow row, int cnt) {
		if (type.getRequiredMinPerWeek() >= 0 && cnt < type.getRequiredMinPerWeek()) {
			row.failed++;
		} else if (type.getDesiredMinPerWeek() >= 0 && cnt < type.getDesiredMinPerWeek()) {
			row.almost++;
		} else if (type.getRequiredMaxPerWeek() >= 0 && cnt > type.getRequiredMaxPerWeek()) {
			row.failed++;
		} else if (type.getDesiredMaxPerWeek() >= 0 && cnt > type.getDesiredMaxPerWeek()) {
			row.almost++;
		} else {
			row.succeded++;
		}
	}
	
	@Getter
	public static class ReportRow {
		EventType type;
		int failed;
		int almost;
		int succeded;
	}
	
	@RequiredArgsConstructor
	private static class YearAndWeek {
		private final int year;
		private final int week;
		
		private LocalDate getMonday() {
			return LocalDate.now()
					.with(TemporalAdjusters.previous(DayOfWeek.MONDAY))
					.withYear(year)
					.with(ChronoField.ALIGNED_WEEK_OF_YEAR, week);
		}
		
		@Override
		public String toString() {
			return year + "-W" + (week < 10 ? ("0" + week) : week);
		}
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