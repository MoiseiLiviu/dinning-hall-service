package com.restaurantapp.dinninghallservice.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.restaurantapp.dinninghallservice.constants.enums.CookingApparatus;
import lombok.Getter;

@Getter
public class MenuItem {

    private Long id;

    private String name;

    @JsonAlias("preparation-time")
    private Integer preparationTime;

    private Integer complexity;

    @JsonAlias("cooking-apparatus")
    private CookingApparatus cookingApparatus;

}
