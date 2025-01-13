package com.phonecompany.billing;

import java.math.BigDecimal;

/**
 * @author Dominik Kozubík
 */
public interface TelephoneBillCalculator {

  /**
   * Method computes the billing cost based on the phone logs. See assignment.
   *
   * @param phoneLog phone log/s
   * @return         final billing cost
   */
  BigDecimal calculate(String phoneLog);
}
