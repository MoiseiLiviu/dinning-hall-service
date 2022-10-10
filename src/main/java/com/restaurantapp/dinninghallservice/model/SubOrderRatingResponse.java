package com.restaurantapp.dinninghallservice.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubOrderRatingResponse {

    @JsonAlias("restaurant_id")
    private Long restaurantId;

    @JsonAlias("prepared_orders")
    private Integer preparedOrders;

    @JsonAlias("restaurant_avg_rating")
    private Double restaurantAvgRating;
}
