package com.rx.demo.ui;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.digitalbuddha.daggerdemo.activitygraphs.R;
import com.rx.demo.dagger.Activity;
import com.rx.demo.dagger.DemoBaseActivity;
import com.rx.demo.model.User;
import com.rx.demo.rest.Github;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Random;

import javax.inject.Inject;

import rx.Observable;
import rx.android.events.OnClickEvent;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import static rx.android.observables.ViewObservable.clicks;


public class MainActivity extends DemoBaseActivity {
    @Inject
    public Github api;
    @Activity
    @Inject
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Slides on consuming observables, actions and observers
        getResponse();

        setupClickStreams();

        subscribeWithAllObservers(getRefreshObservable());

        subscribeWithAllObservers(getUserObservable());
    }

    private void setupClickStreams() {
        //slide everything is a stream including click events show double click buffering as well
        //slide difference map vs flatmap
        clicks(view(R.id.close1))
                .flatMap(getResponse())
                .map(this::getRandomUser)
                .subscribe(this::updateFirstUser);

        clicks(view(R.id.close2))
                .flatMap(getResponse())
                .map(this::getRandomUser)
                .subscribe(this::updateSecondUser);

        clicks(view(R.id.close3))
                .flatMap(getResponse())
                .map(this::getRandomUser)
                .subscribe(this::updateThirdUser);
    }

    private void subscribeWithAllObservers(Observable<ArrayList<User>> observable) {
        ConnectableObservable<ArrayList<User>> connectableObservable = observable.publish();
        //Slide on doOnNext, onError etc.
        setupErrorHandling(connectableObservable);

        //get a single random user from the response and then have each of
        //the three screen elements subscribe to it thus updating the screens with new data
        connectableObservable.map(this::getRandomUser).subscribe(this::updateFirstUser);
        connectableObservable.map(this::getRandomUser).subscribe(this::updateSecondUser);
        connectableObservable.map(this::getRandomUser).subscribe(this::updateThirdUser);
        connectableObservable.connect();
    }

    private Observable<ArrayList<User>> getUserObservable() {
        //slide on creating observables, then show how retrofit can do it for you
        return api.users().cache()  //slides on cache and other aggregates
                .observeOn(AndroidSchedulers.mainThread())  //slide on schedulers/threading
                .subscribeOn(Schedulers.io());  //metion immutibility
    }

    private Func1<OnClickEvent, Observable<ArrayList<User>>> getResponse() {
        //slide on functions, mapping specifically
        return onClickEvent -> getUserObservable();
    }

    private Observable<ArrayList<User>> getRefreshObservable() {
        return clicks(findViewById(R.id.btnRefresh))
                .flatMap(getResponse());
    }

    private User getRandomUser(ArrayList<User> users) {
        return users.get(getRandomIndex(users.size()));
    }

    private Observable<ArrayList<User>> setupErrorHandling(ConnectableObservable<ArrayList<User>> connectableObservable) {
        return connectableObservable.doOnError(throwable -> {
            //lets handle all errors the same way by displaying some message
        });
    }

    private void updateThirdUser(User user) {
        bindData(R.id.name3, R.id.avatar3, user);
    }

    private void updateSecondUser(User user) {
        bindData(R.id.name2, R.id.avatar2, user);
    }

    private void updateFirstUser(User user) {
        bindData(R.id.name1, R.id.avatar1, user);
    }

    private void bindData(int textviewID, int imageViewId, User user) {
        ((TextView) view(textviewID)).setText(user.login);
        Picasso.with(context).load(user.avatar_url).into((ImageView) view(imageViewId));
    }

    public static int getRandomIndex(int size) {
        return new Random().nextInt(size);
    }
}
