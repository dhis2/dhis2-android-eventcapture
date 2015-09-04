/*
 *  Copyright (c) 2015, University of Oslo
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice, this
 *  * list of conditions and the following disclaimer.
 *  *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *  * this list of conditions and the following disclaimer in the documentation
 *  * and/or other materials provided with the distribution.
 *  * Neither the name of the HISP project nor the names of its contributors may
 *  * be used to endorse or promote products derived from this software without
 *  * specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.hisp.dhis.android.eventcapture.fragments;

import android.content.Context;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController;
import org.hisp.dhis.android.sdk.events.OnRowClick;
import org.hisp.dhis.android.sdk.ui.adapters.rows.events.TrackedEntityInstanceColumnNamesRow;
import org.hisp.dhis.android.sdk.ui.fragments.selectprogram.SelectProgramFragmentForm;
import org.hisp.dhis.android.sdk.persistence.loaders.Query;
import org.hisp.dhis.android.sdk.ui.adapters.rows.events.EventItemRow;
import org.hisp.dhis.android.sdk.ui.adapters.rows.events.EventRow;
import org.hisp.dhis.android.sdk.persistence.models.DataValue;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.FailedItem;
import org.hisp.dhis.android.sdk.persistence.models.Option;
import org.hisp.dhis.android.sdk.persistence.models.Program;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class SelectProgramFragmentQuery implements Query<SelectProgramFragmentForm> {
    private static final String TAG = SelectProgramFragmentQuery.class.getSimpleName();
    private final String mOrgUnitId;
    private final String mProgramId;

    public SelectProgramFragmentQuery(String orgUnitId, String programId) {
        mOrgUnitId = orgUnitId;
        mProgramId = programId;
    }

    @Override
    public SelectProgramFragmentForm query(Context context) {
        SelectProgramFragmentForm fragmentForm = new SelectProgramFragmentForm();
        List<EventRow> eventEventRows = new ArrayList<>();

        // create a list of EventItems
        Program selectedProgram = MetaDataController.getProgram(mProgramId);
        if (selectedProgram == null || isListEmpty(selectedProgram.getProgramStages())) {
            return fragmentForm;
        }

        // since this is single event its only 1 stage
        ProgramStage programStage = selectedProgram.getProgramStages().get(0);
        if (programStage == null || isListEmpty(programStage.getProgramStageDataElements())) {
            return fragmentForm;
        }

        List<ProgramStageDataElement> stageElements = programStage
                .getProgramStageDataElements();
        if (isListEmpty(stageElements)) {
            return fragmentForm;
        }

        List<String> elementsToShow = new ArrayList<>();
        TrackedEntityInstanceColumnNamesRow columnNames = new TrackedEntityInstanceColumnNamesRow();


        for (ProgramStageDataElement stageElement : stageElements) {
            if (stageElement.getDisplayInReports() && elementsToShow.size() < 3) {
                elementsToShow.add(stageElement.getDataelement());
                if (stageElement.getDataElement() != null) {
                    String name = stageElement.getDataElement().getShortName();
                    if (elementsToShow.size() == 1) {
                        columnNames.setFirstItem(name);
                    } else if (elementsToShow.size() == 2) {
                        columnNames.setSecondItem(name);
                    } else if (elementsToShow.size() == 3) {
                        columnNames.setThirdItem(name);
                    }
                }
            }
        }
        eventEventRows.add(columnNames);
        List<Event> events = TrackerController.getEvents(
                mOrgUnitId, mProgramId
        );
        if (isListEmpty(events)) {
            return fragmentForm;
        }

        List<Option> options = new Select().from(Option.class).queryList();
        Map<String, String> codeToName = new HashMap<>();
        for (Option option : options) {
            codeToName.put(option.getCode(), option.getName());
        }

        List<FailedItem> failedEvents = TrackerController.getFailedItems(FailedItem.EVENT);

        Set<String> failedEventIds = new HashSet<>();
        for (FailedItem failedItem : failedEvents) {
            Event event = (Event) failedItem.getItem();
            failedEventIds.add(event.getEvent());
        }

        Collections.sort(events, new EventComparator());
        for (Event event : events) {
            eventEventRows.add(createEventItem(context,
                    event, elementsToShow,
                    codeToName, failedEventIds));
        }

        fragmentForm.setEventRowList(eventEventRows);
        fragmentForm.setProgram(selectedProgram);

        return fragmentForm;
    }

    private EventItemRow createEventItem(Context context, Event event, List<String> elementsToShow,
                                         Map<String, String> codeToName,
                                         Set<String> failedEventIds) {
        EventItemRow eventItem = new EventItemRow(context);
        eventItem.setEvent(event);

        if (event.isFromServer()) {
            eventItem.setStatus(OnRowClick.ITEM_STATUS.SENT);
        } else if (failedEventIds.contains(event.getEvent())) {
            eventItem.setStatus(OnRowClick.ITEM_STATUS.ERROR);
        } else {
            eventItem.setStatus(OnRowClick.ITEM_STATUS.OFFLINE);
        }

        for (int i = 0; i < 3; i++) {
            if (i >= elementsToShow.size()) {
                break;
            }
            String dataElement = elementsToShow.get(i);
            if (dataElement != null) {
                DataValue dataValue = getDataValue(event, dataElement);
                if (dataValue == null) {
                    continue;
                }

                String code = dataValue.getValue();
                String name = codeToName.get(code) == null ? code : codeToName.get(code);

                if (i == 0) {
                    eventItem.setFirstItem(name);
                } else if (i == 1) {
                    eventItem.setSecondItem(name);
                } else if (i == 2) {
                    eventItem.setThirdItem(name);
                }
            }
        }
        return eventItem;
    }

    private DataValue getDataValue(Event event, String dataElement) {
        return TrackerController.getDataValue(event.getLocalId(), dataElement);
    }

    private static <T> boolean isListEmpty(List<T> items) {
        return items == null || items.isEmpty();
    }

    private static class EventComparator implements Comparator<Event> {

        @Override
        public int compare(Event first, Event second) {
            if (first.getLastUpdated() == null || second.getLastUpdated() == null) {
                return 0;
            }

            DateTime firstDateTime = first.getLastUpdated();
            DateTime secondDateTime = second.getLastUpdated();

            if (firstDateTime.isBefore(secondDateTime)) {
                return 1;
            } else if (firstDateTime.isAfter(secondDateTime)) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}