package com.huanghua.mysecret.frament;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;

import com.huanghua.mysecret.CustomApplcation;
import com.huanghua.mysecret.R;
import com.huanghua.mysecret.adapter.base.ChoicenessListAdapter;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.load.DateLoadThreadManager;
import com.huanghua.mysecret.service.DateQueryService;
import com.huanghua.mysecret.ui.BaseActivity;
import com.huanghua.mysecret.ui.WriteCommentActivity;
import com.huanghua.mysecret.ui.WriteSecretActivity;
import com.huanghua.mysecret.util.LocationUtil;
import com.huanghua.mysecret.view.xlist.XListView;
import com.huanghua.mysecret.view.xlist.XListView.IXListViewListener;

/***
 * 附近秘密
 * 
 * @author huanghua
 * 
 */
public class NearSecretFragment extends FragmentBase implements
        IXListViewListener, View.OnClickListener, OnItemClickListener {

    private XListView mListChoiceness;
    private ChoicenessListAdapter mChoicenessAdapter;
    private List<Secret> mSecretList = new ArrayList<Secret>();
    private BmobQuery<Secret> mQuerySecret = null;
    private ImageButton mWriteSecret = null;
    private boolean mQueryIng = false;
    private View mLoadView = null;
    private ImageView mLoadImage = null;
    private TextView mTitle = null;
    private static final int LIST_DEFALUT_LIMIT = 20;
    private int mListPage = 1;
    private int mSecretCount = 0;
    private TextView mEmptyText = null;
    private LocationUtil mLutil = null;
    private boolean mHasLocation = true;
    private View mTopView = null;

    private FindListener<Secret> mFindSecretListener = new FindListener<Secret>() {
        @Override
        public void onSuccess(List<Secret> list) {
            showLog("query near secret success:" + list.size());
            mChoicenessAdapter.setList(list);
            refreshPull();
            if (mLoadView.getVisibility() == View.VISIBLE) {
                mLoadView.setVisibility(View.GONE);
                mLoadImage.clearAnimation();
            }
            if (list.size() < mSecretCount) {
                mListChoiceness.setPullLoadEnable(true);
            } else {
                mListChoiceness.setPullLoadEnable(false);
            }
            if (list.size() == 0) {
                mEmptyText.setVisibility(View.VISIBLE);
                mEmptyText.setText(R.string.empty_near_secret);
                if (mLoadView.getVisibility() == View.VISIBLE) {
                    mLoadView.setVisibility(View.GONE);
                    mLoadImage.clearAnimation();
                }
            } else {
                mEmptyText.setVisibility(View.GONE);
            }
            mListChoiceness.setPullRefreshEnable(true);
        }

        @Override
        public void onError(int arg0, String arg1) {
            showLog("query near secret error:" + arg1 + " arg0:" + arg0);
            if (mListPage > 1) {
                mListPage--;
            }
            if (arg0 == 9010) {
                ShowToast(R.string.no_check_network);
                mEmptyText.setText(R.string.no_check_network);
            }
            if (mLoadView.getVisibility() == View.VISIBLE) {
                mLoadView.setVisibility(View.GONE);
                mLoadImage.clearAnimation();
            }
            if (mChoicenessAdapter.getList() == null
                    || mChoicenessAdapter.getList().size() == 0) {
                mEmptyText.setVisibility(View.VISIBLE);
            } else {
                mEmptyText.setVisibility(View.GONE);
            }
            refreshPull();
            mListChoiceness.setPullRefreshEnable(true);
        }
    };

    private CountListener mCountListener = new CountListener() {
        @Override
        public void onSuccess(int arg0) {
            mSecretCount = arg0;
            if (mSecretCount > mListPage * LIST_DEFALUT_LIMIT) {
                mListChoiceness.setPullLoadEnable(true);
            } else {
                mListChoiceness.setPullLoadEnable(false);
            }
        }

        @Override
        public void onFailure(int arg0, String arg1) {
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_nearsecret, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        init();
    }

    private void init() {
        mWriteSecret = (ImageButton) findViewById(R.id.write_secret);
        mWriteSecret.setOnClickListener(this);

        mLoadView = findViewById(R.id.load_view);
        mLoadImage = (ImageView) findViewById(R.id.load_img);
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                getActivity(), R.anim.loading_animation);
        mLoadImage.startAnimation(hyperspaceJumpAnimation);

        mListChoiceness = (XListView) findViewById(R.id.list_choiceness);
        mListChoiceness.setPullLoadEnable(false);
        mListChoiceness.setPullRefreshEnable(true);
        mListChoiceness.setXListViewListener(this);
        mListChoiceness.pullRefreshing();
        mChoicenessAdapter = new ChoicenessListAdapter(getActivity(),
                mSecretList);
        mChoicenessAdapter.setNearSecret(true);
        mListChoiceness.setAdapter(mChoicenessAdapter);
        mListChoiceness.setOnItemClickListener(this);
        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setText(R.string.tab_nearby);
        mTitle.setVisibility(View.VISIBLE);
        mEmptyText = (TextView) findViewById(R.id.empty_text);
        mEmptyText.setOnClickListener(this);
        mLutil = new LocationUtil(getActivity());
        mTopView = findViewById(R.id.top_view);
        mTopView.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mHasLocation = true;
        if (!mLutil.isValidLocation()) {
            mEmptyText.setText(R.string.empty_location_no);
            mEmptyText.setVisibility(View.VISIBLE);
            mHasLocation = false;
            if (mLoadView.getVisibility() == View.VISIBLE) {
                mLoadView.setVisibility(View.GONE);
                mLoadImage.clearAnimation();
            }
        }
        if (mQuerySecret == null) {
            mQuerySecret = new BmobQuery<Secret>();
            mQuerySecret.order("-createdAt");
            mQuerySecret.include("user");
            mQuerySecret.addWhereExists("user");
            mQuerySecret.addWhereDoesNotExists("pic");
            mListPage = 1;
            BmobGeoPoint mGeoPoint = new BmobGeoPoint(mLutil.findLocation()
                    .getLongitude(), mLutil.findLocation().getLatitude());
            mQuerySecret
                    .addWhereWithinKilometers(
                            "location",
                            mGeoPoint,
                            Double.parseDouble(getString(R.string.def_near_secret_kilometers)));
            mQuerySecret.setLimit(mListPage * LIST_DEFALUT_LIMIT);
            mQuerySecret.setCachePolicy(CachePolicy.CACHE_ELSE_NETWORK);
            if (mHasLocation) {
                mQuerySecret.findObjects(getActivity(), mFindSecretListener);
                mQuerySecret.count(getActivity(), Secret.class, mCountListener);
            }
            // toTopSelect();
        }
    }

    @Override
    public void onRefresh() {
        showLog("choiceness onRefresh");
        if (!mQueryIng) {
            mListPage = 1;
            mQueryIng = true;
            DateLoadThreadManager.removeAllTask();
            mQuerySecret.setLimit(mListPage * LIST_DEFALUT_LIMIT);
            BmobGeoPoint mGeoPoint = new BmobGeoPoint(mLutil.findLocation()
                    .getLongitude(), mLutil.findLocation().getLatitude());
            mQuerySecret
                    .addWhereWithinKilometers(
                            "location",
                            mGeoPoint,
                            Double.parseDouble(getString(R.string.def_near_secret_kilometers)));
            mQuerySecret.setCachePolicy(CachePolicy.NETWORK_ONLY);
            mQuerySecret.findObjects(getActivity(), mFindSecretListener);
        }
    }

    @Override
    public void onLoadMore() {
        showLog("choiceness onLoadMore:" + mListChoiceness.getPullLoading());
        if (!mQueryIng) {
            mListPage++;
            mQueryIng = true;
            mQuerySecret.setLimit(mListPage * LIST_DEFALUT_LIMIT);
            mQuerySecret.setCachePolicy(CachePolicy.NETWORK_ONLY);
            mQuerySecret.findObjects(getActivity(), mFindSecretListener);
        }
    }

    private void refreshPull() {
        showLog("refreshPull :" + mListChoiceness.getPullRefreshing());
        if (mListChoiceness.getPullRefreshing()) {
            mListChoiceness.stopRefresh();
            if (DateQueryService.sHasNewSecret && mQueryIng) {
                DateQueryService.sHasNewSecret = false;
                getActivity().sendBroadcast(
                        new Intent(DateQueryService.QUERY_NEW_SECRTE_ACTION));
            }
        }
        if (mListChoiceness.getPullLoading()) {
            mListChoiceness.stopLoadMore();
        }
        mQueryIng = false;
    }

    @Override
    public void onClick(View v) {
        if (v == mEmptyText) {
            ((CustomApplcation) getActivity().getApplication()).mLocationClient.requestLocation();
            if (mLutil.isValidLocation()) {
                mListPage = 1;
                mQuerySecret.setLimit(mListPage * LIST_DEFALUT_LIMIT);
                BmobGeoPoint mGeoPoint = new BmobGeoPoint(mLutil.findLocation()
                        .getLongitude(), mLutil.findLocation().getLatitude());
                mQuerySecret
                        .addWhereWithinKilometers(
                                "location",
                                mGeoPoint,
                                Double.parseDouble(getString(R.string.def_near_secret_kilometers)));
                mQuerySecret.findObjects(getActivity(), mFindSecretListener);
                mQuerySecret.setCachePolicy(CachePolicy.NETWORK_ONLY);
                mQuerySecret.count(getActivity(), Secret.class, mCountListener);
            }
        } else if (v == mTopView) {
            if (mListChoiceness != null && mChoicenessAdapter.getCount() != 0) {
                mListChoiceness.setSelection(0);
            }
        } else if (((BaseActivity) getActivity()).checkUserLogin()) {
            return;
        }
        if (v == mWriteSecret) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), WriteSecretActivity.class);
            startAnimActivity(intent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Secret s = mChoicenessAdapter.getList().get(position - 1);
        if (s != null) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), WriteCommentActivity.class);
            intent.putExtra("secret", s);
            startAnimActivity(intent);
        }
    }

    public void toTopSelect() {
        if (mListChoiceness != null && mListChoiceness.getCount() > 0
                && !mListChoiceness.getPullRefreshing()) {
            mListChoiceness.setSelection(0);
            mListChoiceness.pullRefreshing();
            mListChoiceness.startRefresh();
        }
    }

    public void refreshDate() {
        mQuerySecret = new BmobQuery<Secret>();
        mQuerySecret.order("-createdAt");
        mQuerySecret.include("user");
        mQuerySecret.addWhereExists("user");
        mQuerySecret.addWhereDoesNotExists("pic");
        mListPage = 1;
        BmobGeoPoint mGeoPoint = new BmobGeoPoint(mLutil.findLocation()
                .getLongitude(), mLutil.findLocation().getLatitude());
        mQuerySecret.addWhereWithinKilometers("location", mGeoPoint, Double
                .parseDouble(getString(R.string.def_near_secret_kilometers)));
        mQuerySecret.setLimit(mListPage * LIST_DEFALUT_LIMIT);
        mQuerySecret.setCachePolicy(CachePolicy.NETWORK_ONLY);
        mQuerySecret.findObjects(getActivity(), mFindSecretListener);
        mQuerySecret.count(getActivity(), Secret.class, mCountListener);
    }
}
