package com.restaurantapp.dinninghallservice.service;

import com.restaurantapp.dinninghallservice.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.restaurantapp.dinninghallservice.constants.enums.KitchenUrls.KITCHEN_SERVICE_URL;

@Service
public class ExternalOrderService {

    private static final Map<Long, FinishedOrderExternal> finishedOrders = new HashMap<>();

    private static final RestTemplate restTemplate = new RestTemplate();

    private static final String FOOD_SERVICE_REGISTRATION_URL = "localhost:8081/registration";

    private static final String CHECK_ORDER_URL = "kitchen-service:8083/order/";

    public ExternalOrderService() {
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(1L);
        restaurant.setAddress("localhost:8082");
        restaurant.setMenu(Table.menuItems);
        restaurant.setMenuItems(Table.menuItems.size());
        restaurant.setRating(OrderRatingService.avg);
        restaurant.setName("Restaurant 1");
//        restTemplate.postForEntity(FOOD_SERVICE_REGISTRATION_URL, restaurant, Void.class);
    }

    public FinishedOrderExternal checkIfOrderIsReady(Long id) {
        FinishedOrderExternal finishedOrderExternal = finishedOrders.get(id);
        if (finishedOrderExternal.getIsReady()) {
            finishedOrders.remove(id);
        } else {
            finishedOrderExternal.setEstimatedWaitingTime(getEstimatedCookingTimeFromKitchen(id));
        }

        return finishedOrderExternal;
    }

    public void receiveExternalOrder(FinishedOrder finishedOrder) {
        FinishedOrderExternal finishedOrderExternal = finishedOrders.get(finishedOrder.getOrderId());
        finishedOrderExternal.setCookingTime(finishedOrder.getCookingTime());
        finishedOrderExternal.setPreparedTime(Instant.now().toEpochMilli());
        finishedOrderExternal.setCookingDetails(finishedOrder.getCookingDetails());
        finishedOrderExternal.setIsReady(true);
    }

    public SubOrderResponse submitExternalOrder(SubOrderRequest subOrderRequest) {
        Order order = new Order(subOrderRequest.getItems());
        Long registeredTime = Instant.now().toEpochMilli();

        ResponseEntity<Double> estimatedPrepTime = restTemplate.postForEntity(KITCHEN_SERVICE_URL, order, Double.class);

        FinishedOrderExternal finishedOrderExternal = new FinishedOrderExternal();
        finishedOrderExternal.setOrderId(order.getOrderId());
        finishedOrderExternal.setRegisteredTime(registeredTime);
        finishedOrderExternal.setCreatedTime(subOrderRequest.getCreatedTime());
        finishedOrderExternal.setEstimatedWaitingTime(estimatedPrepTime.getBody());
        finishedOrders.put(order.getOrderId(), finishedOrderExternal);

        SubOrderResponse subOrderResponse = new SubOrderResponse();
        subOrderResponse.setOrderId(order.getOrderId());
        subOrderResponse.setEstimatedWaitingTime(estimatedPrepTime.getBody());
        subOrderResponse.setRestaurantId(subOrderResponse.getRestaurantId());
        subOrderResponse.setRegisteredTime(registeredTime);
        subOrderResponse.setCreatedTime(subOrderRequest.getCreatedTime());

        return subOrderResponse;
    }

    private Double getEstimatedCookingTimeFromKitchen(Long orderId) {
        ResponseEntity<Double> response = restTemplate.getForEntity(CHECK_ORDER_URL + orderId, Double.class);

        return response.getBody();
    }
}
