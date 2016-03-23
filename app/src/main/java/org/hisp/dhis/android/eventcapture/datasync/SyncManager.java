package org.hisp.dhis.android.eventcapture.datasync;

import android.support.annotation.Nullable;

import org.hisp.dhis.client.sdk.ui.SettingPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SyncManager {

    static private SyncManager instance;
    public static final long DAY_IN_MILLISECONDS = 86400000; //1000milliseconds * 60sec * 60 min * 24h

    private long lastSynced = 0l;

    private SyncManager() {}

    static public SyncManager getInstance() {
        if (instance == null) {
            instance = new SyncManager();
        }
        return instance;
    }

    public void sync() {
        //...metadata sync code calls go here...

        //and on success :
        setLastSyncedNow();
    }

    public void setLastSyncedNow() {
        lastSynced = Calendar.getInstance().getTime().getTime();
        SettingPreferences.setLastSynched(lastSynced);
    }

    @Nullable
    public Date getLastSyncedDate() {
        if (lastSynced == 0l) {
            lastSynced = SettingPreferences.getLastSynced();
            if (lastSynced == 0l) {
                return null;
            }
        }
        Date date = new Date();
        date.setTime(lastSynced);
        return date;
    }

    public long getLastSyncedLong() {
        if (lastSynced == 0l) {
            lastSynced = SettingPreferences.getLastSynced();
        }
        return lastSynced;
    }

    public String getLastSyncedString() {
        getLastSyncedLong();
        if (lastSynced == 0f) {
            return "Never";
        } else {
            Long diff = Calendar.getInstance().getTime().getTime() - lastSynced;
            if (diff >= DAY_IN_MILLISECONDS) {
                Date d = getLastSyncedDate();
                SimpleDateFormat dt = new SimpleDateFormat("dd/mm/yy hh:mm");
                return dt.format(d);
            } else {
                Long hours = TimeUnit.MILLISECONDS.toHours(diff);
                Long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);

                String result = "";
                if (hours > 0) {
                    result += hours + "h ";
                }
                result += minutes + "m ago";
                return result;
            }
        }
    }


}
