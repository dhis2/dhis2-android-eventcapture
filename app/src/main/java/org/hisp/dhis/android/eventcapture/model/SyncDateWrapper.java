package org.hisp.dhis.android.eventcapture.model;

import android.support.annotation.Nullable;

import org.hisp.dhis.client.sdk.ui.AppPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class SyncDateWrapper {
    private static final long DAYS_OLD = 1L;
    private static final long NEVER = 0L;

    // TODO shift "Never" prompt to resources
    private static final String NEVER_PROMPT = "never";

    private final AppPreferences appPreferences;
    private final Calendar calendar;

    public SyncDateWrapper(AppPreferences appPreferences) {
        this.appPreferences = appPreferences;
        this.calendar = Calendar.getInstance();
    }

    public void setLastSyncedNow() {
        long lastSynced = calendar.getTime().getTime();
        appPreferences.setLastSynced(lastSynced);
    }

    public void clearLastSynced() {
        appPreferences.setLastSynced(NEVER);
    }

    @Nullable
    public Date getLastSyncedDate() {
        long lastSynced = appPreferences.getLastSynced();

        if (lastSynced > NEVER) {
            Date date = new Date();
            date.setTime(lastSynced);
            return date;
        }

        return null;
    }

    public long getLastSyncedLong() {
        return appPreferences.getLastSynced();
    }

    public String getLastSyncedString() {
        long lastSynced = getLastSyncedLong();

        if (lastSynced == NEVER) {
            return NEVER_PROMPT;
        }

        Long diff = calendar.getTime().getTime() - lastSynced;
        if (diff >= TimeUnit.DAYS.toMillis(DAYS_OLD)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm/yy hh:mm");
            return dateFormat.format(getLastSyncedDate());
        }

        Long hours = TimeUnit.MILLISECONDS.toHours(diff);
        Long minutes = TimeUnit.MILLISECONDS.toMinutes(
                diff - TimeUnit.HOURS.toMillis(hours));

        // TODO shift letters and keywords like "ago" to resources
        String result = "";
        if (hours > 0) {
            result += hours + "h ";
        }

        result += minutes + "m ago";
        return result;
    }
}
