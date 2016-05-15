/*
 *  Copyright (c) 2016, University of Oslo
 *
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *  Neither the name of the HISP project nor the names of its contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.eventcapture.model;

import android.content.Context;

import org.hisp.dhis.client.sdk.android.event.EventInteractor;
import org.hisp.dhis.client.sdk.android.organisationunit.OrganisationUnitInteractor;
import org.hisp.dhis.client.sdk.android.organisationunit.UserOrganisationUnitInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageDataElementInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageSectionInteractor;
import org.hisp.dhis.client.sdk.android.program.UserProgramInteractor;
import org.hisp.dhis.client.sdk.core.common.utils.ModelUtils;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.models.program.ProgramType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

public class SyncWrapper {
    UserOrganisationUnitInteractor userOrganisationUnitInteractor;
    UserProgramInteractor userProgramInteractor;
    OrganisationUnitInteractor organisationUnitInteractor;
    ProgramInteractor programInteractor;
    ProgramStageInteractor programStageInteractor;
    ProgramStageSectionInteractor programStageSectionInteractor;
    ProgramStageDataElementInteractor programStageDataElementInteractor;
    EventInteractor eventInteractor;

    public SyncWrapper(Context context,
                       UserOrganisationUnitInteractor userOrganisationUnitInteractor,
                       UserProgramInteractor userProgramInteractor,
                       OrganisationUnitInteractor organisationUnitInteractor,
                       ProgramInteractor programInteractor,
                       ProgramStageInteractor programStageInteractor,
                       ProgramStageSectionInteractor programStageSectionInteractor,
                       ProgramStageDataElementInteractor programStageDataElementInteractor,
                       EventInteractor eventInteractor) {
        
        this.userOrganisationUnitInteractor = userOrganisationUnitInteractor;
        this.userProgramInteractor = userProgramInteractor;
        this.organisationUnitInteractor = organisationUnitInteractor;
        this.programInteractor = programInteractor;
        this.programStageInteractor = programStageInteractor;
        this.programStageSectionInteractor = programStageSectionInteractor;
        this.programStageDataElementInteractor = programStageDataElementInteractor;
        this.eventInteractor = eventInteractor;
    }

    public Observable sync() {
        return Observable.zip(
                userOrganisationUnitInteractor.pull(),
                userProgramInteractor.pull(),
                new Func2<List<OrganisationUnit>, List<Program>, List<Program>>() {
                    @Override
                    public List<Program> call(List<OrganisationUnit> units, List<Program> programs) {
                        return programs;
                    }
                })
                .map(new Func1<List<Program>, List<ProgramStageDataElement>>() {
                    @Override
                    public List<ProgramStageDataElement> call(List<Program> programs) {
                        List<Program> programsWithoutRegistration = new ArrayList<>();

                        if (programs != null && !programs.isEmpty()) {
                            for (Program program : programs) {
                                if (ProgramType.WITHOUT_REGISTRATION
                                        .equals(program.getProgramType())) {
                                    programsWithoutRegistration.add(program);
                                }
                            }
                        }

                        List<ProgramStage> programStages =
                                loadProgramStages(programsWithoutRegistration);
                        List<ProgramStageSection> programStageSections =
                                loadProgramStageSections(programStages);

                        return loadProgramStageDataElements(programStages, programStageSections);
                    }
                });
    }

    private List<ProgramStage> loadProgramStages(List<Program> programs) {
        Set<String> stageUids = new HashSet<>();

        for (Program program : programs) {
            Set<String> programStageUids = ModelUtils.toUidSet(
                    program.getProgramStages());
            stageUids.addAll(programStageUids);
        }
        return programStageInteractor.pull(stageUids).toBlocking().first();
    }

    private List<ProgramStageSection> loadProgramStageSections(List<ProgramStage> stages) {
        Set<String> sectionUids = new HashSet<>();

        for (ProgramStage programStage : stages) {
            Set<String> stageSectionUids = ModelUtils.toUidSet(
                    programStage.getProgramStageSections());
            sectionUids.addAll(stageSectionUids);
        }
        return programStageSectionInteractor.pull(sectionUids).toBlocking().first();
    }

    private List<ProgramStageDataElement> loadProgramStageDataElements(
            List<ProgramStage> stages, List<ProgramStageSection> programStageSections) {

        Set<String> dataElementUids = new HashSet<>();

        for (ProgramStage programStage : stages) {
            Set<String> stageDataElementUids = ModelUtils.toUidSet(
                    programStage.getProgramStageDataElements());
            dataElementUids.addAll(stageDataElementUids);
        }

        for (ProgramStageSection programStageSection : programStageSections) {
            Set<String> stageSectionElements = ModelUtils.toUidSet(
                    programStageSection.getProgramStageDataElements());
            dataElementUids.addAll(stageSectionElements);
        }
        return programStageDataElementInteractor.pull(dataElementUids).toBlocking().first();
    }
}
