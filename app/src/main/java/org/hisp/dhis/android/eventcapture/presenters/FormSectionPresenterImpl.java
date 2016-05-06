package org.hisp.dhis.android.eventcapture.presenters;

import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.android.eventcapture.views.activities.FormSectionView;
import org.hisp.dhis.client.sdk.android.organisationunit.OrganisationUnitInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageSectionInteractor;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.ui.models.FormSection;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class FormSectionPresenterImpl implements FormSectionPresenter {
    private static final String TAG = FormSectionPresenterImpl.class.getSimpleName();

    private final OrganisationUnitInteractor organisationUnitInteractor;
    private final ProgramInteractor programInteractor;
    private final ProgramStageInteractor programStageInteractor;
    private final ProgramStageSectionInteractor programStageSectionInteractor;
    private final Logger logger;

    private FormSectionView formSectionView;
    private CompositeSubscription subscription;

    public FormSectionPresenterImpl(OrganisationUnitInteractor organisationUnitInteractor,
                                    ProgramInteractor programInteractor,
                                    ProgramStageInteractor programStageInteractor,
                                    ProgramStageSectionInteractor stageSectionInteractor,
                                    Logger logger) {
        this.organisationUnitInteractor = organisationUnitInteractor;
        this.programInteractor = programInteractor;
        this.programStageInteractor = programStageInteractor;
        this.programStageSectionInteractor = stageSectionInteractor;
        this.logger = logger;
    }

    @Override
    public void attachView(View view) {
        isNull(view, "View must not be null");
        formSectionView = (FormSectionView) view;
    }

    @Override
    public void detachView() {
        formSectionView = null;
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public void createDataEntryForm(final String organisationUnitId, final String programId) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        subscription = new CompositeSubscription();
        subscription.add(showTitle(organisationUnitId));
        subscription.add(showSubtitle(programId));
        subscription.add(showFormSections(programId));
    }

    private Subscription showTitle(final String organisationUnitId) {
        return organisationUnitInteractor.get(organisationUnitId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<OrganisationUnit>() {
                    @Override
                    public void call(OrganisationUnit organisationUnit) {
                        if (formSectionView != null && organisationUnit != null) {
                            formSectionView.showTitle(organisationUnit.getDisplayName());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "OrganisationUnit with id: " + organisationUnitId +
                                " was not found", throwable);
                    }
                });
    }

    private Subscription showSubtitle(final String programId) {
        return programInteractor.get(programId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Program>() {
                    @Override
                    public void call(Program program) {
                        if (formSectionView != null && program != null) {
                            formSectionView.showSubtitle(program.getDisplayName());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Program with id: " + programId +
                                " was not found", throwable);
                    }
                });
    }

    private Subscription showFormSections(final String programId) {
        Program program = new Program();
        program.setUId(programId);

        return programStageInteractor.list(program)
                .switchMap(new Func1<List<ProgramStage>, Observable<List<ProgramStageSection>>>() {

                    @Override
                    public Observable<List<ProgramStageSection>> call(List<ProgramStage> stages) {
                        // since this form is intended to be used in event capture
                        // and programs for event capture apps consist only from one
                        // and only one program stage, we can just retrieve it from the list
                        if (stages != null && !stages.isEmpty()) {
                            return programStageSectionInteractor.list(stages.get(0));
                        }

                        return null;
                    }
                })
                .map(new Func1<List<ProgramStageSection>, List<FormSection>>() {
                    @Override
                    public List<FormSection> call(List<ProgramStageSection> programStageSections) {
                        List<FormSection> formSections = new ArrayList<>();

                        for (ProgramStageSection section : programStageSections) {
                            formSections.add(new FormSection(
                                    section.getUId(), section.getDisplayName()));
                        }

                        return formSections;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<FormSection>>() {
                    @Override
                    public void call(List<FormSection> formSections) {
                        logger.d(TAG, "ProgramStageSections are loaded: " + formSections);

                        if (formSectionView != null) {
                            formSectionView.showFormSections(formSections);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.d(TAG, "Form construction failed", throwable);
                    }
                });
    }
}
