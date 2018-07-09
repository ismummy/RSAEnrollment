package com.ismummy.rsaenrollment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.ismummy.rsaenrollment.bases.BaseActivity;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;

public class CaturingActivity extends BaseActivity {
    @BindView(R.id.iv_signature)
    ImageView signature;
    @BindView(R.id.iv_picture)
    ImageView picture;

    private File signatureFile;
    private Uri mCropImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caturing);
    }

    @OnClick(R.id.btn_next)
    void nextClicked() {
        startActivity(new Intent(this, BiometricCapturingActivity.class));
    }

    @OnClick(R.id.signature_layout)
    void signatureClick() {
        Intent intent = new Intent(this, SigningPad.class);
        startActivityForResult(intent, 10);
    }
    @OnClick(R.id.picture_layout)
    void pictureClick(){
        selectImage();
    }

    @SuppressLint("NewApi")
    public void selectImage() {
        if (CropImage.isExplicitCameraPermissionRequired(this)) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
        } else {
            CropImage.startPickImageActivity(this);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.startPickImageActivity(this);
            } else {
                toast("Cancelling, required permissions are not granted");
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);
            } else {
                toast("Cancelling, required permissions are not granted");
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setAspectRatio(1, 1)
                .setFixAspectRatio(true)
                .setActivityTitle("Profile Picture")
                .setAllowRotation(false)
                .start(this);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already grunted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                picture.setImageBitmap(BitmapFactory.decodeFile(resultUri.getPath()));
            }
        } else if (resultCode == RESULT_OK && requestCode == 10 && data != null) {
            try {
                Uri imageUri = Uri.parse(data.getStringExtra("signature"));
                signature.setImageURI(imageUri);
                signatureFile = new File(imageUri.getPath());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

}
