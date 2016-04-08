/*
 * Copyright (c) 2016, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.eventcapture.presenters;

import org.hisp.dhis.android.eventcapture.views.IOrganisationUnitProgramPickerView;
import org.hisp.dhis.android.eventcapture.views.OrganisationUnitPickable;
import org.hisp.dhis.android.eventcapture.views.ProgramPickable;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramType;
import org.hisp.dhis.client.sdk.models.utils.Preconditions;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.Pickable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static org.hisp.dhis.client.sdk.ui.utils.Preconditions.isNull;

public class OrganisationUnitProgramPickerPresenter {
    private IOrganisationUnitProgramPickerView mOrganisationUnitProgramPickerView;
    private OrganisationUnitPickableMapper mOrganisationUnitPickableMapper;
    private ProgramPickableMapper mProgramPickableMapper;
    private Subscription programSubscription;
    private Subscription organisationUnitSubscription;
    private Subscription pickedOrganisationUnitSubscription;


    public OrganisationUnitProgramPickerPresenter(IOrganisationUnitProgramPickerView pickerView) {
        mOrganisationUnitPickableMapper = new OrganisationUnitPickableMapper();
        mProgramPickableMapper = new ProgramPickableMapper();
        mOrganisationUnitProgramPickerView = pickerView;

        loadOrganisationUnits();
    }

    public void onDestroy() {
        if (organisationUnitSubscription != null && !organisationUnitSubscription.isUnsubscribed
                ()) {
            organisationUnitSubscription.unsubscribe();
            organisationUnitSubscription = null;
        }

        if (programSubscription != null && !programSubscription.isUnsubscribed()) {
            programSubscription.unsubscribe();
            programSubscription = null;
        }

        if (pickedOrganisationUnitSubscription != null && !pickedOrganisationUnitSubscription
                .isUnsubscribed()) {
            pickedOrganisationUnitSubscription.unsubscribe();
            pickedOrganisationUnitSubscription = null;
        }
    }

    public void loadOrganisationUnits() {
        if (organisationUnitSubscription == null || organisationUnitSubscription.isUnsubscribed()) {
            mOrganisationUnitProgramPickerView.onStartLoading();

            organisationUnitSubscription = D2.me().organisationUnits().list()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<OrganisationUnit>>() {
                        @Override
                        public void call(List<OrganisationUnit> organisationUnits) {
                            setOrganisationUnitPickables(organisationUnits);
                            mOrganisationUnitProgramPickerView.onFinishLoading();
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            mOrganisationUnitProgramPickerView.onLoadingError(); // (throwable);
                        }
                    });
        }
    }

    public void loadPrograms(OrganisationUnit organisationUnit) {

        if (programSubscription == null || programSubscription.isUnsubscribed()) {
            mOrganisationUnitProgramPickerView.onStartLoading();

            // TODO revise
            programSubscription = D2.me().programs().list(Arrays.asList(organisationUnit))
                    // ProgramType.WITHOUT_REGISTRATION)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<Program>>() {
                        @Override
                        public void call(List<Program> programs) {
                            List<Program> filteredPrograms = new ArrayList<>();

                            for (Program program : programs) {
                                if (program.isAssignedToUser() && ProgramType.WITHOUT_REGISTRATION
                                        .equals(program.getProgramType())) {
                                    filteredPrograms.add(program);
                                }
                            }

                            setProgramPickables(filteredPrograms);
                            mOrganisationUnitProgramPickerView.onFinishLoading();
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            mOrganisationUnitProgramPickerView.onLoadingError(); //(throwable);
                        }
                    });
        }
    }

    public void setOrganisationUnitPickables(List<OrganisationUnit> organisationUnits) {
        List<Pickable> organisationUnitPickables =
                mOrganisationUnitPickableMapper.transform(organisationUnits);
        mOrganisationUnitProgramPickerView.renderOrganisationUnitPickables(
                organisationUnitPickables);

    }

    public void setProgramPickables(List<Program> programs) {
        List<Pickable> programPickables = mProgramPickableMapper.transform(programs);
        mOrganisationUnitProgramPickerView.renderProgramPickables(programPickables);

    }

    public void setOrganisationUnitProgramPickerView(IOrganisationUnitProgramPickerView
                                                             mOrganisationUnitProgramPickerView) {
        this.mOrganisationUnitProgramPickerView = mOrganisationUnitProgramPickerView;
    }

    public void setPickedOrganisationUnit(Observable<OrganisationUnit> organisationUnit) {
        if (pickedOrganisationUnitSubscription == null ||
                pickedOrganisationUnitSubscription.isUnsubscribed()) {
            pickedOrganisationUnitSubscription = organisationUnit.
                    subscribeOn(Schedulers.io()).
                    observeOn(AndroidSchedulers.mainThread()).
                    subscribe(new Action1<OrganisationUnit>() {
                        @Override
                        public void call(OrganisationUnit organisationUnit) {
                            loadPrograms(organisationUnit);
                        }
                    });

        }
    }

    private static class OrganisationUnitPickableMapper {

        public Pickable transform(OrganisationUnit organisationUnit) {
            isNull(organisationUnit, "Org unit must not be null");

            Pickable organisationUnitPickable = new OrganisationUnitPickable(organisationUnit.getName(), organisationUnit.getUId());
            return organisationUnitPickable;
        }

        public List<Pickable> transform(List<OrganisationUnit> organisationUnits) {
            List<Pickable> organisationUnitPickables = new ArrayList<>();

            for (OrganisationUnit organisationUnit : organisationUnits) {
                Pickable organisationUnitPickable = transform(organisationUnit);
                organisationUnitPickables.add(organisationUnitPickable);
            }

            return organisationUnitPickables;
        }
    }

    private static class ProgramPickableMapper {
        public Pickable transform(Program program) {
            Preconditions.isNull(program, "Program must not be null");

            Pickable programPickable = new ProgramPickable(program.getName(), program.getUId());
            return programPickable;
        }

        public List<Pickable> transform(List<Program> programs) {
            List<Pickable> programPickables = new ArrayList<>();

            for (Program program : programs) {
                Pickable programPickable = transform(program);
                programPickables.add(programPickable);
            }

            return programPickables;
        }
    }
}
