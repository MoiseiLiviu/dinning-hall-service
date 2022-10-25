package com.restaurantapp.dinninghallservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurantapp.dinninghallservice.model.FinishedOrder;
import com.restaurantapp.dinninghallservice.model.MenuItem;
import com.restaurantapp.dinninghallservice.model.Table;
import com.restaurantapp.dinninghallservice.model.Waiter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@Slf4j
@Getter
public class WaiterService {

    private static final List<Table> tables = new ArrayList<>();
    private static final List<Waiter> waiters = new ArrayList<>();

    public static final BlockingQueue<Waiter> freeWaiters = new LinkedBlockingQueue<>();
    public static List<MenuItem> menuItems;

    private static final Integer NUMBER_OF_TABLES = 6;
    private static final Integer NUMBER_OF_WAITERS = 3;

    private final ExternalOrderService externalOrderService;

    public static String KITCHEN_SERVICE_URL;

    @Value("${kitchen.service.url}")
    private void setKitchenServiceUrl(String value){
        KITCHEN_SERVICE_URL=value;
    };

    @Value("${restaurant.menu}")
    public String restaurantMenu;

    public WaiterService(ExternalOrderService externalOrderService) {
        initWaiters();
        this.externalOrderService = externalOrderService;
    }

    @PostConstruct
    public void readMenu(){
        initTables();
        this.externalOrderService = externalOrderService;
    }

    private void initMenuItems() {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = WaiterService.class.getResourceAsStream("/"+restaurantMenu);
        try {
            menuItems =  mapper.readValue(is, new TypeReference<List<MenuItem>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initWaiters() {
        for (int i = 0; i < NUMBER_OF_WAITERS; i++) {
            Waiter waiter = new Waiter();
            waiters.add(waiter);
            freeWaiters.add(waiter);
        }
    }

    private void initTables() {
        initMenuItems();
        for (int i = 0; i < NUMBER_OF_TABLES; i++) {
            Table table = new Table();
            tables.add(table);
            table.submitOrder();
        }
    }

    public void receiveFinishedOrder(FinishedOrder finishedOrder) {
        if (finishedOrder.getWaiterId() == null) {
            externalOrderService.receiveExternalOrder(finishedOrder);
        } else {
            Waiter waiter = getWaiterById(finishedOrder.getWaiterId());
            waiter.markOrderAsFinished(finishedOrder);
        }
    }

    public static Table getTableById(Long tableId) {
        return tables.stream().filter(t -> t.getTableId().equals(tableId)).findAny().orElseThrow();
    }

    public static Waiter getWaiterById(Long waiterId) {
        return waiters.stream().filter(w -> w.getId().equals(waiterId)).findAny().orElseThrow();
    }
}
