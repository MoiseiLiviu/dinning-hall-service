package com.restaurantapp.dinninghallservice.service;

import com.restaurantapp.dinninghallservice.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.restaurantapp.dinninghallservice.constants.enums.KitchenUrls.KITCHEN_SERVICE_URL;

@Service
@Slf4j
public class ExternalOrderService {

    private static final Map<Long, FinishedOrderExternal> finishedOrders = new HashMap<>();

    private static final RestTemplate restTemplate = new RestTemplate();

    private static final String FOOD_SERVICE_REGISTRATION_URL = "http://localhost:8081/register";

    private static final String CHECK_ORDER_URL = "http://localhost:8083/kitchen/order/";

    public ExternalOrderService() {
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(1L);
        restaurant.setAddress("http://localhost:8082");
        restaurant.setMenu(Table.menuItems);
        restaurant.setMenuItems(Table.menuItems.size());
        restaurant.setRating(OrderRatingService.avg);
        restaurant.setName("Restaurant 1");
//        restTemplate.postForEntity(FOOD_SERVICE_REGISTRATION_URL, restaurant, Void.class);
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
        FinishedOrderExternal finishedOrderExternal = finishedOrders.get(finishedOrder.getOrderId());
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

        ResponseEntity<Double> estimatedPrepTime = restTemplate.postForEntity(KITCHEN_SERVICE_URL, order, Double.class);

        finishedOrderExternal.setEstimatedWaitingTime(estimatedPrepTime.getBody());

        SubOrderResponse subOrderResponse = new SubOrderResponse();
        log.info("Suborder response : "+subOrderResponse+ " for order : "+order+" finishedOrdersMap : "+finishedOrders);
        subOrderResponse.setOrderId(order.getOrderId());
        subOrderResponse.setEstimatedWaitingTime(estimatedPrepTime.getBody());
        subOrderResponse.setRestaurantId(subOrderRequest.getRestaurantId());
        subOrderResponse.setRegisteredTime(registeredTime);
        subOrderResponse.setCreatedTime(subOrderRequest.getCreatedTime());

        return subOrderResponse;
    }

    private Double getEstimatedCookingTimeFromKitchen(Long orderId) {
        ResponseEntity<Double> response = restTemplate.getForEntity(CHECK_ORDER_URL + orderId, Double.class);

        return response.getBody();
    }
}
