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

import org.hisp.dhis.android.eventcapture.views.SelectorView2;
import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.client.sdk.android.organisationunit.UserOrganisationUnitInteractor;
import org.hisp.dhis.client.sdk.android.program.UserProgramInteractor;
import org.hisp.dhis.client.sdk.core.common.utils.ModelUtils;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.ui.models.picker.Picker;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class SelectorPresenterImpl implements SelectorPresenter {
    private static final String ORGANISATION_UNIT_PICKER = "picker:organisationUnit";
    private static final String PROGRAM_PICKER = "picker:program";

    private final UserOrganisationUnitInteractor organisationUnitInteractor;
    private final UserProgramInteractor programInteractor;
    private final Logger logger;
    private CompositeSubscription subscription;
    private SelectorView2 selectorView;

    public SelectorPresenterImpl(UserOrganisationUnitInteractor interactor,
                                 UserProgramInteractor programInteractor,
                                 Logger logger) {
        this.organisationUnitInteractor = interactor;
        this.programInteractor = programInteractor;
        this.subscription = new CompositeSubscription();
        this.logger = logger;
    }

    public void attachView(View view) {
        isNull(view, "SelectorView must not be null");
        selectorView = (SelectorView2) view;
    }

    @Override
    public void detachView() {
        selectorView = null;

        if (!subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = new CompositeSubscription();
        }
    }

    @Override
    public void sync() {
        selectorView.showProgressBar();
        subscription.add(Observable.zip(organisationUnitInteractor.pull(), programInteractor.pull(),
                new Func2<List<OrganisationUnit>, List<Program>, List<Program>>() {
                    @Override
                    public List<Program> call(
                            List<OrganisationUnit> organisationUnits, List<Program> programs) {
                        return programs;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Program>>() {
                    @Override
                    public void call(List<Program> programs) {
                        logger.d(SelectorPresenterImpl.class.getSimpleName(), "sync() - success");
                        selectorView.hideProgressBar();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(SelectorPresenterImpl.class.getSimpleName(),
                                "sync() failed", throwable);
                    }
                }));
    }

    @Override
    public void listPickers() {
        logger.d("listPickers()", "called");
        subscription.add(Observable.zip(organisationUnitInteractor.list(), programInteractor.list(),
                new Func2<List<OrganisationUnit>, List<Program>, Picker>() {
                    @Override
                    public Picker call(List<OrganisationUnit> units, List<Program> programs) {
                        Map<String, OrganisationUnit> organisationUnitMap = ModelUtils.toMap(units);
                        Map<String, Program> assignedProgramsMap = ModelUtils.toMap(programs);

                        Picker rootPicker = Picker.create("Choose organisation unit");
                        for (String unitKey : organisationUnitMap.keySet()) {
                            // Creating organisation unit picker items
                            OrganisationUnit organisationUnit = organisationUnitMap.get(unitKey);
                            Picker organisationUnitPicker = Picker.create(
                                    organisationUnit.getUId(),
                                    organisationUnit.getDisplayName(),
                                    "Choose program",
                                    rootPicker);

                            for (Program program : organisationUnit.getPrograms()) {
                                Program assignedProgram = assignedProgramsMap.get(program.getUId());

                                if (assignedProgram != null) {
                                    Picker programPicker = Picker.create(
                                            assignedProgram.getUId(),
                                            assignedProgram.getDisplayName(),
                                            organisationUnitPicker);
                                    organisationUnitPicker.addChild(programPicker);
                                }
                            }

                            rootPicker.addChild(organisationUnitPicker);
                        }

                        return rootPicker;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Picker>() {
                    @Override
                    public void call(Picker picker) {
                        if (selectorView != null) {
                            selectorView.showPickers(picker);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }));
    }
}
