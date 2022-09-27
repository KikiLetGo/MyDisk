package com.elexlab.mydisk.utils;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;


/**
 * Created by BruceYoung on 12/13/16.
 */
public class FragmentUtils {

    public static Fragment switchFragment(FragmentActivity fragmentActivity, int resId, Fragment from, Fragment to,boolean addToBack) {

        FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
        if(from != null){
            transaction.hide(from);
        }
        if (!to.isAdded()) {    // 先判断是否被add过

            transaction
                    .add(resId, to);

            if(addToBack){
                transaction.addToBackStack(null);
            }
            transaction.commit();
        } else {

            transaction.show(to).commit(); // 隐藏当前的fragment，显示下一个
        }
        return to;

    }
}
