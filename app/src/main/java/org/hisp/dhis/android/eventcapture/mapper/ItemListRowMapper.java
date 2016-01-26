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
        List<Pair<String,Integer>> valuesPos = new ArrayList<>();
        valuesPos.add(new Pair<String, Integer>("Erling", 1));
        valuesPos.add(new Pair<String, Integer>("Fjelstad", 2));
        valuesPos.add(new Pair<String, Integer>("Mann", 3));
        ItemListRow itemCardRow1 = new ItemListRow(null, valuesPos, EItemListRowStatus.OFFLINE.toString());

        List<Pair<String,Integer>> valuesPos1 = new ArrayList<>();
        valuesPos1.add(new Pair<String, Integer>("Simen", 1));
        valuesPos1.add(new Pair<String, Integer>("R", 1));
        valuesPos1.add(new Pair<String, Integer>("Russnes", 2));
        valuesPos1.add(new Pair<String, Integer>("Mann", 3));
        ItemListRow itemCardRow2 = new ItemListRow(null, valuesPos1, EItemListRowStatus.SENT.toString());
        List<Pair<String,Integer>> valuesPos2 = new ArrayList<>();
        valuesPos2.add(new Pair<String, Integer>("Araz", 1));
        valuesPos2.add(new Pair<String, Integer>("AB", 1));
        valuesPos2.add(new Pair<String, Integer>("Abishov", 2));
        valuesPos2.add(new Pair<String, Integer>("Man", 3));
        ItemListRow itemCardRow3 = new ItemListRow(null, valuesPos2, EItemListRowStatus.ERROR.toString());
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
        itemCardRow1.setOnRowClickListener(onClickListener);
        itemCardRow1.setOnStatusClickListener(onStatusClickListener);
        itemCardRow1.setOnLongClickListener(onLongClickListener);
        itemCardRow2.setOnRowClickListener(onClickListener);
        itemCardRow2.setOnStatusClickListener(onStatusClickListener);
        itemCardRow2.setOnLongClickListener(onLongClickListener);
        itemCardRow3.setOnRowClickListener(onClickListener);
        itemCardRow3.setOnStatusClickListener(onStatusClickListener);
        itemCardRow3.setOnLongClickListener(onLongClickListener);
        List<ItemListRow> itemCardRowList = new ArrayList<>();
        itemCardRowList.add(itemCardRow1);
        itemCardRowList.add(itemCardRow2);
        itemCardRowList.add(itemCardRow3);

        return itemCardRowList;
    }

}
