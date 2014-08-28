package com.huanghua.mysecret.ui;

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.Comment;
import com.huanghua.mysecret.bean.PushMessage;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.service.DateQueryService;
import com.huanghua.mysecret.util.CommonUtils;
import com.huanghua.mysecret.util.LocationUtil;
import com.huanghua.mysecret.view.SupportView;

/***
 * 发布秘密
 * 
 * @author huanghua
 * 
 */
public class WriteSecretActivity extends BaseActivity implements TextWatcher, View.OnClickListener {

    public static final int WRITE_TYPE_SECRET = 1;
    public static final int WRITE_TYPE_COMMENT = 2;
    public static final int WRITE_TYPE_REPLY_COMMENT = 3;
    public static final int WRITE_TYPE_PIC_SECRET = 4;
    public static final float PIC_SCALE_WIDTH = 60;

    private int mWriteType = WRITE_TYPE_SECRET;
    private TextView mContentsCountView = null;
    private TextView mTitle = null;
    private EditText mContents;
    private int mContentsCount = 300;
    private Handler mHandler = new Handler();
    private TextView mAddLocation = null;
    private boolean mShowLocation = true;
    private LocationUtil mLutil = null;
    private String mLocation = "";
    private TextView mParnetCommnet = null;
    private View mPhotoView = null;
    private ImageView mUserPhoto = null;
    private int mRandomHead = 0;
    private ImageView mAddPic = null;
    private Bitmap mPicBitmap = null;
    private String mPicFilePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_secret_view);

        mWriteType = getIntent().getIntExtra("write_type", WRITE_TYPE_SECRET);

        mTitle = (TextView) findViewById(R.id.title);
        mContents = (EditText) findViewById(R.id.contents);
        mContents.addTextChangedListener(this);
        mContentsCountView = (TextView) findViewById(R.id.contents_count);
        mAddLocation = (TextView) findViewById(R.id.add_location);
        mPhotoView = findViewById(R.id.photo_view);
        mPhotoView.setVisibility(View.GONE);
        mUserPhoto = (ImageView) findViewById(R.id.item_photo);
        mUserPhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mRandomHead = CommonUtils.getRandomHead();
                mUserPhoto.setImageResource(CommonUtils.HEAD_RESOURS[mRandomHead]);
            }
        });
        mAddPic = (ImageView) findViewById(R.id.add_pic);
        mAddPic.setVisibility(mWriteType == WRITE_TYPE_PIC_SECRET ? View.VISIBLE : View.GONE);
        mAddPic.setOnClickListener(this);
        mLutil = new LocationUtil(this);
        if (mWriteType == WRITE_TYPE_SECRET || mWriteType == WRITE_TYPE_PIC_SECRET) {
            mTitle.setText(R.string.publication_secret);
            mContents.setHint(R.string.write_secret_hint);
            mAddLocation.setVisibility(View.VISIBLE);
            mPhotoView.setVisibility(View.VISIBLE);
            mRandomHead = CommonUtils.getRandomHead();
        } else if (mWriteType == WRITE_TYPE_COMMENT) {
            mTitle.setText(R.string.publication_comment);
            mContents.setHint(R.string.write_comment_hint);
            mAddLocation.setVisibility(View.GONE);
        } else if (mWriteType == WRITE_TYPE_REPLY_COMMENT) {
            mTitle.setText(R.string.publication_comment);
            mContents.setHint(R.string.write_comment_hint);
            mAddLocation.setVisibility(View.GONE);
            Comment parentComment = (Comment) getIntent().getSerializableExtra(
                    "comment");
            User user = (User) getIntent().getSerializableExtra("toUser");
            mParnetCommnet = (TextView) findViewById(R.id.parentComment);
            mParnetCommnet.setText("//@" + user.getUsername() + ":" + parentComment.getContents());
            mParnetCommnet.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mLutil.isValidLocation()) {
            mAddLocation.setClickable(false);
            mShowLocation = false;
        }
        mLocation = mShowLocation ? mLutil.getAddress(mLutil.findLocation())
                : getString(R.string.unknown_address);
        mAddLocation.setText(mLocation);
        if (mWriteType == WRITE_TYPE_SECRET || mWriteType == WRITE_TYPE_PIC_SECRET) {
            mUserPhoto.setImageResource(CommonUtils.HEAD_RESOURS[mRandomHead]);
        }
    }

    public void onHideLocation(View v) {
        mShowLocation = !mShowLocation;
        mLocation = mShowLocation ? mLutil.getAddress(mLutil.findLocation())
                : getString(R.string.unknown_address);
        mAddLocation.setText(mLocation);
    }

    public void onPublication(final View v) {
        if (!checkNetwork()) {
            return;
        }
        if (checkUserLogin()) {
            return;
        }
        final String content = mContents.getText().toString();
        if (content != null && !"".endsWith(content)) {
            final Dialog dialog = CommonUtils.createLoadingDialog(this, getString(R.string.cominting));
            dialog.show();
            if (mWriteType == WRITE_TYPE_SECRET) {
                Secret s = new Secret();
                s.setContents(content);
                s.setUser(userManager.getCurrentUser());
                s.setStatus(0);
                s.setLocation(mShowLocation ? mLutil.findLocation() : null);
                s.setAddress(mLocation);
                s.setRandomHead(mRandomHead);
                s.save(this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        showLog("save secret success ");
                        dialog.dismiss();
                        ShowToastOld(R.string.publication_success);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 100);
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        showLog("save secret failure " + arg1);
                        dialog.dismiss();
                        ShowToast(R.string.publication_faile);
                        v.setClickable(true);
                    }
                });
            } else if (mWriteType == WRITE_TYPE_REPLY_COMMENT) {
                final User user = (User) getIntent().getSerializableExtra("toUser");
                Secret secret = (Secret) getIntent().getSerializableExtra(
                        "secret");
                secret.increment("commentCount");
                secret.update(this);
                final Comment parentComment = (Comment) getIntent().getSerializableExtra(
                        "comment");
                final Comment comment = new Comment();
                comment.setContents(content);
                comment.setFromUser(userManager.getCurrentUser());
                comment.setToUser(user);
                comment.setSecret(secret);
                comment.setParentComment(parentComment);
                comment.save(this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        showLog("save comment success ");
                        dialog.dismiss();
                        ShowToastOld(R.string.comment_publication_success);
                        sendBroadcast(new Intent(SupportView.DATE_COMMENT_CHANGER));
                        Intent intent = new Intent(DateQueryService.PUSH_ACTION_SEND_COMMENT);
                        intent.putExtra("comment", comment);
                        intent.putExtra("toUser", user);
                        intent.putExtra("type", PushMessage.PUSH_MESSAGE_TYPE_REPLY_COMMENT);
                        sendBroadcast(intent);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 100);
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        showLog("save comment failure " + arg1);
                        dialog.dismiss();
                        ShowToast(R.string.publication_faile);
                        v.setClickable(true);
                    }
                });
            } else if (mWriteType == WRITE_TYPE_PIC_SECRET) {
                File f = new File(mPicFilePath);
                final BmobFile bmobFile = new BmobFile(f);
                bmobFile.uploadblock(this, new UploadFileListener() {
                    @Override
                    public void onSuccess() {
                        Secret s = new Secret();
                        s.setContents(content);
                        s.setUser(userManager.getCurrentUser());
                        s.setStatus(0);
                        s.setPic(mPicBitmap == null ? null : bmobFile.getFileUrl());
                        s.setLocation(mShowLocation ? mLutil.findLocation() : null);
                        s.setAddress(mLocation);
                        s.setRandomHead(mRandomHead);
                        s.save(WriteSecretActivity.this, new SaveListener() {
                            @Override
                            public void onSuccess() {
                                showLog("save secret success ");
                                dialog.dismiss();
                                ShowToastOld(R.string.publication_success);
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                }, 100);
                            }

                            @Override
                            public void onFailure(int arg0, String arg1) {
                                showLog("save secret failure " + arg1);
                                dialog.dismiss();
                                ShowToast(R.string.publication_faile);
                                v.setClickable(true);
                            }
                        });
                    }
                    @Override
                    public void onProgress(Integer arg0) {
                    }
                    @Override
                    public void onFailure(int arg0, String arg1) {
                        showLog("save secret failure " + arg1);
                        dialog.dismiss();
                        ShowToast(R.string.publication_faile);
                        v.setClickable(true);
                    }
                });
            } else {
                final User user = (User) getIntent().getSerializableExtra("toUser");
                Secret secret = (Secret) getIntent().getSerializableExtra(
                        "secret");
                secret.increment("commentCount");
                secret.update(this);
                final Comment comment = new Comment();
                comment.setContents(content);
                comment.setFromUser(userManager.getCurrentUser());
                comment.setToUser(user);
                comment.setSecret(secret);
                comment.save(this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        showLog("save comment success ");
                        dialog.dismiss();
                        ShowToastOld(R.string.comment_publication_success);
                        sendBroadcast(new Intent(SupportView.DATE_COMMENT_CHANGER));
                        Intent intent = new Intent(DateQueryService.PUSH_ACTION_SEND_COMMENT);
                        intent.putExtra("comment", comment);
                        intent.putExtra("toUser", user);
                        intent.putExtra("type", PushMessage.PUSH_MESSAGE_TYPE_COMMENT);
                        sendBroadcast(intent);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 100);
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        showLog("save comment failure " + arg1);
                        dialog.dismiss();
                        ShowToast(R.string.publication_faile);
                        v.setClickable(true);
                    }
                });
            }
            v.setClickable(false);
        }
    }

    public void onCancel(View v) {
        onBackPressed();
    }

    public void onBackPressed(){
        String content = mContents.getText().toString();
        if ((content != null && content.length() > 0) || mPicBitmap != null) {
            showExitConfirmDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showExitConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle(mTitle.getText().toString())
                .setMessage(R.string.exit_confirm)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                WriteSecretActivity.super.onBackPressed();
                            }
                        }).setCancelable(false).show();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
        mContentsCount += count;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mContentsCount -= count;
    }

    @Override
    public void afterTextChanged(Editable s) {
        mContentsCountView.setText(mContentsCount + "");
    }

    @Override
    public void onClick(View v) {
        if (v == mAddPic) {
            showSelectPicDialog();
        }
    }

    private void showSelectPicDialog() {
        AlertDialog.Builder build = new AlertDialog.Builder(this)
                .setTitle(R.string.add_pic_dialog_title).setNegativeButton(android.R.string.cancel, null)
                .setCancelable(false);

        if (mPicBitmap != null) {
            build.setItems(R.array.add_pic_delete,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                            case 0:
                                Intent intent2 = new Intent(
                                        MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(intent2, 2);
                                break;
                            case 1:
                                Intent intent = new Intent(
                                        Intent.ACTION_GET_CONTENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.setType("image/*");
                                startActivityForResult(intent, 1);
                                break;
                            case 2:
                                mAddPic.setImageResource(R.drawable.add_pic);
                                mPicBitmap.isRecycled();
                                mPicBitmap = null;
                                break;
                            }
                        }
                    });
        } else {
            build.setItems(R.array.add_pic,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                            case 0:
                                Intent intent2 = new Intent(
                                        MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(intent2, 2);
                                break;
                            case 1:
                                Intent intent = new Intent(
                                        Intent.ACTION_GET_CONTENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.setType("image/*");
                                startActivityForResult(intent, 1);
                                break;
                            }
                        }
                    });
        }
        build.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPicBitmap != null && !mPicBitmap.isRecycled()) {
            mPicBitmap.recycle();
        }
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
        if (arg0 == 1) {
            if (arg1 == RESULT_OK) {
                int sdk_int = Build.VERSION.SDK_INT;
                if (sdk_int < 19) {

                } else {

                }
                Uri imageUri = arg2.getData();
                setPicForUri(imageUri);
            }
        } else if (arg0 == 2) {
            if (arg1 == RESULT_OK) {
                Bundle bundle = arg2.getExtras();
                mPicBitmap = (Bitmap) bundle.get("data");
                mAddPic.setImageBitmap(small(mPicBitmap, PIC_SCALE_WIDTH,
                        PIC_SCALE_WIDTH));
            }
        }
    }

    private void setPicForUri(Uri imageUri) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };

        Cursor cursor = getContentResolver().query(imageUri,
                filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        if (mPicBitmap != null && !mPicBitmap.isRecycled()) {
            mPicBitmap.isRecycled();
            mPicBitmap = null;
            mPicFilePath = null;
        }
        mPicFilePath = picturePath;
        mPicBitmap = BitmapFactory.decodeFile(picturePath);
        mAddPic.setImageBitmap(small(mPicBitmap, PIC_SCALE_WIDTH,
                PIC_SCALE_WIDTH));
    }

    private Bitmap small(Bitmap bitmap, float scaleW, float scaleH) {

        int oldW = bitmap.getWidth();
        int oldH = bitmap.getHeight();
        float newW = scaleW;
        float newH = scaleH;

        Matrix matrix = new Matrix();
        matrix.postScale(newW / oldW, newH / oldH);
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }
}
