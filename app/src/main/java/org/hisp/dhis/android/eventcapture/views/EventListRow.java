package org.hisp.dhis.android.eventcapture.views;

import android.support.v4.util.Pair;
import android.view.View;

import org.hisp.dhis.client.sdk.models.event.Event;

import java.util.List;

public class EventListRow implements IEventListRow {
    private Event event;
    private String status;
    private List<Pair<String, Integer>> valuesPosition;
    private View.OnClickListener onRowClickListener;
    private View.OnClickListener onStatusClickListener;
    private View.OnLongClickListener onLongClickListener;

    public EventListRow(Event event, List<Pair<String, Integer>> valuesPosition, String status) {
        this.event = event;
        this.valuesPosition = valuesPosition;
        this.status = status;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public List<Pair<String, Integer>> getValuesPosition() {
        return valuesPosition;
    }

    @Override
    public void setValuesPosition(List<Pair<String, Integer>> valuesPosition) {
        this.valuesPosition = valuesPosition;
    }

    @Override
    public View.OnClickListener getOnRowClickListener() {
        return onRowClickListener;
    }

    @Override
    public void setOnRowClickListener(View.OnClickListener onRowClickListener) {
        this.onRowClickListener = onRowClickListener;
    }

    @Override
    public View.OnClickListener getOnStatusClickListener() {
        return onStatusClickListener;
    }

    @Override
    public void setOnStatusClickListener(View.OnClickListener onStatusClickListener) {
        this.onStatusClickListener = onStatusClickListener;
    }

    @Override
    public View.OnLongClickListener getOnLongClickListener() {
        return onLongClickListener;
    }

    @Override
    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    @Override
    public Event getEvent() {
        return event;
    }

    @Override
    public void setEvent(Event event) {
        this.event = event;
    }
}
