package com.phonecompany.billing.utils;

import com.phonecompany.billing.utilities.BillingUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/**
 * @author Dominik Kozubík
 */
class BillingUtilsTest {

  @Test
  void testParsePhoneLog_emptyString() {
    Assertions.assertEquals(0, BillingUtils.parsePhoneLog("").size());
  }

  @Test
  void testParsePhoneLog_validInput() {
    String phoneLogStr = String.join("\n",
            "420774577453,13-01-2025 10:00:00,13-01-2025 10:00:30",
            "420776562353,14-01-2025 09:00:00,14-01-2025 09:11:00"
    );

    var result = BillingUtils.parsePhoneLog(phoneLogStr);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(2, result.size());

    var first = result.get(0);
    Assertions.assertEquals("420774577453", first.getPhoneNumber());
    Assertions.assertEquals(13, first.getFrom().getDayOfMonth());
    Assertions.assertEquals(2025, first.getFrom().getYear());
    Assertions.assertEquals(10, first.getFrom().getHour());
    Assertions.assertEquals(0, first.getTo().getMinute());

    var second = result.get(1);
    Assertions.assertEquals("420776562353", second.getPhoneNumber());
    Assertions.assertEquals(14, second.getFrom().getDayOfMonth());
    Assertions.assertEquals(9, second.getFrom().getHour());
    Assertions.assertEquals(11, second.getTo().getMinute());
  }

  @Test
  void testComputeCallDurationMinutes_lessThanOneMinute() {
    var from = LocalDateTime.of(2025, 1, 13, 10, 0, 0);
    var to   = LocalDateTime.of(2025, 1, 13, 10, 0, 30);

    var duration = BillingUtils.computeCallDurationMinutes(from, to);
    Assertions.assertEquals(1, duration);
  }

  @Test
  void testComputeCallDurationMinutes_multipleMinutes() {
    var from = LocalDateTime.of(2025, 1, 13, 10, 0, 0);
    var to   = LocalDateTime.of(2025, 1, 13, 10, 10, 59);

    long duration = BillingUtils.computeCallDurationMinutes(from, to);
    Assertions.assertEquals(11, duration);
  }

  @Test
  void testIsDayRate_true() {
    LocalDateTime dateTime = LocalDateTime.of(2025, 1, 13, 10, 0, 0);
    boolean result = BillingUtils.isDayRate(dateTime, 8, 16);
    Assertions.assertTrue(result);
  }

  @Test
  void testIsDayRate_false() {
    LocalDateTime dateTime = LocalDateTime.of(2025, 1, 13, 18, 0, 0);
    boolean result = BillingUtils.isDayRate(dateTime, 8, 16);
    Assertions.assertFalse(result);
  }
}