package com.ghj.btcontrol;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;

public abstract class BaseFragmentActivity extends BaseActivity {

    private ArrayList<Fragment> fragmentStack = new ArrayList<>();

    abstract public int getFragmentID();


    public void addToFragmentStack(Fragment fragment) {
        this.fragmentStack.add(fragment);
    }

    // 현재 스택에서 해당 프래그먼트의 마지막을 삭제
    public void popFromFragmentStack(Fragment fragment) {
        for(int i = fragmentStack.size()-1; i >= 0; i--) {
            if(fragmentStack.get(i).getClass().getSimpleName().equals(fragment.getClass().getSimpleName())) {
                this.fragmentStack.remove(i);
                break;
            }
        }
    }

    // 현재 스택에서 마지막 프래그먼트를 삭제
    public void popFromFragmentStack() {
        if(fragmentStack.size() > 0) {
            this.fragmentStack.remove(fragmentStack.size()-1);
        }
    }

    // 현재 프래그먼트
    public Fragment getCurrentFragment() {
//        if(fragmentStack.size() == 0) {
//            return null;
//        }
//        return fragmentStack.get(fragmentStack.size()-1);
        return getChildFragmentManager().getFragments().get(0);
    }

    public NavController getNavController() {
        return Navigation.findNavController(this, getFragmentID());
    }

    public FragmentManager getChildFragmentManager() {
        return getSupportFragmentManager().findFragmentById(getFragmentID()).getChildFragmentManager();
    }

    public boolean popStack() {
        int cnt = getChildFragmentManager().getBackStackEntryCount();
        if(cnt > 0) {
            return getNavController().popBackStack();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        int cnt = getChildFragmentManager().getBackStackEntryCount();
        if(cnt > 0) {
            getNavController().popBackStack();
            return;
        }
        AppFinish();
    }
}
