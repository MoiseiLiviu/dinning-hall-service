package com.restaurantapp.dinninghallservice.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CookingDetails {

    @JsonAlias("food_id")
    private Long foodId;

    @JsonAlias("cook_id")
    private Long cookId;
}
