package com.restaurantapp.dinninghallservice.service;

import com.restaurantapp.dinninghallservice.model.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@Slf4j
@Getter
public class WaiterService {

    private static final List<Table> tables = new ArrayList<>();
    private static final List<Waiter> waiters = new ArrayList<>();

    public static final BlockingQueue<Waiter> freeWaiters = new LinkedBlockingQueue<>();

    private static final Integer NUMBER_OF_TABLES = 10;
    private static final Integer NUMBER_OF_WAITERS = 4;

    public WaiterService() {
        initWaiters();
        initTables();
    }

    private void initWaiters() {
        for (int i = 0; i < NUMBER_OF_WAITERS; i++) {
            Waiter waiter = new Waiter();
            waiters.add(waiter);
            freeWaiters.add(waiter);
        }
    }

    private void initTables() {
        for (int i = 0; i < NUMBER_OF_TABLES; i++) {
            Table table = new Table();
            tables.add(table);
            table.submitOrder();
        }
    }

    public void receiveFinishedOrder(FinishedOrder finishedOrder) {
        Waiter waiter = getWaiterById(finishedOrder.getWaiterId());
        waiter.markOrderAsFinished(finishedOrder);
    }

    public static Table getTableById(Long tableId) {
        return tables.stream().filter(t -> t.getTableId().equals(tableId)).findAny().orElseThrow();
    }

    public static Waiter getWaiterById(Long waiterId){
        return waiters.stream().filter(w -> w.getId().equals(waiterId)).findAny().orElseThrow();
    }
}
