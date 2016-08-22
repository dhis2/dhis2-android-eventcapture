package org.hisp.dhis.android.eventcapture.presenters;

import android.location.Location;

import org.hisp.dhis.android.eventcapture.LocationProvider;
import org.hisp.dhis.android.eventcapture.model.RxRulesEngine;
import org.hisp.dhis.android.eventcapture.views.FormSectionView;
import org.hisp.dhis.client.sdk.android.event.EventInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageSectionInteractor;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.FormSection;
import org.hisp.dhis.client.sdk.ui.models.Picker;
import org.hisp.dhis.client.sdk.utils.Logger;
import org.joda.time.DateTime;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

// TODO cache metadata and data in memory
public class FormSectionPresenterImpl implements FormSectionPresenter {
    private static final String TAG = FormSectionPresenterImpl.class.getSimpleName();
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private final ProgramStageInteractor programStageInteractor;
    private final ProgramStageSectionInteractor programStageSectionInteractor;

    private final EventInteractor eventInteractor;
    private final RxRulesEngine rxRuleEngine;

    private final Logger logger;

    private FormSectionView formSectionView;
    private CompositeSubscription subscription;

    private LocationProvider locationProvider;
    private boolean gettingLocation = false;

    public FormSectionPresenterImpl(ProgramStageInteractor programStageInteractor,
                                    ProgramStageSectionInteractor stageSectionInteractor,
                                    EventInteractor eventInteractor, RxRulesEngine rxRuleEngine,
                                    LocationProvider locationProvider, Logger logger) {
        this.programStageInteractor = programStageInteractor;
        this.programStageSectionInteractor = stageSectionInteractor;
        this.eventInteractor = eventInteractor;
        this.rxRuleEngine = rxRuleEngine;
        this.locationProvider = locationProvider;
        this.logger = logger;
    }

    @Override
    public void attachView(View view) {
        isNull(view, "View must not be null");
        formSectionView = (FormSectionView) view;
        if (gettingLocation) {
            formSectionView.setLocationButtonState(false);
        }
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
    public void createDataEntryForm(final String eventUid) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        subscription = new CompositeSubscription();
        subscription.add(eventInteractor.get(eventUid)
                .map(new Func1<Event, Event>() {
                    @Override
                    public Event call(Event event) {
                        // TODO consider refactoring rules-engine logic out of map function)
                        // synchronously initializing rule engine
                        rxRuleEngine.init(eventUid).toBlocking().first();

                        // compute initial RuleEffects
                        rxRuleEngine.notifyDataSetChanged();
                        return event;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Event>() {
                    @Override
                    public void call(Event event) {
                        isNull(event, String.format("Event with uid %s does not exist", eventUid));

                        if (formSectionView != null) {
                            formSectionView.showEventStatus(event.getStatus());
                        }

                        // fire next operations
                        subscription.add(showFormPickers(event));
                        subscription.add(showFormSections(event));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, null, throwable);
                    }
                }));
    }

    @Override
    public void saveEventDate(final String eventUid, final DateTime eventDate) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        subscription = new CompositeSubscription();
        subscription.add(eventInteractor.get(eventUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Event>() {
                    @Override
                    public void call(Event event) {
                        isNull(event, String.format("Event with uid %s does not exist", eventUid));

                        event.setEventDate(eventDate);

                        subscription.add(saveEvent(event));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, null, throwable);
                    }
                }));
    }

    @Override
    public void saveEventStatus(final String eventUid, final Event.EventStatus eventStatus) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        subscription = new CompositeSubscription();
        subscription.add(eventInteractor.get(eventUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Event>() {
                    @Override
                    public void call(Event event) {
                        isNull(event, String.format("Event with uid %s does not exist", eventUid));

                        event.setStatus(eventStatus);

                        subscription.add(saveEvent(event));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, null, throwable);
                    }
                }));
    }

    private void viewSetLocation(Location location) {
        if (formSectionView != null) {
            formSectionView.setLocation(location);
        }
    }

    @Override
    public void subscribeToLocations() {
        gettingLocation = true;
        locationProvider.locations()
                .timeout(31L, TimeUnit.SECONDS)
                .buffer(2L, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<List<Location>>() {
                            @Override
                            public void call(List<Location> locations) {
                                if (locations.isEmpty() || locations.get(0) == null) {
                                    return;
                                }
                                Location currentLocation = locations.get(0);
                                Location bestLocation = currentLocation;
                                float accuracyAverage = currentLocation.getAccuracy();
                                //go over the locations and find the best + keep average
                                for (int i = 1; i < locations.size(); i++) {
                                    currentLocation = locations.get(i);
                                    accuracyAverage += currentLocation.getAccuracy();
                                    if (locationProvider.isBetterLocation(currentLocation, bestLocation)) {
                                        bestLocation = currentLocation;
                                    }
                                }
                                accuracyAverage = accuracyAverage / locations.size();
                                // if accuracy doesn't improve and we have more than one, we have the best estimate.
                                if (Math.round(accuracyAverage)
                                        == Math.round(bestLocation.getAccuracy())
                                        && locations.size() > 1) {
                                    gettingLocation = false;
                                    viewSetLocation(bestLocation);
                                    locationProvider.stopUpdates();
                                }
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                if (throwable instanceof TimeoutException) {
                                    logger.d(TAG, "Rx subscribeToLocaitons() timed out.");
                                } else {
                                    logger.e(TAG, "subscribeToLocations() rx call :" + throwable);
                                }
                                gettingLocation = false;
                                viewSetLocation(null);
                                locationProvider.stopUpdates();
                            }
                        },
                        new Action0() {
                            @Override
                            public void call() {
                                logger.d(TAG, "onComplete");
                                gettingLocation = false;
                                viewSetLocation(null);
                                locationProvider.stopUpdates();
                            }
                        }
                );
        locationProvider.requestLocation();
    }

    @Override
    public void stopLocationUpdates() {
        locationProvider.stopUpdates();
    }

    private Subscription saveEvent(final Event event) {
        return eventInteractor.save(event)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean isSaved) {
                        if (isSaved) {
                            logger.d(TAG, "Successfully saved event " + event);
                        } else {
                            logger.d(TAG, "Failed to save event " + event);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Failed to save event " + event, throwable);
                    }
                });
    }

    private Subscription showFormPickers(final Event event) {
        final Program program = new Program();
        program.setUId(event.getProgram());

        return loadProgramStage(program)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ProgramStage>() {
                    @Override
                    public void call(ProgramStage programStage) {
                        if (formSectionView != null) {
                            String eventDate = event.getEventDate() != null ?
                                    event.getEventDate().toString(DATE_FORMAT) : "";
                            formSectionView.showReportDatePicker(
                                    programStage.getExecutionDateLabel(), eventDate);

                            if (programStage.isCaptureCoordinates()) {
                                String latitude = null;
                                String longitude = null;

                                if (event.getCoordinate() != null &&
                                        event.getCoordinate().getLatitude() != null) {
                                    latitude = String.valueOf(event.getCoordinate().getLatitude());
                                }

                                if (event.getCoordinate() != null &&
                                        event.getCoordinate().getLongitude() != null) {
                                    longitude = String.valueOf(event.getCoordinate().getLongitude());
                                }

                                formSectionView.showCoordinatesPicker(latitude, longitude);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Failed to fetch program stage", throwable);
                    }
                });
    }

    private Subscription showFormSections(final Event currentEvent) {
        final Program program = new Program();
        program.setUId(currentEvent.getProgram());

        return loadProgramStage(program)
                .map(new Func1<ProgramStage, SimpleEntry<Picker, List<FormSection>>>() {
                    @Override
                    public SimpleEntry<Picker, List<FormSection>> call(ProgramStage programStage) {
                        List<ProgramStageSection> stageSections = programStageSectionInteractor
                                .list(programStage).toBlocking().first();

                        String chooseSectionPrompt = null;
                        if (formSectionView != null) {
                            chooseSectionPrompt = formSectionView.getFormSectionLabel(
                                    FormSectionView.ID_CHOOSE_SECTION);
                        }

                        // fetching prompt from resources
                        Picker picker = new Picker.Builder()
                                .id(programStage.getUId())
                                .name(chooseSectionPrompt)
                                .build();

                        // transform sections
                        List<FormSection> formSections = new ArrayList<>();
                        if (stageSections != null && !stageSections.isEmpty()) {

                            // sort sections
                            Collections.sort(stageSections,
                                    ProgramStageSection.SORT_ORDER_COMPARATOR);

                            for (ProgramStageSection section : stageSections) {
                                formSections.add(new FormSection(
                                        section.getUId(), section.getDisplayName()));
                                picker.addChild(
                                        new Picker.Builder()
                                                .id(section.getUId())
                                                .name(section.getDisplayName())
                                                .parent(picker)
                                                .build());
                            }
                        }

                        return new SimpleEntry<>(picker, formSections);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<SimpleEntry<Picker, List<FormSection>>>() {
                    @Override
                    public void call(SimpleEntry<Picker, List<FormSection>> results) {
                        if (results != null && formSectionView != null) {
                            if (results.getValue() == null || results.getValue().isEmpty()) {
                                formSectionView.showFormDefaultSection(results.getKey().getId());
                            } else {
                                formSectionView.showFormSections(results.getValue());
                                formSectionView.setFormSectionsPicker(results.getKey());
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Form construction failed", throwable);
                    }
                });
    }

    private Observable<ProgramStage> loadProgramStage(final Program program) {
        return programStageInteractor.list(program)
                .map(new Func1<List<ProgramStage>, ProgramStage>() {
                    @Override
                    public ProgramStage call(List<ProgramStage> stages) {
                        // since this form is intended to be used in event capture
                        // and programs for event capture apps consist only from one
                        // and only one program stage, we can just retrieve it from the list
                        if (stages == null || stages.isEmpty()) {
                            logger.e(TAG, "Form construction failed. No program " +
                                    "stages are assigned to given program: " + program.getUId());
                            return null;
                        }

                        return stages.get(0);
                    }
                });
    }
}
