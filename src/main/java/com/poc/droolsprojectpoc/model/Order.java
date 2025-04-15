package com.poc.droolsprojectpoc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    private Long customerId;
    private BigDecimal totalAmount;
    private boolean eligible = false;
    private int discount = 0;
}

