package shaunhossain.com.rcode.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import shaunhossain.com.rcode.RcodeFragment;
import shaunhossain.com.rcode.fragments.ConversationsFragment;
import shaunhossain.com.rcode.fragments.MapFragment;

public class OnlineFragmentPagerAdapter extends FragmentPagerAdapter {

    public OnlineFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:

                return RcodeFragment.newInstance();
            case 1:
                return MapFragment.newInstance();
            case 2:
                return ConversationsFragment.newInstance();
            default:
                return RcodeFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}