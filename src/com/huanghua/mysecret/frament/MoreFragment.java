package com.huanghua.mysecret.frament;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huanghua.mysecret.R;

/***
 * 更多设置
 * 
 * @author huanghua
 * 
 */
public class MoreFragment extends FragmentBase implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_more, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        init();
    }

    private void init() {
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onClick(View v) {
    }

}
