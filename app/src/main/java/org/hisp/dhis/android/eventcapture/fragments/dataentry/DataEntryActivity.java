package org.hisp.dhis.android.eventcapture.fragments.dataentry;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextSwitcher;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.fragments.itemlist.ItemListFragment;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;

import java.util.List;

public class DataEntryActivity extends FragmentActivity implements IDataEntryView {
    private String organisationUnitUid;
    private String programUid;
    private String eventUid;
    private Event event;
    private List<ProgramStageSection> programStageSections;

    private ViewPager viewPager;
    private TextSwitcher sectionLabelTextSwitcher;
    private ImageView previousSectionButton, nextSectionButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventdataentry);

        Intent intent = getIntent();
        organisationUnitUid = intent.getStringExtra(ItemListFragment.ORG_UNIT_UID);
        programUid = intent.getStringExtra(ItemListFragment.PROGRAM_UID);
        eventUid = intent.getStringExtra(ItemListFragment.EVENT_UID);

        Log.d("ORGUNIT DATAENTRY", organisationUnitUid);
        Log.d("PROGRAM DATAENTRY", programUid);
        Log.d("EVENT DATAENTRY", eventUid);

        viewPager = (ViewPager) findViewById(R.id.viewpager_eventdataentry_fragment);
        sectionLabelTextSwitcher = (TextSwitcher) findViewById(R.id.textswitcher_eventdataentry);
        previousSectionButton = (ImageView) findViewById(R.id.previous_section);
        nextSectionButton = (ImageView) findViewById(R.id.next_section);

        IDataEntryPresenter dataEntryPresenter = new DataEntryPresenter(this);
        dataEntryPresenter.onCreate();
        dataEntryPresenter.listProgramStageSections(programUid);

        if(eventUid.equals("")) {
            dataEntryPresenter.createNewEvent(organisationUnitUid, programUid);
        }
        else {
            event = dataEntryPresenter.getEvent(eventUid);  //doesn't work when we have dummy data
        }



    }
    @Override
    @UiThread
    public void initializeViewPager(List<ProgramStageSection> programStageSections) {
        this.programStageSections = programStageSections;
        if(!programStageSections.isEmpty()) {
            viewPager.setAdapter(new DataEntrySectionPageAdapter(getSupportFragmentManager()));
            viewPager.addOnPageChangeListener(new DataEntrySectionPageChangedListener(
                    previousSectionButton,
                    nextSectionButton,
                    sectionLabelTextSwitcher));
        }
        else {
            getSupportFragmentManager().beginTransaction()
                    .add(EventDataEntryFragment.newInstance(
                            event.getUId(),
                            event.getProgramStageId()),
                            "EventDataEntryFragment").commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void setEvent(Event event) {
        this.event = event;
    }

    private class DataEntrySectionPageAdapter extends FragmentStatePagerAdapter {

        public DataEntrySectionPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(!programStageSections.isEmpty()) {
                return programStageSections.get(position).getDisplayName();
            }
            else return "Program name";
        }

        @Override
        public Fragment getItem(int position) {
            if(event != null) {
                return EventDataEntryFragment.newInstance(event.getUId(), programStageSections.get(position).getUId());
            }
            else return EventDataEntryFragment.newInstance(eventUid, programStageSections.get(position).getUId());
        }

        @Override
        public int getCount() {
            if(programStageSections == null || programStageSections.isEmpty())
                return 0;
            else {
                return programStageSections.size();
            }
        }
    }

    private class DataEntrySectionPageChangedListener extends SimpleOnPageChangeListener implements View.OnClickListener {
        private ImageView nextSectionButton, previousSectionButton;
        private TextSwitcher sectionLabelTextSwitcher;
        private int lastPosition;
        private int numberOfPages;
        private Animation slideOutRight, slideInRight, slideInLeft, slideOutLeft;

        public DataEntrySectionPageChangedListener(ImageView previousSectionButton,
                                                   ImageView nextSectionButton,
                                                   TextSwitcher sectionLabelTextSwitcher) {
            super();
            this.previousSectionButton = previousSectionButton;
            this.nextSectionButton = nextSectionButton;
            this.sectionLabelTextSwitcher = sectionLabelTextSwitcher;

            this.slideOutRight = AnimationUtils.
                    loadAnimation(
                            sectionLabelTextSwitcher.getContext(), android.R.anim.slide_out_right);
            this.slideInLeft = AnimationUtils
                    .loadAnimation(
                            sectionLabelTextSwitcher.getContext(), android.R.anim.slide_in_left);

            this.slideOutLeft = AnimationUtils
                    .loadAnimation(
                            sectionLabelTextSwitcher.getContext(), R.anim.slide_out_left);

            this.slideInRight = AnimationUtils
                    .loadAnimation(
                            sectionLabelTextSwitcher.getContext(), R.anim.slide_in_right);

            this.sectionLabelTextSwitcher.setText(viewPager.getAdapter().getPageTitle(lastPosition));
            this.previousSectionButton.setVisibility(View.INVISIBLE);
            this.nextSectionButton.setVisibility(View.INVISIBLE);
            this.previousSectionButton.setOnClickListener(this);
            this.nextSectionButton.setOnClickListener(this);

            this.numberOfPages = (viewPager.getAdapter().getCount() - 1);

            if (viewPager.getAdapter().getCount() > 0) {
                this.nextSectionButton.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            animateUiChanges(position);

        }

        private void animateUiChanges(int position) {
            sectionLabelTextSwitcher.setText(viewPager.getAdapter().getPageTitle(position));

            if (position == 0) {
                previousSectionButton.setVisibility(View.INVISIBLE);
                nextSectionButton.setVisibility(View.VISIBLE);
            } else if (position == numberOfPages) {
                previousSectionButton.setVisibility(View.VISIBLE);
                nextSectionButton.setVisibility(View.INVISIBLE);
            } else {
                previousSectionButton.setVisibility(View.VISIBLE);
                nextSectionButton.setVisibility(View.VISIBLE);
            }

            if (position > lastPosition) {
                //change these:
                sectionLabelTextSwitcher.setOutAnimation(slideOutLeft);
                sectionLabelTextSwitcher.setInAnimation(slideInRight);
            } else if(position < lastPosition){
                sectionLabelTextSwitcher.setInAnimation(slideInLeft);
                sectionLabelTextSwitcher.setOutAnimation(slideOutRight);
            }

            lastPosition = position;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.previous_section: {
                    if (viewPager.getCurrentItem() > 0) {
                        viewPager.setCurrentItem((viewPager.getCurrentItem() - 1), true);
                        viewPager.getAdapter().notifyDataSetChanged();
                        break;
                    }
                }
                case R.id.next_section: {
                    if (viewPager.getCurrentItem() < numberOfPages)
                        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                    viewPager.getAdapter().notifyDataSetChanged();
                    break;
                }
            }
        }
    }
}
