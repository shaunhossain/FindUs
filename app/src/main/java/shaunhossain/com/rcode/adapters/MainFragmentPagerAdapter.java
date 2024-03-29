package shaunhossain.com.rcode.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import shaunhossain.com.rcode.fragments.LoginFragment;
import shaunhossain.com.rcode.fragments.RegisterFragment;

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {

    public MainFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        if (position == 0) {
            return LoginFragment.newInstance();
        } else {
            return RegisterFragment.newInstance();
        }

    }

    @Override
    public int getCount() {
        return 2;
    }
}