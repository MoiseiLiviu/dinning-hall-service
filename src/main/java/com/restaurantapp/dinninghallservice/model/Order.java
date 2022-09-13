package com.restaurantapp.dinninghallservice.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Order {

    @JsonAlias("order_id")
    private Long orderId;

    private List<Long> items = new ArrayList<>();

    private Integer priority;

    @JsonAlias("max_wait")
    private Double maximumWaitTime;

    @JsonAlias("pick_up_time")
    private Long pickUpTime;

    @JsonAlias("waiter_id")
    private Long waiterId;

    @JsonAlias("table_id")
    private Long tableId;

    private static AtomicLong idCounter = new AtomicLong();

    public Order(List<Long> items, Integer priority, Double maximumWaitTime, Long pickUpTime, Long tableId) {
        this.orderId = idCounter.incrementAndGet();
        this.items = items;
        this.priority = priority;
        this.maximumWaitTime = maximumWaitTime;
        this.pickUpTime = pickUpTime;
        this.tableId = tableId;
    }
}
