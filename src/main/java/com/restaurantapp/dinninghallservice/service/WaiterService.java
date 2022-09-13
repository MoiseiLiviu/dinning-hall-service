package com.restaurantapp.dinninghallservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurantapp.dinninghallservice.constants.enums.TableState;
import com.restaurantapp.dinninghallservice.model.FinishedOrder;
import com.restaurantapp.dinninghallservice.model.MenuItem;
import com.restaurantapp.dinninghallservice.model.Order;
import com.restaurantapp.dinninghallservice.model.Table;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
@Getter
public class WaiterService {

    public static final List<MenuItem> menuItems = initMenuItems();
    private static List<Table> tables;
    private static final Integer NUMBER_OF_TABLES = 10;
    private static final Integer NUMBER_OF_WAITERS = 4;
    private final Map<Long, ExecutorService> waiters = new HashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private Long waiterIdCounter = 1L;

    @Value("${kitchen.service.url}")
    private String kitchenServiceUrl;

    private final OrderRatingService orderRatingService;

    public WaiterService(OrderRatingService orderRatingService) {
        initWaiters();
        tables = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TABLES; i++) {
            tables.add(new Table());
        }
        this.orderRatingService = orderRatingService;
    }

    private void initWaiters() {
        for (int i = 0; i < NUMBER_OF_WAITERS; i++) {
            waiters.put(waiterIdCounter++, Executors.newFixedThreadPool(1));
        }
    }

    private void submitOrderToWaiters(Table table) {

        Map.Entry<Long, ExecutorService> executorServiceEntry = waiters
                .entrySet()
                .stream()
                .min(Comparator.comparing(es -> {
                    ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) es.getValue();
                    return threadPoolExecutor.getQueue().size();
                }))
                .orElseThrow();
        executorServiceEntry.getValue().execute(() -> takeOrder(table, executorServiceEntry.getKey()));
    }

    private static List<MenuItem> initMenuItems() {

        ObjectMapper mapper = new ObjectMapper();
        InputStream is = WaiterService.class.getResourceAsStream("/menu-items.json");
        try {
            return mapper.readValue(is, new TypeReference<List<MenuItem>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(fixedRate = 50)
    public void occupyRandomFreeTable() {

        Optional<Table> optionalFreeTable = tables.stream().filter(t -> t.getState().equals(TableState.FREE)).findAny();
        if (optionalFreeTable.isPresent()) {
            Table freeTable = optionalFreeTable.get();
            freeTable.setState(TableState.WAITING_TO_MAKE_AN_ORDER);
            submitOrderToWaiters(freeTable);
        }
    }

    private void takeOrder(Table table, Long waiterId) {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
        Order order = table.generateOrder();
        order.setWaiterId(waiterId);
        table.setLastOrder(order);
        ResponseEntity<Void> kitchenResponse = restTemplate.postForEntity(kitchenServiceUrl, order, Void.class);
        if (kitchenResponse.getStatusCode().equals(HttpStatus.ACCEPTED)) {
            log.info(String.format("Order %s was sent successfully by waiter with id %d", order, waiterId));
        } else {
            log.warn(String.format("Order %s couldn't be sent by waiter with id %d", order, waiterId));
        }
    }

    public void receiveFinishedOrder(FinishedOrder finishedOrder) {

        ExecutorService waiter = waiters.get(finishedOrder.getWaiterId());
        waiter.execute(()->markOrderAsFinished(finishedOrder));
    }

    public void markOrderAsFinished(FinishedOrder finishedOrder){

        Table table = getTableById(finishedOrder.getTableId());
        table.verifyIfOrderIsRight(finishedOrder);
        orderRatingService.rateOrderBasedOnThePreparationTime(finishedOrder);
        table.setState(TableState.FREE);
    }

    private Table getTableById(Long tableId) {

        return tables.stream().filter(t->t.getId().equals(tableId)).findFirst().orElseThrow();
    }
}
