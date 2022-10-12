package com.restaurantapp.dinninghallservice.service;

import com.restaurantapp.dinninghallservice.model.FinishedOrder;
import com.restaurantapp.dinninghallservice.model.SubOrderRatingRequest;
import com.restaurantapp.dinninghallservice.model.SubOrderRatingResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.restaurantapp.dinninghallservice.DinningHallServiceApplication.TIME_UNIT;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderRatingService {

    private static final AtomicLong totalRating = new AtomicLong();
    private static final AtomicInteger numberOfOrdersServed = new AtomicInteger();

    public static double avg = 0;

    public static void rateOrderBasedOnThePreparationTime(FinishedOrder finishedOrder){

        rateOrderBasedOnThePreparationTime(finishedOrder.getMaximumWaitTime(), finishedOrder.getServingTime().toEpochMilli(), finishedOrder.getPickUpTime());
    }

    public static void rateOrderBasedOnThePreparationTime(double maxWaitTime, Long servingTime, Long pickUpTime){
        int rating;
        long prepTime = servingTime - pickUpTime;
        maxWaitTime = maxWaitTime * TIME_UNIT;
        if(prepTime < maxWaitTime){
            rating = 5;
        } else if (prepTime < maxWaitTime * 1.1){
            rating = 4;
        } else if(prepTime < maxWaitTime * 1.2){
            rating = 3;
        } else if (prepTime < maxWaitTime * 1.3){
            rating = 2;
        } else if(prepTime < maxWaitTime * 1.4){
            rating = 1;
        } else {
            rating = 0;
        }
        avg = totalRating.addAndGet(rating) / (double) numberOfOrdersServed.incrementAndGet();
        log.info("Average restaurant rating is :" + avg);
    }


    public static SubOrderRatingResponse submitExternalRating(SubOrderRatingRequest subOrderRatingRequest) {

        SubOrderRatingResponse subOrderRatingResponse = new SubOrderRatingResponse();
        subOrderRatingResponse.setRestaurantId(subOrderRatingRequest.getRestaurantId());
        subOrderRatingResponse.setRestaurantAvgRating(avg = totalRating.addAndGet(subOrderRatingRequest.getRating()) / (double) numberOfOrdersServed.incrementAndGet());
        subOrderRatingResponse.setPreparedOrders(numberOfOrdersServed.get());

        return subOrderRatingResponse;
    }
}
