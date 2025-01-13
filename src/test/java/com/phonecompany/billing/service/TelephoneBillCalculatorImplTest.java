package com.phonecompany.billing.service;

import com.phonecompany.billing.TelephoneBillCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;


/**
 * @author Dominik Kozubík
 */
@SpringBootTest
@ActiveProfiles(value = "test")
class TelephoneBillCalculatorImplTest {

  @Autowired
  private TelephoneBillCalculator calculator;

  @Test
  void testNoCalls() {
    String phoneLog = "";
    BigDecimal result = calculator.calculate(phoneLog);

    Assertions.assertEquals(0, result.compareTo(BigDecimal.ZERO));
  }

  @Test
  void testShortSingleDayRate() {
    String phoneLog = "42077457744,13-01-2025 12:00:00,13-01-2025 12:00:30";

    var result = calculator.calculate(phoneLog);

    // because max count, should be for free
    Assertions.assertEquals(BigDecimal.ZERO, result);
  }

  @Test
  void testMultipleCallsProvidedExample() {
    String phoneLog = String.join("\n",
            "420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57",
            "420776562353,18-01-2020 08:59:20,18-01-2020 09:10:00"
    );

    var result = calculator.calculate(phoneLog);

    // both have the same count frequency but second one is numerically larger
    Assertions.assertEquals(BigDecimal.valueOf(1.5), result);
  }

  @Test
  void testMultipleCallsPromo() {
    String phoneLog = String.join("\n",
            "420774577453,13-01-2025 18:10:15,13-01-2025 18:12:57",  // ~3 min night => 1.5
            "420776562353,14-01-2025 09:00:00,14-01-2025 09:11:00",  // 11 min day => 6.2
            "420776562353,14-01-2025 12:00:00,14-01-2025 12:04:59"   // ~5 min day => 5.0
    );

    BigDecimal result = calculator.calculate(phoneLog);

    // 420776562353 is the most frequent
    Assertions.assertEquals(BigDecimal.valueOf(1.5), result);
  }
}
