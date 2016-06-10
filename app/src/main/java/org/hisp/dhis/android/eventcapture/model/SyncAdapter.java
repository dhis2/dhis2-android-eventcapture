package org.hisp.dhis.android.eventcapture.model;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.ui.AppPreferences;
import org.hisp.dhis.client.sdk.ui.SyncDateWrapper;
import org.hisp.dhis.client.sdk.ui.bindings.commons.DefaultNotificationHandler;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = SyncAdapter.class.getSimpleName();

    @Inject
    SyncWrapper syncWrapper;

    @Inject
    SyncDateWrapper syncDateWrapper;

    @Inject
    AppPreferences appPreferences;

    @Inject
    DefaultNotificationHandler notificationHandler;


    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        //inject the syncWrapper:
        ((EventCaptureApp) context.getApplicationContext()).getUserComponent().inject(this);
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);

        //inject the syncWrapper:
        ((EventCaptureApp) context.getApplicationContext()).getUserComponent().inject(this);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        syncWrapper.checkIfSyncIsNeeded()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .switchMap(new Func1<Boolean, Observable<List<Event>>>() {
                    @Override
                    public Observable<List<Event>> call(Boolean syncIsNeeded) {
                        if (syncIsNeeded) {
                            if (appPreferences.getSyncNotifications()) {
                                notificationHandler.showIsSyncingNotification();
                            }
                            return syncWrapper.backgroundSync();
                        }
                        return Observable.empty();
                    }
                })
                .subscribe(new Action1<List<Event>>() {
                               @Override
                               public void call(List<Event> events) {
                                   if (events != Observable.empty()) {
                                       syncDateWrapper.setLastSyncedNow();

                                       if (appPreferences.getSyncNotifications()) {
                                           notificationHandler.showSyncCompletedNotification(true);
                                       }
                                   }
                               }
                           }, new Action1<Throwable>() {
                               @Override
                               public void call(Throwable throwable) {
                                   Log.e(TAG, "Background synchronization failed.", throwable);
                                   if (appPreferences.getSyncNotifications()) {
                                       notificationHandler.showSyncCompletedNotification(false);
                                   }
                               }
                           }
                );

    }
}