package org.hisp.dhis.android.eventcapture.mapper;

import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;

import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.EItemListRowStatus;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.ItemListRow;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

import static org.hisp.dhis.client.sdk.models.utils.Preconditions.isNull;

public class ItemListRowMapper {
    public final String TAG = this.getClass().getSimpleName();
    public ItemListRowMapper() {

    }

    public ItemListRow transform(Event event) {
        isNull(event, "Event object must not be null");



        List<TrackedEntityDataValue> trackedEntityDataValues = event.getTrackedEntityDataValues();
        ItemListRow itemListRow = null;
//        new ItemListRow(event, event.getTrackedEntityDataValues(), event.getStatus());


        return itemListRow;
    }

    public List<ItemListRow> transform(List<Event> events) {

        return null;
    }

    public List<ItemListRow> transform(Observable<List<Event>> events) {
        List<Pair<String,Integer>> itemListRow1Values = new ArrayList<>();
        itemListRow1Values.add(new Pair<String, Integer>("Erling", 1));
        itemListRow1Values.add(new Pair<String, Integer>("Fjelstad", 2));
        itemListRow1Values.add(new Pair<String, Integer>("Mann", 3));
        ItemListRow itemListRow1 = new ItemListRow(null, itemListRow1Values, EItemListRowStatus.OFFLINE.toString());

        List<Pair<String,Integer>> itemListRow2Values = new ArrayList<>();
        itemListRow2Values.add(new Pair<String, Integer>("Simen", 1));
        itemListRow2Values.add(new Pair<String, Integer>("R", 1));
        itemListRow2Values.add(new Pair<String, Integer>("Russnes", 2));
        itemListRow2Values.add(new Pair<String, Integer>("Mann", 3));
        ItemListRow itemListRow2 = new ItemListRow(null, itemListRow2Values, EItemListRowStatus.SENT.toString());
        List<Pair<String,Integer>> itemListRow3Values = new ArrayList<>();
        itemListRow3Values.add(new Pair<String, Integer>("Araz", 1));
        itemListRow3Values.add(new Pair<String, Integer>("AB", 1));
        itemListRow3Values.add(new Pair<String, Integer>("Abishov", 2));
        itemListRow3Values.add(new Pair<String, Integer>("Man", 3));
        ItemListRow itemListRow3 = new ItemListRow(null, itemListRow3Values, EItemListRowStatus.ERROR.toString());
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
        List<ItemListRow> itemListRows = new ArrayList<>();
        itemListRows.add(itemListRow1);
        itemListRows.add(itemListRow2);
        itemListRows.add(itemListRow3);

        return itemListRows;
    }

}
