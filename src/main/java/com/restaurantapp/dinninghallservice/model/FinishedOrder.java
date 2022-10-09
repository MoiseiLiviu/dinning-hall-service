package com.restaurantapp.dinninghallservice.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class FinishedOrder {

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

    @JsonAlias("cooking_time")
    private Double cookingTime;

    @JsonAlias("cooking_details")
    private List<CookingDetails> cookingDetails;

    @JsonIgnore
    private Instant servingTime;
}
