package org.hisp.dhis.android.eventcapture.fragments.dataentry;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.fragments.profile.ProfileFragment;

public class DataEntryActivity extends FragmentActivity {

    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    private AppBarLayout appBarLayout;
    private TextSwitcher backButtonTextSwitcher,
                         forwardButtonTextSwitcher,
                         sectionLabelTextSwitcher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventdataentry);
        viewPager = (ViewPager) findViewById(R.id.viewpager_eventdataentry_fragment);
        appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
//        backButtonTextSwitcher = (TextSwitcher) findViewById(R.id.back_button_textswitcher_eventdataentry);
//        forwardButtonTextSwitcher = (TextSwitcher) findViewById(R.id.forward_button_textswitcher_eventdataentry);
        sectionLabelTextSwitcher = (TextSwitcher) findViewById(R.id.textswitcher_eventdataentry);


        viewPager.addOnPageChangeListener(new DataEntrySectionPageChangedListener(
                        backButtonTextSwitcher,
                        forwardButtonTextSwitcher,
                        sectionLabelTextSwitcher));

        viewPager.setAdapter(new DataEntrySectionPageAdapter(getSupportFragmentManager()));
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

    private class DataEntrySectionPageChangedListener extends SimpleOnPageChangeListener {
        private TextSwitcher backButtonTextSwitcher,
                forwardButtonTextSwitcher,
                sectionLabelTextSwitcher;

        private int lastPosition;
        private final static int MAX_PAGES = 7;
        private Animation slideOut, slideIn;

        public DataEntrySectionPageChangedListener(TextSwitcher backButtonTextSwitcher,
                                                   TextSwitcher forwardButtonTextSwitcher,
                                                   TextSwitcher sectionLabelTextSwitcher) {
            super();
            this.backButtonTextSwitcher = backButtonTextSwitcher;
            this.forwardButtonTextSwitcher = forwardButtonTextSwitcher;
            this.sectionLabelTextSwitcher = sectionLabelTextSwitcher;
            this.slideOut = AnimationUtils.
                    loadAnimation(
                            sectionLabelTextSwitcher.getContext(), android.R.anim.slide_out_right);
            this.slideIn = AnimationUtils
                    .loadAnimation(
                            sectionLabelTextSwitcher.getContext(), android.R.anim.slide_in_left);
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            animateUiChanges(position);
        }

        private void animateUiChanges(int position) {
            sectionLabelTextSwitcher.setText("Section " + position);

//            switch (position) {
//                case 0: {
//                    backButtonTextSwitcher.setText("");
//                    forwardButtonTextSwitcher.setText(">");
//                }
//                case (MAX_PAGES): {
//                    backButtonTextSwitcher.setText("<");
//                    forwardButtonTextSwitcher.setText("");
//                }
//                default: {
//                    backButtonTextSwitcher.setText("<");
//                    forwardButtonTextSwitcher.setText(">");
//                }
//            }


            if(position >= lastPosition) {
                sectionLabelTextSwitcher.setInAnimation(slideOut);
                sectionLabelTextSwitcher.setOutAnimation(slideIn);

//                backButtonTextSwitcher.setInAnimation(slideOut);
//                backButtonTextSwitcher.setOutAnimation(slideIn);
//
//                forwardButtonTextSwitcher.setInAnimation(slideOut);
//                forwardButtonTextSwitcher.setOutAnimation(slideIn);
            }
            else {
                sectionLabelTextSwitcher.setInAnimation(slideIn);
                sectionLabelTextSwitcher.setOutAnimation(slideOut);

//                backButtonTextSwitcher.setInAnimation(slideIn);
//                backButtonTextSwitcher.setOutAnimation(slideOut);
//
//                forwardButtonTextSwitcher.setInAnimation(slideIn);
//                forwardButtonTextSwitcher.setOutAnimation(slideOut);
            }

            lastPosition = position;
        }

    }
}
