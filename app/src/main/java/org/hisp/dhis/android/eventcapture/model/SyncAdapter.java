package org.hisp.dhis.android.eventcapture.model;

import android.accounts.Account;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.views.activities.HomeActivity;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.ui.AppPreferences;
import org.hisp.dhis.client.sdk.ui.SyncDateWrapper;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = SyncAdapter.class.getSimpleName();

    ContentResolver mContentResolver;

    @Inject
    SyncWrapper syncWrapper;

    @Inject
    SyncDateWrapper syncDateWrapper;

    @Inject
    AppPreferences appPreferences;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();

        //inject the syncWrapper:
        ((EventCaptureApp) context.getApplicationContext()).getUserComponent().inject(this);
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
        //inject the syncWrapper:
        ((EventCaptureApp) context.getApplicationContext()).getUserComponent().inject(this);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        if (syncWrapper == null) {
            return;
        }

        if (syncIsNeeded()) {
            if (appPreferences.getSyncNotifications()) {
                showIsSyncingNotification();
            }
            syncWrapper.syncMetaData()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .switchMap(new Func1<List<ProgramStageDataElement>, Observable<List<Event>>>() {
                        @Override
                        public Observable<List<Event>> call(List<ProgramStageDataElement> programStageDataElements) {
                            return syncWrapper.syncData();
                        }
                    })
                    .subscribe(new Action1<List<Event>>() {
                                   @Override
                                   public void call(List<Event> events) {
                                       syncDateWrapper.setLastSyncedNow();
                                       if (appPreferences.getSyncNotifications()) {
                                           showSyncCompletedNotification(true);
                                       }
                                   }
                               }, new Action1<Throwable>() {
                                   @Override
                                   public void call(Throwable throwable) {
                                       Log.e(TAG, "Background synchronization failed.", throwable);
                                       if (appPreferences.getSyncNotifications()) {
                                           showSyncCompletedNotification(false);
                                       }
                                   }
                               }
                    );
        }

    }

    private boolean syncIsNeeded() {
        // TODO: Use convenience method from SDK when available
        return true;
    }

    private void showIsSyncingNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
        builder.setContentTitle(getContext().getString(R.string.sync_in_progress_notification_title))
                .setContentText(getContext().getString(R.string.sync_in_progress_notification_content))
                .setProgress(0, 0, true)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS);

        showSyncNotification(builder);
    }

    private void showSyncCompletedNotification(boolean successful) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());

        if (successful) {
            builder.setContentTitle(getContext().getString(R.string.sync_complete_notification_title))
                    .setContentText(getContext().getString(R.string.sync_complete_notification_content))
                    .setCategory(NotificationCompat.CATEGORY_STATUS);
        } else {
            builder.setContentTitle(getContext().getString(R.string.sync_failed_notification_title))
                    .setContentText(getContext().getString(R.string.sync_failed_notification_content))
                    .setCategory(NotificationCompat.CATEGORY_ERROR);
        }

        // remove progressbar
        builder.setProgress(0, 0, false);

        showSyncNotification(builder);
    }

    private void showSyncNotification(NotificationCompat.Builder builder) {

        builder.setSmallIcon(R.drawable.ic_notification);

        Intent resultIntent = new Intent(getContext(), HomeActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());

        stackBuilder.addParentStack(HomeActivity.class);

        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(007, builder.build());
    }
}