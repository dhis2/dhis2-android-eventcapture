package org.hisp.dhis.android.eventcapture.presenters;

import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.android.eventcapture.views.fragments.FormSectionView;
import org.hisp.dhis.client.sdk.android.program.ProgramStageInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageSectionInteractor;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.ui.models.FormSection;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class FormSectionPresenterImpl implements FormSectionPresenter {
    private static final String TAG = FormSectionPresenterImpl.class.getSimpleName();

    private final ProgramStageInteractor programStageInteractor;
    private final ProgramStageSectionInteractor programStageSectionInteractor;
    private final Logger logger;

    private FormSectionView formSectionView;
    private CompositeSubscription subscription;

    public FormSectionPresenterImpl(ProgramStageInteractor programStageInteractor,
                                    ProgramStageSectionInteractor stageSectionInteractor,
                                    Logger logger) {
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
    public void createDataEntryForm(String organisationUnitId, String programId) {
        Program program = new Program();
        program.setUId(programId);

        subscription = new CompositeSubscription();
        subscription.add(programStageInteractor.list(program)
                .map(new Func1<List<ProgramStage>, String>() {
                    @Override
                    public String call(List<ProgramStage> stages) {
                        // since this form is intended to be used in event capture
                        // and programs for event capture apps consist only from one
                        // and only one program stage, we can just retrieve it from the list
                        if (stages != null && !stages.isEmpty()) {
                            ProgramStage programStage = stages.get(0);

                            if (programStage != null) {
                                return programStage.getDisplayName();
                            }
                        }

                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String label) {
                        if (formSectionView != null) {
                            formSectionView.showTitle(label);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Error retrieving ProgramStage", throwable);
                    }
                }));

        subscription.add(programStageInteractor.list(program)
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
                }));
    }
}
