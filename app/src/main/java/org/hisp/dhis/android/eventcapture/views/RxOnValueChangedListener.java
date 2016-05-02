package org.hisp.dhis.android.eventcapture.views;

import android.os.Looper;

import org.hisp.dhis.client.sdk.ui.models.OnValueChangeListener;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

public class RxOnValueChangedListener<T>
        implements Observable.OnSubscribe<T>, OnValueChangeListener<T> {
    private OnValueChangeListener<T> onValueChangeListener;

    public RxOnValueChangedListener() {
        // explicit empty constructor
    }

    @Override
    public void call(final Subscriber<? super T> subscriber) {
        // we need to make sure, that
        // subscriber is called on min thread only
        checkIfOnMainThread();

        onValueChangeListener = new OnValueChangeListener<T>() {

            @Override
            public void onValueChanged(String id, T keyValue) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(keyValue);
                }
            }
        };

        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {
                // removing reference to listener
                // in order not to leak anything
                onValueChangeListener = null;
            }
        });
    }

    @Override
    public void onValueChanged(String id, T keyValue) {
        if (onValueChangeListener != null) {
            onValueChangeListener.onValueChanged(id, keyValue);
        }
    }

    private void checkIfOnMainThread() {
        // if looper attached to current thread is not main
        // thread looper, throw exception
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new UnsupportedOperationException("Subscriber must observer " +
                    "changes on MainThread");
        }
    }
}
