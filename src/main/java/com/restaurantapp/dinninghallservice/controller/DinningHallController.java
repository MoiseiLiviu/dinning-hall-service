package com.restaurantapp.dinninghallservice.controller;

import com.restaurantapp.dinninghallservice.model.FinishedOrder;
import com.restaurantapp.dinninghallservice.service.WaiterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/distribution")
@RequiredArgsConstructor
@Slf4j
public class DinningHallController {

    private final WaiterService waiterService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void receiveFinishedOrder(@RequestBody FinishedOrder finishedOrder){
        log.info("Received "+finishedOrder.toString());
        waiterService.receiveFinishedOrder(finishedOrder);
    }
}
