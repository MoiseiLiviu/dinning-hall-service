package com.restaurantapp.dinninghallservice.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FinishedOrderExternal {

    @JsonAlias("order_id")
    private Long orderId;

    @JsonAlias("is_ready")
    private Boolean isReady = false;

    @JsonAlias("estimated_waiting_time")
    private Double estimatedWaitingTime;

    private Integer priority = 0;

    @JsonAlias("max_wait")
    private Double maximumWaitTime;

    @JsonAlias("created_time")
    private Long createdTime;

    @JsonAlias("registered_time")
    private Long registeredTime;

    @JsonAlias("prepared_time")
    private Long preparedTime;

    @JsonAlias("cooking_time")
    private Double cookingTime;

    @JsonAlias("cooking_details")
    private List<CookingDetails> cookingDetails;
}
