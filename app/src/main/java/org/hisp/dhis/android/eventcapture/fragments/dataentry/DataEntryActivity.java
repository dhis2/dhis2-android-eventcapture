package org.hisp.dhis.android.eventcapture.fragments.dataentry;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextSwitcher;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.fragments.profile.ProfileFragment;

public class DataEntryActivity extends FragmentActivity {

    private ViewPager viewPager;
    private AppBarLayout appBarLayout;
    private TextSwitcher sectionLabelTextSwitcher;
    private ImageView previousSectionButton, nextSectionButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventdataentry);
        viewPager = (ViewPager) findViewById(R.id.viewpager_eventdataentry_fragment);
        appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);


        sectionLabelTextSwitcher = (TextSwitcher) findViewById(R.id.textswitcher_eventdataentry);
        previousSectionButton = (ImageView) findViewById(R.id.previous_section);
        nextSectionButton = (ImageView) findViewById(R.id.next_section);



        viewPager.setAdapter(new DataEntrySectionPageAdapter(getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(new DataEntrySectionPageChangedListener(
                previousSectionButton,
                nextSectionButton,
                sectionLabelTextSwitcher));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private class DataEntrySectionPageAdapter extends FragmentStatePagerAdapter {

        public DataEntrySectionPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Section " + position;
        }

        @Override
        public Fragment getItem(int position) {
            return new ProfileFragment();
        }

        @Override
        public int getCount() {
            return 7;
        }
    }

    private class DataEntrySectionPageChangedListener extends SimpleOnPageChangeListener implements View.OnClickListener{
        private ImageView nextSectionButton, previousSectionButton;
        private TextSwitcher sectionLabelTextSwitcher;
        private int lastPosition;
        private int numberOfPages;
        private Animation slideOut, slideIn;

        public DataEntrySectionPageChangedListener(ImageView previousSectionButton,
                                                   ImageView nextSectionButton,
                                                   TextSwitcher sectionLabelTextSwitcher) {
            super();
            this.previousSectionButton = previousSectionButton;
            this.nextSectionButton = nextSectionButton;
            this.sectionLabelTextSwitcher = sectionLabelTextSwitcher;
            this.slideOut = AnimationUtils.
                    loadAnimation(
                            sectionLabelTextSwitcher.getContext(), android.R.anim.slide_out_right);
            this.slideIn = AnimationUtils
                    .loadAnimation(
                            sectionLabelTextSwitcher.getContext(), android.R.anim.slide_in_left);

            this.sectionLabelTextSwitcher.setText(viewPager.getAdapter().getPageTitle(lastPosition));
            this.previousSectionButton.setVisibility(View.INVISIBLE);
            this.nextSectionButton.setVisibility(View.INVISIBLE);
            this.previousSectionButton.setOnClickListener(this);
            this.nextSectionButton.setOnClickListener(this);

            this.numberOfPages = viewPager.getAdapter().getCount() - 1;

            if(viewPager.getAdapter().getCount() > 0) {
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

            if(position == 0) {
                previousSectionButton.setVisibility(View.INVISIBLE);
                nextSectionButton.setVisibility(View.VISIBLE);
            }
            else if(position == numberOfPages) {
                previousSectionButton.setVisibility(View.VISIBLE);
                nextSectionButton.setVisibility(View.INVISIBLE);
            }
            else {
                previousSectionButton.setVisibility(View.VISIBLE);
                nextSectionButton.setVisibility(View.VISIBLE);
            }

            if(position >= lastPosition) {
                sectionLabelTextSwitcher.setInAnimation(slideOut);
                sectionLabelTextSwitcher.setOutAnimation(slideIn);
            }
            else {
                sectionLabelTextSwitcher.setInAnimation(slideIn);
                sectionLabelTextSwitcher.setOutAnimation(slideOut);
            }

            lastPosition = position;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.previous_section: {
                    if(viewPager.getCurrentItem() > 0) {
                        viewPager.setCurrentItem((viewPager.getCurrentItem() - 1), true);
                        viewPager.getAdapter().notifyDataSetChanged();
                        break;
                    }
                }
                case R.id.next_section: {
                    if(viewPager.getCurrentItem() < numberOfPages)
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                    viewPager.getAdapter().notifyDataSetChanged();
                    break;
                }
            }
        }
    }
}
