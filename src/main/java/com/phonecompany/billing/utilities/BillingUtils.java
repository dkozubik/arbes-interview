package com.phonecompany.billing.utilities;

import com.phonecompany.billing.dto.PhoneLogDTO;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * @author Dominik Kozubík
 */
@UtilityClass
public class BillingUtils {

  public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

  /**
   * The method parses the input of csv-like format with lines consisting of 'phoneNumber, datetimeFrom, datetimeTo' to
   * list of PhoneLogDTO corresponding to the input values.
   * The method assumes that the String input is in the correct form!
   *
   * @param phoneLogStr call log input of the csv format with 'phoneNumber,datetimeFrom,datetimeTo' as a line
   * @return            Optional with CallLogDTO if parse was successful, optional empty otherwise
   */
  public static List<PhoneLogDTO> parsePhoneLog(@NonNull String phoneLogStr) {
    if (!StringUtils.hasLength(phoneLogStr)) {
      return Collections.emptyList();
    }

    return phoneLogStr.lines()
            .map(l -> {
              var splitted = l.split(",");
              return new PhoneLogDTO(
                      splitted[0], // phone number
                      LocalDateTime.parse(splitted[1], DATE_TIME_FORMATTER), // datetime from
                      LocalDateTime.parse(splitted[2], DATE_TIME_FORMATTER) // datetime to
              );
            })
            .toList();
  }


  /**
   * The method calculates the duration of two LocalDateTimes in minutes, where the second one should be after the
   * first. Every started minute counts towards the result!
   *
   * @param from datetime from
   * @param to   datetime to
   * @return     long representing the  difference in minutes
   */
  public static long computeCallDurationMinutes(@NonNull LocalDateTime from, @NonNull LocalDateTime to) {
    var duration = Duration.between(from, to);
    var seconds = duration.getSeconds();

    var result = seconds / 60;
    return (seconds % 60 > 0) ? (result + 1) : result; // every minute that begins counts in the result
  }


  /**
   * The method checks whether the input datetime is inside a day rate.
   *
   * @param dateTime  the input datetime to check
   * @param startHour start hour when day rate begins
   * @param endHour   end hour when date rate ends
   * @return          true if input datetime is in a day rate range, false otherwise
   */
  public static boolean isDayRate(@NonNull LocalDateTime dateTime, int startHour, int endHour) {
    var hour = dateTime.getHour();
    return hour >= startHour && hour < endHour;
  }

}
