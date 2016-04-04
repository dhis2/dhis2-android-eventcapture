package org.hisp.dhis.android.eventcapture.mapper;

import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;

import org.hisp.dhis.android.eventcapture.views.EventListRow;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.EItemListRowStatus;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.IItemListRow;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rx.Observable;

import static org.hisp.dhis.client.sdk.models.utils.Preconditions.isNull;

public class ItemListRowMapper {
    public final String TAG = this.getClass().getSimpleName();
    public ItemListRowMapper() {

    }

    public IItemListRow transform(Event event) {
        isNull(event, "Event object must not be null");



        List<TrackedEntityDataValue> trackedEntityDataValues = event.getDataValues();
        EventListRow itemListRow = null;
//        new ItemListRow(event, event.getTrackedEntityDataValues(), event.getStatus());


        return itemListRow;
    }

    public List<IItemListRow> transform(List<Event> events) {

        return null;
    }

    public IItemListRow transform(Event event, Map<String,String> map) {
        List<Pair<String, Integer>> valuePos = new ArrayList<>();
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            int i = valuePos.size();
            if (valuePos.size() == 3) {
                break;
            }
            Map.Entry<String, String> entry = iterator.next();
            valuePos.add(new Pair<>(entry.getValue(), i));
            iterator.remove();
        }


        return EventListRow.create(event, valuePos, event.getStatus().name());

    }

    public IItemListRow transformToEventListRow(Program program, Event event) {


        EventListRow eventListRow = EventListRow.create(event,
                new ArrayList<Pair<String, Integer>>(), event.getStatus().name());
        return eventListRow;
    }

    public List<IItemListRow> getDummyData() {
        Event event1 = new Event();
        event1.setUId("001");
        List<Pair<String,Integer>> itemListRow1Values = new ArrayList<>();
        itemListRow1Values.add(new Pair<>("Erling", 1));
        itemListRow1Values.add(new Pair<>("Fjelstad", 2));
        itemListRow1Values.add(new Pair<>("Mann", 3));
        IItemListRow itemListRow1 = EventListRow.create(event1, itemListRow1Values, EItemListRowStatus.OFFLINE.toString());

        Event event2 = new Event();
        event2.setUId("002");
        List<Pair<String,Integer>> itemListRow2Values = new ArrayList<>();
        itemListRow2Values.add(new Pair<>("Simen", 1));
        itemListRow2Values.add(new Pair<>("R", 1));
        itemListRow2Values.add(new Pair<>("Russnes", 2));
        itemListRow2Values.add(new Pair<>("Mann", 3));
        IItemListRow itemListRow2 = EventListRow.create(event2, itemListRow2Values, EItemListRowStatus.SENT.toString());

        Event event3 = new Event();
        event3.setUId("003");
        List<Pair<String,Integer>> itemListRow3Values = new ArrayList<>();
        itemListRow3Values.add(new Pair<>("Araz", 1));
        itemListRow3Values.add(new Pair<>("AB", 1));
        itemListRow3Values.add(new Pair<>("Abishov", 2));
        itemListRow3Values.add(new Pair<>("Man", 3));
        IItemListRow itemListRow3 = EventListRow.create(event3, itemListRow3Values, EItemListRowStatus.ERROR.toString());
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
