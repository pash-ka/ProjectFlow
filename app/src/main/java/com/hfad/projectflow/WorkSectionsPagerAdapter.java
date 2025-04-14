package com.hfad.projectflow;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import androidx.viewpager2.adapter.FragmentStateAdapter;

public class WorkSectionsPagerAdapter extends FragmentStateAdapter {

    private FragmentActivity activity;
    private int userId;
    private int projectId;

    public WorkSectionsPagerAdapter(@NonNull FragmentActivity fragmentActivity, int userId, int projectId) {
        super(fragmentActivity);
        this.activity = fragmentActivity;
        this.userId = userId;
        this.projectId = projectId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return DocumentationFragment.newInstance(userId, projectId);
            case 1:
                return WhiteBoardFragment.newInstance(userId, projectId);
        }
        return new Fragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return activity.getResources().getText(R.string.documentation);
            case 1:
                return activity.getResources().getText(R.string.whiteboard);
        }
        return null;
    }

}
