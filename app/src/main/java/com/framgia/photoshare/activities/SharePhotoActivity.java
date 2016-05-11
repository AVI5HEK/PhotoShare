package com.framgia.photoshare.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.framgia.photoshare.R;
import com.framgia.photoshare.utils.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SharePhotoActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
    private static final int PICK_IMAGE_ACTIVITY_REQUEST_CODE = 2;
    private static final String EXTRA_DATA = "data";
    private static final String IMAGE_EXTENSION = ".jpg";
    private static final int IMAGE_QUALITY = 90;
    private static final String EXTRA_PACKAGE_FACEBOOK = "com.facebook.katana";
    private static final String EXTRA_PACKAGE_TWITTER = "com.twitter.android";
    private static final String EXTRA_PACKAGE_PINTEREST = "com.pinterest";
    private static final String EXTRA_PACKAGE_INSTAGRAM = "com.instagram.android";
    private static final String EXTRA_PACKAGE_WHATSAPP = "com.whatsapp";
    @InjectView(R.id.button_share_photo)
    Button mButtonSharePhoto;
    @InjectView(R.id.image_photo)
    ImageView mImagePhoto;
    @InjectView(R.id.button_share)
    Button mButtonShare;
    @InjectView(R.id.text_caption)
    TextView mTextCaption;
    @InjectView(R.id.et_caption)
    EditText mEtCaption;
    private ShareDialog mShareDialog;
    private Bitmap mImageBitmap;
    private CallbackManager mCallbackManager;
    private SessionManager mSession;
    private Uri mImageUri;
    PackageManager mPackageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFacebookSdk();
        setContentView(R.layout.activity_share_photo);
        ButterKnife.inject(this);
        mButtonSharePhoto.setOnClickListener(this);
        mButtonShare.setOnClickListener(this);
        mShareDialog = new ShareDialog(this);
        mSession = new SessionManager(getApplicationContext());
        mPackageManager = getPackageManager();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_share_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logOut() {
        LoginManager.getInstance().logOut();
        mSession.logoutUser();
    }

    protected void initializeFacebookSdk() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_share_photo:
                showOptionDialog();
                break;
            case R.id.button_share:
                showAppDialog();
                break;
            default:
                break;
        }
    }

    private void showAppDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_apps);
        dialog.setTitle(getString(R.string.dialog_share_from));
        dialog.findViewById(R.id.image_facebook).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                shareToApp(EXTRA_PACKAGE_FACEBOOK);
            }
        });
        dialog.findViewById(R.id.image_twitter).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                shareToApp(EXTRA_PACKAGE_TWITTER);
            }
        });
        dialog.findViewById(R.id.image_pinterest).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                shareToApp(EXTRA_PACKAGE_PINTEREST);
            }
        });
        dialog.findViewById(R.id.image_instagram).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                shareToApp(EXTRA_PACKAGE_INSTAGRAM);
            }
        });
        dialog.findViewById(R.id.image_whatsapp).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                shareToApp(EXTRA_PACKAGE_WHATSAPP);
            }
        });
        dialog.show();
    }

    private void shareToFacebook(Bitmap imageBitmap) {
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(imageBitmap)
                .setCaption(mEtCaption.getText().toString())
                .build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();
        mShareDialog.show(content);
    }

    private void shareToApp(String packageName) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String text = mEtCaption.getText().toString();
            mPackageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
            intent.setPackage(packageName);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_STREAM, mImageUri);
            if ((packageName.equals(EXTRA_PACKAGE_FACEBOOK)
                    || packageName.equals(EXTRA_PACKAGE_INSTAGRAM)
                    || packageName.equals(EXTRA_PACKAGE_PINTEREST)) && !TextUtils.isEmpty
                    (mEtCaption.getText().toString())) {
                showToast(getString(R.string.cannot_share_text));
            } else {
                intent.putExtra(Intent.EXTRA_TEXT, text);
            }
            startActivity(intent);

        } catch (PackageManager.NameNotFoundException e) {
            showToast(getString(R.string.app_not_installed));
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private void showOptionDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_share_photo);
        dialog.setTitle(getString(R.string.dialog_share_from));
        dialog.findViewById(R.id.button_dialog_camera).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                captureFromCamera();
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.button_dialog_gallery).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFromGallery();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    private void captureFromCamera() {
        startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            mImageUri = data.getData();
            switch (requestCode) {
                case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                    if (resultCode == RESULT_OK) {
                        showImage(data.getData());
                        toggleViewVisibility();
                        onCapture(data);
                    }
                    break;
                case PICK_IMAGE_ACTIVITY_REQUEST_CODE:
                    if (resultCode == RESULT_OK) {
                        showImage(data.getData());
                        toggleViewVisibility();
                        onSelect(data);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void showImage(Uri imageUri) {
        Glide.with(getApplicationContext())
                .load(imageUri)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .fitCenter()
                .into(mImagePhoto);
    }

    private void onCapture(Intent data) {
        mImageBitmap = (Bitmap) data.getExtras().get(EXTRA_DATA);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        mImageBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + IMAGE_EXTENSION);
        FileOutputStream fileOutputStream;
        try {
            destination.createNewFile();
            fileOutputStream = new FileOutputStream(destination);
            fileOutputStream.write(bytes.toByteArray());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onSelect(Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = managedQuery(selectedImageUri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        String selectedImagePath = cursor.getString(column_index);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectedImagePath, options);
        final int REQUIRED_SIZE = 200;
        int scale = 1;
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                && options.outHeight / scale / 2 >= REQUIRED_SIZE) {
            scale *= 2;
        }
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        mImageBitmap = BitmapFactory.decodeFile(selectedImagePath, options);
    }

    private void toggleViewVisibility() {
        mButtonSharePhoto.setText(getString(R.string.share_another));
        if (mTextCaption.getVisibility() != View.VISIBLE) {
            mTextCaption.setVisibility(View.VISIBLE);
        }
        if (mEtCaption.getVisibility() != View.VISIBLE) {
            mEtCaption.setVisibility(View.VISIBLE);
        }
        if (mButtonShare.getVisibility() != View.VISIBLE) {
            mButtonShare.setVisibility(View.VISIBLE);
        }
    }
}
