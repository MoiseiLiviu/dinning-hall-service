package com.restaurantapp.dinninghallservice.service;

import com.restaurantapp.dinninghallservice.model.FinishedOrder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderRatingService {

    private static final AtomicLong totalRating = new AtomicLong();
    private static final AtomicLong numberOfOrdersServed = new AtomicLong();

    public static void rateOrderBasedOnThePreparationTime(FinishedOrder finishedOrder){

        int rating;
        long prepTime = finishedOrder.getServingTime().toEpochMilli() - finishedOrder.getPickUpTime();
        double maxWaitTime = finishedOrder.getMaximumWaitTime() * 50L;
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
        double avg = totalRating.addAndGet(rating)/(double) numberOfOrdersServed.incrementAndGet();
        log.info("Average restaurant rating is :"+avg);
    }
}
