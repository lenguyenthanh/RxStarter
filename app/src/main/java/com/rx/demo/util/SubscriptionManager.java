package com.rx.demo.util;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Subscription;

/**
 * Created by Nakhimovich on 3/28/15.
 */
@Singleton
public class SubscriptionManager {
    private ArrayList<Subscription> subscriptions;

    @Inject
    public SubscriptionManager() {
        subscriptions = new ArrayList<>();
    }

    public void addSubscription(Subscription subscription) {
        subscriptions.add(subscription);
    }

    public void unsubscribeAll() {
        for (Subscription subscription : subscriptions) {
            if (subscription != null) {
                subscription.unsubscribe();
            }
        }
    }
}
