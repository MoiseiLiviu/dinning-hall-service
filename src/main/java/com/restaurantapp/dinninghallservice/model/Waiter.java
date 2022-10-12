package com.restaurantapp.dinninghallservice.model;


import com.restaurantapp.dinninghallservice.constants.enums.TableState;
import com.restaurantapp.dinninghallservice.service.OrderRatingService;
import com.restaurantapp.dinninghallservice.service.WaiterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static com.restaurantapp.dinninghallservice.DinningHallServiceApplication.TIME_UNIT;
import static com.restaurantapp.dinninghallservice.constants.enums.KitchenUrls.KITCHEN_SERVICE_URL;

@Slf4j
public class Waiter {

    private final Long id;
    private static final AtomicLong idCounter = new AtomicLong();

    private final ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private static final RestTemplate restTemplate = new RestTemplate();


    public Waiter() {
        this.id = idCounter.incrementAndGet();
    }

    public void takeOrder(Order order) {
        this.executorService.submit(() -> {
            order.setPickUpTime(Instant.now().toEpochMilli());
            order.setWaiterId(this.id);
           restTemplate.postForEntity(KITCHEN_SERVICE_URL, order, Void.class);
           log.info(String.format("Order %s was sent successfully by waiter with id %d", order, id));
            WaiterService.freeWaiters.add(this);
        });
    }

    public void markOrderAsFinished(FinishedOrder finishedOrder) {
        this.executorService.submit(() -> {
            Table table = WaiterService.getTableById(finishedOrder.getTableId());
            table.verifyIfOrderIsRight(finishedOrder);
            OrderRatingService.rateOrderBasedOnThePreparationTime(finishedOrder);
            table.setCurrentState(TableState.FREE);
            table.submitOrder();
            WaiterService.freeWaiters.add(this);
        });
    }

    public Long getId() {
        return id;
    }
}
