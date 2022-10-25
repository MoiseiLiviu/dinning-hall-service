package com.restaurantapp.dinninghallservice.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurantapp.dinninghallservice.constants.enums.TableState;
import com.restaurantapp.dinninghallservice.service.ExternalOrderService;
import com.restaurantapp.dinninghallservice.service.WaiterService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.restaurantapp.dinninghallservice.DinningHallServiceApplication.TIME_UNIT;


@Getter
@Setter
@Slf4j
public class Table {

    private Long tableId;
    private TableState currentState = TableState.FREE;
    private Order lastOrder;
    private Random random = new Random();

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static AtomicLong idCounter = new AtomicLong();

    public Table() {
        this.tableId = idCounter.incrementAndGet();
    }

    public void submitOrder(){

        executorService.submit(()->{
            try {
                Thread.sleep(4 * TIME_UNIT);
//                log.info("Sending order");

                this.currentState = TableState.WAITING_TO_MAKE_AN_ORDER;
                Order order = generateOrder();
                this.setLastOrder(order);
                Waiter waiter = WaiterService.freeWaiters.take();
                waiter.takeOrder(order);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        });
    }

    public Order generateOrder() {
        int numberOfMenuItems = random.nextInt(4) + 1;
        List<MenuItem> items = Stream
                .generate(() -> ExternalOrderService.menuItems.get(random.nextInt(ExternalOrderService.menuItems.size() - 1)))
                .limit(numberOfMenuItems)
                .collect(Collectors.toList());
        Double maxWaitTime = items.stream().mapToInt(MenuItem::getPreparationTime).max().orElse(0) * 1.3;
        List<Long> itemIds = items.stream().map(MenuItem::getId).collect(Collectors.toList());
        int priority = random.nextInt(2) + 1;
        return new Order(itemIds, priority, maxWaitTime, Instant.now().toEpochMilli(), this.tableId);
    }

    public void verifyIfOrderIsRight(FinishedOrder finishedOrder) {
        finishedOrder.setServingTime(Instant.now());
        if (!(Objects.equals(finishedOrder.getOrderId(), lastOrder.getOrderId())
                && finishedOrder.getItems().equals(lastOrder.getItems()))) {
            throw new RuntimeException("Table did not expect this order!");
        }
    }

}
