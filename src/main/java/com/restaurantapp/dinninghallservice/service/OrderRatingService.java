package com.restaurantapp.dinninghallservice.service;

import com.restaurantapp.dinninghallservice.model.FinishedOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class OrderRatingService {

    private AtomicLong totalRating = new AtomicLong();
    private AtomicLong numberOfOrdersServed = new AtomicLong();

    public void rateOrderBasedOnThePreparationTime(FinishedOrder finishedOrder){

        int rating;
        long prepTime = Instant.now().toEpochMilli() - finishedOrder.getPickUpTime();
        double maxWaitTime = finishedOrder.getMaximumWaitTime() * 1000;
        if(Instant.now().toEpochMilli() - finishedOrder.getPickUpTime() < finishedOrder.getMaximumWaitTime()){
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
