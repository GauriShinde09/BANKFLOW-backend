package com.app.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
@Data
public class DepositeDto {
	@NotNull
	@Positive(message="Amount Should be greater then 0")
	private BigDecimal amount;

}
