package org.hisp.dhis.android.eventcapture.fragments.dataentry;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.fragments.profile.ProfileFragment;

public class DataEntryActivity extends FragmentActivity {

    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    private AppBarLayout appBarLayout;
    private TabLayout tabLayout;
    private PagerTitleStrip pagerTitleStrip;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventdataentry);
        viewPager = (ViewPager) findViewById(R.id.viewpager_eventdataentry_fragment);
        appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        pagerTitleStrip = (PagerTitleStrip) findViewById(R.id.pagerTitleStrip);
        pagerTitleStrip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
//        tabLayout = (TabLayout) findViewById(R.id.tabLayout);


        viewPager.setAdapter(new DataEntrySectionPageAdapter(getSupportFragmentManager()));
//        tabLayout.setupWithViewPager(viewPager);
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
}
