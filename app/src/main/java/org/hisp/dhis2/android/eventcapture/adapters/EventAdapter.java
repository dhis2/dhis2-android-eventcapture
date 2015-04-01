/*
 * Copyright (c) 2014, Araz Abishov
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis2.android.eventcapture.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.eventcapture.models.EventItem;

import java.util.List;

public class EventAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private List<EventItem> mItems;

    public EventAdapter(LayoutInflater inflater) {
        this.mInflater = inflater;
    }

    @Override
    public int getCount() {
        if (mItems != null) {
            return mItems.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if (mItems != null) {
            return mItems.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.listview_event_item, parent, false);
            holder = new ViewHolder(
                    (TextView) view.findViewById(R.id.first_event_item),
                    (TextView) view.findViewById(R.id.second_event_item),
                    (TextView) view.findViewById(R.id.third_event_item)
            );
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        EventItem eventItem = (EventItem) getItem(position);
        if (eventItem != null) {
            holder.firstItem.setText(eventItem.getFirstItem());
            holder.secondItem.setText(eventItem.getSecondItem());
            holder.thirdItem.setText(eventItem.getThirdItem());
        }
        return view;
    }

    public void swapData(List<EventItem> items) {
        boolean notifyAdapter = mItems != items;
        mItems = items;

        if (notifyAdapter) {
            notifyDataSetChanged();
        }
    }

    private static class ViewHolder {
        public final TextView firstItem;
        public final TextView secondItem;
        public final TextView thirdItem;

        private ViewHolder(TextView firstItem,
                           TextView secondItem,
                           TextView thirdItem) {
            this.firstItem = firstItem;
            this.secondItem = secondItem;
            this.thirdItem = thirdItem;
        }
    }
}
