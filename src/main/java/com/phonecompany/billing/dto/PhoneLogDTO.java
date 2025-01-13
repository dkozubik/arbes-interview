package com.phonecompany.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * @author Dominik Kozubík
 */
@Getter
@AllArgsConstructor
public class PhoneLogDTO {

  private String phoneNumber;
  private LocalDateTime from;
  private LocalDateTime to;
}
