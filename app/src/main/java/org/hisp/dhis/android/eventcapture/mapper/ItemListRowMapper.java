package org.hisp.dhis.android.eventcapture.mapper;

import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;

import org.hisp.dhis.android.eventcapture.views.EventListRow;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.EItemListRowStatus;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.IItemListRow;


import java.util.ArrayList;
import java.util.List;

import rx.Observable;

import static org.hisp.dhis.client.sdk.models.utils.Preconditions.isNull;

public class ItemListRowMapper {
    public final String TAG = this.getClass().getSimpleName();
    public ItemListRowMapper() {

    }

    public IItemListRow transform(Event event) {
        isNull(event, "Event object must not be null");



        List<TrackedEntityDataValue> trackedEntityDataValues = event.getTrackedEntityDataValues();
        EventListRow itemListRow = null;
//        new ItemListRow(event, event.getTrackedEntityDataValues(), event.getStatus());


        return itemListRow;
    }

    public List<IItemListRow> transform(List<Event> events) {

        return null;
    }

    public List<IItemListRow> transform(Observable<List<Event>> events) {
        Event event1 = new Event();
        event1.setUId("001");
        List<Pair<String,Integer>> itemListRow1Values = new ArrayList<>();
        itemListRow1Values.add(new Pair<>("Erling", 1));
        itemListRow1Values.add(new Pair<>("Fjelstad", 2));
        itemListRow1Values.add(new Pair<>("Mann", 3));
        IItemListRow itemListRow1 = new EventListRow(event1, itemListRow1Values, EItemListRowStatus.OFFLINE.toString());

        Event event2 = new Event();
        event2.setUId("002");
        List<Pair<String,Integer>> itemListRow2Values = new ArrayList<>();
        itemListRow2Values.add(new Pair<>("Simen", 1));
        itemListRow2Values.add(new Pair<>("R", 1));
        itemListRow2Values.add(new Pair<>("Russnes", 2));
        itemListRow2Values.add(new Pair<>("Mann", 3));
        IItemListRow itemListRow2 = new EventListRow(event2, itemListRow2Values, EItemListRowStatus.SENT.toString());

        Event event3 = new Event();
        event3.setUId("003");
        List<Pair<String,Integer>> itemListRow3Values = new ArrayList<>();
        itemListRow3Values.add(new Pair<>("Araz", 1));
        itemListRow3Values.add(new Pair<>("AB", 1));
        itemListRow3Values.add(new Pair<>("Abishov", 2));
        itemListRow3Values.add(new Pair<>("Man", 3));
        IItemListRow itemListRow3 = new EventListRow(event3, itemListRow3Values, EItemListRowStatus.ERROR.toString());
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick");
            }
        };
        View.OnClickListener onStatusClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onStatusClick");
            }
        };
        View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(TAG, "onLongClick");
                return true;
            }
        };
        itemListRow1.setOnRowClickListener(onClickListener);
        itemListRow1.setOnStatusClickListener(onStatusClickListener);
        itemListRow1.setOnLongClickListener(onLongClickListener);
        itemListRow2.setOnRowClickListener(onClickListener);
        itemListRow2.setOnStatusClickListener(onStatusClickListener);
        itemListRow2.setOnLongClickListener(onLongClickListener);
        itemListRow3.setOnRowClickListener(onClickListener);
        itemListRow3.setOnStatusClickListener(onStatusClickListener);
        itemListRow3.setOnLongClickListener(onLongClickListener);
        List<IItemListRow> itemListRows = new ArrayList<>();
        itemListRows.add(itemListRow1);
        itemListRows.add(itemListRow2);
        itemListRows.add(itemListRow3);

        return itemListRows;
    }

}
