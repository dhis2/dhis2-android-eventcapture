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
import org.hisp.dhis.client.sdk.ui.SyncDateWrapper;

import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = SyncAdapter.class.getSimpleName();

    ContentResolver mContentResolver;

    @Inject
    SyncWrapper syncWrapper;

    @Inject
    SyncDateWrapper syncDateWrapper;

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
            showIsSyncingNotification();
            syncWrapper.syncMetaData()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<ProgramStageDataElement>>() {
                                   @Override
                                   public void call(List<ProgramStageDataElement> o) {
                                       //TODO: do this "reactively" instead of nested calls
                                       syncWrapper.syncData().
                                               subscribeOn(AndroidSchedulers.mainThread()).
                                               subscribe(new Action1<List<Event>>() {
                                                   @Override
                                                   public void call(List<Event> events) {
                                                       syncDateWrapper.setLastSyncedNow();
                                                       showSyncCompletedNotification(true);
                                                   }
                                               }, new Action1<Throwable>() {
                                                   @Override
                                                   public void call(Throwable throwable) {
                                                       Log.e(TAG, "Background synchronization failed", throwable);
                                                       showSyncCompletedNotification(false);
                                                   }
                                               });
                                   }
                               }, new Action1<Throwable>() {
                                   @Override
                                   public void call(Throwable throwable) {
                                       Log.e(TAG, "Background synchronization of metadata failed.", throwable);
                                       showSyncCompletedNotification(false);
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
        String title = getContext().getString(R.string.sync_in_progress_notification_title);
        String contentText = getContext().getString(R.string.sync_in_progress_notification_content);
        boolean showProgressBarInNotification = true;
        showSyncNotification(title, contentText, showProgressBarInNotification);
    }

    private void showSyncCompletedNotification(boolean successful) {

        String title;
        String contentText;

        if (successful) {
            title = getContext().getString(R.string.sync_complete_notification_title);
            contentText = getContext().getString(R.string.sync_complete_notification_content);
        } else {
            title = getContext().getString(R.string.sync_failed_notification_title);
            contentText = getContext().getString(R.string.sync_failed_notification_content);
        }
        boolean showProgressBarInNotification = false;
        showSyncNotification(title, contentText, showProgressBarInNotification);
    }

    private void showSyncNotification(String title, String contentText, boolean showProgressBar) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getContext());
        notificationBuilder.setSmallIcon(R.drawable.ic_notification).
                setContentTitle(title).
                setContentText(contentText).
                setCategory(NotificationCompat.CATEGORY_STATUS);

        Intent resultIntent = new Intent(getContext(), HomeActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(HomeActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        notificationBuilder.setContentIntent(resultPendingIntent);
        notificationBuilder.setProgress(0, 0, showProgressBar);

        NotificationManager notificationManager =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(007, notificationBuilder.build());
    }
}