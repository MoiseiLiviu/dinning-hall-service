package com.restaurantapp.dinninghallservice.model;

import com.restaurantapp.dinninghallservice.constants.enums.TableState;
import com.restaurantapp.dinninghallservice.service.WaiterService;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class Table {

    private Long id;
    private TableState state = TableState.FREE;
    private Order lastOrder;
    private  Random random = new Random();

    private static AtomicLong idCounter = new AtomicLong();

    public Table(){
        this.id = idCounter.incrementAndGet();
    }

    public Order generateOrder() {

        int numberOfMenuItems = random.nextInt(6) + 1;
        List<MenuItem> items = Stream
                .generate(() -> WaiterService.menuItems.get(random.nextInt(WaiterService.menuItems.size() - 1)))
                .limit(numberOfMenuItems)
                .collect(Collectors.toList());
        Double maxWaitTime = items.stream().mapToInt(MenuItem::getPreparationTime).max().orElse(0) * 1.3;
        List<Long> itemIds = items.stream().map(MenuItem::getId).collect(Collectors.toList());
        int priority = random.nextInt(4) + 1;
        return new Order(itemIds, priority, maxWaitTime, Instant.now().toEpochMilli(), this.id);
    }

    public void verifyIfOrderIsRight(FinishedOrder finishedOrder) {

        if(!(Objects.equals(finishedOrder.getOrderId(), lastOrder.getOrderId())
        && finishedOrder.getItems().equals(lastOrder.getItems()))){
            throw new RuntimeException("Table did not expect this order!");
        }
    }
}
