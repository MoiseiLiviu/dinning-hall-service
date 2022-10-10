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
            try {
                Thread.sleep(2 * 50L);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                Thread.currentThread().interrupt();
            }
            order.setPickUpTime(Instant.now().toEpochMilli());
            order.setWaiterId(this.id);
            ResponseEntity<Void> kitchenResponse = restTemplate.postForEntity(KITCHEN_SERVICE_URL, order, Void.class);
            if (kitchenResponse.getStatusCode().equals(HttpStatus.ACCEPTED)) {
                log.info(String.format("Order %s was sent successfully by waiter with id %d", order, id));
            } else {
                log.warn(String.format("Order %s couldn't be sent by waiter with id %d", order, id));
            }
            WaiterService.freeWaiters.add(this);
        });
    }

    public void markOrderAsFinished(FinishedOrder finishedOrder) {
        this.executorService.submit(() -> {
            Table table = WaiterService.getTableById(finishedOrder.getTableId());
            table.verifyIfOrderIsRight(finishedOrder);
            OrderRatingService.getInstance().rateOrderBasedOnThePreparationTime(finishedOrder);
            table.setCurrentState(TableState.FREE);
            table.submitOrder();
            WaiterService.freeWaiters.add(this);
        });
    }

    public Long getId() {
        return id;
    }
}
