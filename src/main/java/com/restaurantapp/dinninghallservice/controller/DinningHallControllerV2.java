package com.restaurantapp.dinninghallservice.controller;

import com.restaurantapp.dinninghallservice.model.*;
import com.restaurantapp.dinninghallservice.service.ExternalOrderService;
import com.restaurantapp.dinninghallservice.service.OrderRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2")
public class DinningHallControllerV2 {

    private final ExternalOrderService externalOrderService;

    @GetMapping("/order/{id}")
    public FinishedOrderExternal checkIfOrderIsReady(@PathVariable Long id){
        return externalOrderService.checkIfOrderIsReady(id);
    }

    @PostMapping("/order")
    public SubOrderResponse submitExternalOrder(@RequestBody SubOrderRequest subOrderRequest){
        return externalOrderService.submitExternalOrder(subOrderRequest);
    }

    @PostMapping("/rating")
    public SubOrderRatingResponse submitRating(@RequestBody SubOrderRatingRequest subOrderRatingRequest ){
        return OrderRatingService.getInstance().submitExternalRating(subOrderRatingRequest);
    }
}
