package com.restaurantapp.dinninghallservice.service;

import com.restaurantapp.dinninghallservice.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class ExternalOrderService {

    private static final Map<Long, FinishedOrderExternal> finishedOrders = new HashMap<>();

    private static final RestTemplate restTemplate = new RestTemplate();

    private static final String FOOD_SERVICE_REGISTRATION_URL = "/register";

    private static final String CHECK_ORDER_URL = "/order/";

    @Value("${food-ordering-service.url}")
    private String foodOrderServiceUrl;

    @Value("${restaurant.address}")
    private String restaurantAdress;

    @Value("${restaurant.name}")
    private String restaurantName;

    @Value("${restaurant.id}")
    private Long restaurantId;

    @Value("${kitchen.service.url}")
    private String kitchenServiceUrl;

    @PostConstruct
    public void registerRestaurant(){
        if(foodOrderServiceUrl != null) {
            Restaurant restaurant = new Restaurant();
            restaurant.setRestaurantId(restaurantId);
            restaurant.setAddress(restaurantAdress);
            restaurant.setMenu(Table.menuItems);
            restaurant.setMenuItems(Table.menuItems.size());
            restaurant.setRating(OrderRatingService.avg);
            restaurant.setName(restaurantName);
            restTemplate.postForEntity(foodOrderServiceUrl + FOOD_SERVICE_REGISTRATION_URL, restaurant, Void.class);
        }
    }

    public FinishedOrderExternal checkIfOrderIsReady(Long id) {
        log.info("Requesting status for finished order with id : "+id);
        FinishedOrderExternal finishedOrderExternal = finishedOrders.get(id);
        if (finishedOrderExternal.getIsReady()) {
            log.info("Removing finished order with id : "+id);
            finishedOrders.remove(id);
        } else {
            finishedOrderExternal.setEstimatedWaitingTime(getEstimatedCookingTimeFromKitchen(id));
        }

        log.info("Returning order status : "+finishedOrderExternal);
        return finishedOrderExternal;
    }

    public void receiveExternalOrder(FinishedOrder finishedOrder) {
        log.info("Received external order : "+finishedOrder+" finishedOrdersMap : "+finishedOrders);
        FinishedOrderExternal finishedOrderExternal = new FinishedOrderExternal();
        finishedOrderExternal.setOrderId(finishedOrder.getOrderId());
        finishedOrders.putIfAbsent(finishedOrder.getOrderId(), finishedOrderExternal);
        finishedOrderExternal = finishedOrders.get(finishedOrder.getOrderId());
        finishedOrderExternal.setCookingTime(finishedOrder.getCookingTime());
        finishedOrderExternal.setPreparedTime(Instant.now().toEpochMilli());
        finishedOrderExternal.setCookingDetails(finishedOrder.getCookingDetails());
        finishedOrderExternal.setMaximumWaitTime(finishedOrder.getMaximumWaitTime());
        finishedOrderExternal.setEstimatedWaitingTime(null);
        finishedOrderExternal.setIsReady(true);
    }

    public SubOrderResponse submitExternalOrder(SubOrderRequest subOrderRequest) {


        Order order = new Order(subOrderRequest.getItems());
        order.setMaximumWaitTime(subOrderRequest.getMaximumWaitTime());
        order.setPickUpTime(Instant.now().toEpochMilli());
        log.info("Sending new order to kitchen : "+order);
        Long registeredTime = Instant.now().toEpochMilli();

        FinishedOrderExternal finishedOrderExternal = new FinishedOrderExternal();
        finishedOrderExternal.setOrderId(order.getOrderId());
        finishedOrderExternal.setRegisteredTime(registeredTime);
        finishedOrderExternal.setCreatedTime(subOrderRequest.getCreatedTime());
        finishedOrders.put(order.getOrderId(), finishedOrderExternal);

        ResponseEntity<Double> estimatedPrepTime = restTemplate.postForEntity(kitchenServiceUrl + "/order", order, Double.class);

        finishedOrderExternal.setEstimatedWaitingTime(estimatedPrepTime.getBody());

        SubOrderResponse subOrderResponse = new SubOrderResponse();
        subOrderResponse.setOrderId(order.getOrderId());
        subOrderResponse.setEstimatedWaitingTime(estimatedPrepTime.getBody());
        subOrderResponse.setRestaurantId(subOrderRequest.getRestaurantId());
        subOrderResponse.setRegisteredTime(registeredTime);
        subOrderResponse.setCreatedTime(subOrderRequest.getCreatedTime());

        log.info("Suborder response : "+subOrderResponse+ " for order : "+order+" finishedOrdersMap : "+finishedOrders);

        return subOrderResponse;
    }

    private Double getEstimatedCookingTimeFromKitchen(Long orderId) {
        ResponseEntity<Double> response = restTemplate.getForEntity(kitchenServiceUrl + CHECK_ORDER_URL + orderId, Double.class);

        return response.getBody();
    }
}
