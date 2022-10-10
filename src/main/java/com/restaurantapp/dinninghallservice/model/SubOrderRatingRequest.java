package com.restaurantapp.dinninghallservice.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubOrderRatingRequest {

    @JsonAlias("restaurant_id")
    private Long restaurantId;

    @JsonAlias("order_id")
    private Long orderId;

    private Integer rating;

    @JsonAlias("estimated_waiting_time")
    private Double estimatedWaitingTime;

    @JsonAlias("waiting_time")
    private Double waitingTime;
}
