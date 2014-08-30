package com.huanghua.mysecret.ui;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnMatrixChangedListener;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import uk.co.senab.photoview.PhotoViewAttacher.OnViewTapListener;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.util.ImageLoadOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * PhotoView
 * 
 * @author huanghua
 * 
 */
public class PhotoViewActivity extends BaseActivity {

    private ImageView mImageView;
    private PhotoViewAttacher mAttacher;
    private boolean mHasTouchPhoto = false;
    private boolean mFirstRun = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_view);
        mImageView = (ImageView) findViewById(R.id.iv_photo);
        Bitmap bit = (Bitmap) getIntent().getParcelableExtra("photo_bit");
        if (bit == null) {
            String uri = getIntent().getStringExtra("photo_uri");
            ImageLoader.getInstance().displayImage(uri, mImageView,
                    ImageLoadOptions.getOptions());
        } else {
            mImageView.setImageBitmap(bit);
        }
        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setOnMatrixChangeListener(mOatrixChangedListener);
        mAttacher.setOnPhotoTapListener(mOnPhotoTapListener);
        mAttacher.setOnViewTapListener(mViewTapListener);
        mRunFinishAnim = false;
        mFirstRun = true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_down_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAttacher.cleanup();
    }

    private OnMatrixChangedListener mOatrixChangedListener = new OnMatrixChangedListener() {
        @Override
        public void onMatrixChanged(RectF arg0) {
            if (!mFirstRun) {
                mHasTouchPhoto = true;
            } else {
                mFirstRun = false;
            }
        }
    };

    private OnViewTapListener mViewTapListener = new OnViewTapListener() {
        @Override
        public void onViewTap(View arg0, float arg1, float arg2) {
            finish();
        }
    };

    private OnPhotoTapListener mOnPhotoTapListener = new OnPhotoTapListener() {
        @Override
        public void onPhotoTap(View arg0, float arg1, float arg2) {
            if (!mHasTouchPhoto) {
                finish();
            }
            mHasTouchPhoto = false;
        }
    };
}
