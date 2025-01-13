package com.phonecompany.billing.service;

import com.phonecompany.billing.TelephoneBillCalculator;
import com.phonecompany.billing.utilities.BillingUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Dominik Kozubík
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class TelephoneBillCalculatorImpl implements TelephoneBillCalculator {

  // could have also used @ConfigurationProperties(prefix = "billing") and inject the config component
  @Value("${billing.day-rate}")
  private BigDecimal dayRate;
  @Value("${billing.night-rate}")
  private BigDecimal nightRate;
  @Value("${billing.over-five-min-rate}")
  private BigDecimal overFiveMinRate;
  @Value("${billing.day-start-hour}")
  private int dayStartHour;
  @Value("${billing.day-end-hour}")
  private int dayEndHour;

  @Override
  public BigDecimal calculate(String phoneLog) {
    log.info("Starting calculation for phone log input.");

    var phoneLogDTOs = BillingUtils.parsePhoneLog(phoneLog);
    if (CollectionUtils.isEmpty(phoneLogDTOs)) {
      log.warn("Provided an empty input");
      return BigDecimal.ZERO;
    }

    log.debug("Parsed {} line(s) from input.", phoneLogDTOs.size());

    Map<String, Integer> callCountMap = new HashMap<>();
    Map<String, BigDecimal> costMap = new HashMap<>();

    for (var logDTO : phoneLogDTOs) {
      var phoneNumber = logDTO.getPhoneNumber();
      var callFrom = logDTO.getFrom();
      var callTo = logDTO.getTo();

      callCountMap.put(phoneNumber, callCountMap.getOrDefault(phoneNumber, 0) + 1);

      var durationMinutes = BillingUtils.computeCallDurationMinutes(callFrom, callTo);
      var callCost = calculateFinalCost(callFrom, durationMinutes);

      costMap.put(phoneNumber, costMap.getOrDefault(phoneNumber, BigDecimal.ZERO).add(callCost));

      log.debug(
              "Processed call for phoneNumber={} from={} to={} => durationMinutes={} => partialCost={}",
              phoneNumber, callFrom, callTo, durationMinutes, callCost
      );
    }

    var mostCalledOptional = getMostCalledPhoneNumber(callCountMap);
    log.info("Most-called (or tied highest) phoneNumber={} => setting its cost to 0 as part of promo.",
            mostCalledOptional.orElse("N/A"));
    mostCalledOptional.ifPresent(s -> costMap.put(s, BigDecimal.ZERO));

    var result = costMap.values()
            .stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    log.info("Final total cost for this phone log = {}", result);
    return result;
  }

  /**
   * The method calculates the final cost of one phone log.
   *
   * @param from            the log's from datetime
   * @param durationMinutes duration of the phone call
   * @return                computed cost for the given phone log based on start time, duration and billing rates
   */
  private BigDecimal calculateFinalCost(@NonNull LocalDateTime from, long durationMinutes) {
    if (durationMinutes <= 0) {
      return BigDecimal.ZERO;
    }

    var result = BigDecimal.ZERO;
    var minutesProcessed = 0;

    var firstMinutes = Math.min(durationMinutes, 5);

    // process first minutes
    for (int i = 0; i < firstMinutes; i++) {
      var currentProcessedTime = from.plusMinutes(i);
      result = result.add(
              BillingUtils.isDayRate(
                      currentProcessedTime, dayStartHour, dayEndHour
              )
                      ? dayRate
                      : nightRate
      );

      minutesProcessed++;
    }

    // process remaining minutes
    if (minutesProcessed < durationMinutes) {
      var remainingMinutes = durationMinutes - minutesProcessed;
      result = result.add(overFiveMinRate.multiply(BigDecimal.valueOf(remainingMinutes)));
    }

    return result;
  }

  /**
   * The method finds out the  most called phone number in the map. If there are two or more phone numbers with the
   * same count, the method chooses the one with the larger arithmetic value
   *
   * @param callCountMap map of phone numbers and their phone count
   * @return             phone number with the most calls, else optional empty
   */
  private Optional<String> getMostCalledPhoneNumber(@NonNull Map<String, Integer> callCountMap) {
    if (callCountMap.isEmpty()) {
      return Optional.empty();
    }

    // find the max phone count in the map
    var maxCallCount = callCountMap.values().stream()
            .max(Integer::compareTo)
            .orElse(0);

    // find phone numbers with the max phone count
    var maxCandidates = callCountMap.entrySet().stream()
            .filter(cm -> Objects.equals(maxCallCount, cm.getValue()))
            .map(Map.Entry::getKey)
            .toList();

    if (CollectionUtils.isEmpty(maxCandidates)) {
      return Optional.empty();
    }

    if (maxCandidates.size() == 1) {
      return Optional.of(maxCandidates.get(0));
    }

    // multiple phone numbers with the same max count
    return maxCandidates.stream()
            .max(Comparator.comparingLong(Long::parseLong));
  }

}
