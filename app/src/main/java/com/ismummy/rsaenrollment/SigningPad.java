package com.ismummy.rsaenrollment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.ismummy.rsaenrollment.bases.BaseActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class SigningPad extends BaseActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int READ_EXTERNAL_STORAGE = 2;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static String[] PERMISSIONS_READ = {Manifest.permission.READ_EXTERNAL_STORAGE};

    @BindView(R.id.signature_pad)
    SignaturePad mSignaturePad;
    @BindView(R.id.signing_toolbar)
    Toolbar toolbar;
    @BindView(R.id.clear_button)
    Button clearBtn;
    @BindView(R.id.save_button)
    Button saveBtn;

    private Uri signUri;

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE

            );
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_READ,
                    READ_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signing_pad);
        verifyStoragePermissions(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
            }

            @Override
            public void onSigned() {
                saveBtn.setEnabled(true);
                clearBtn.setEnabled(true);
            }

            @Override
            public void onClear() {
                saveBtn.setEnabled(false);
                clearBtn.setEnabled(false);
            }
        });
        clearBtn.setEnabled(false);
        saveBtn.setEnabled(false);
    }

    @OnClick(R.id.clear_button)
    void clearClick() {
        mSignaturePad.clear();
    }

    @OnClick(R.id.save_button)
    void saveClick() {
        Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
        if (addJpgSignatureToGallery(signatureBitmap)) {
            Intent intent = getIntent();
            intent.putExtra("signature", signUri.toString());
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(SigningPad.this, "Unable to store the signature", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length <= 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(SigningPad.this, "Cannot write images to external storage",
                            Toast.LENGTH_SHORT).show();
                }
            }
            case READ_EXTERNAL_STORAGE: {
                if (grantResults.length <= 1
                        || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(SigningPad.this, "Cannot read images from external storage",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("SignaturePad", "Directory not created");
        }
        return file;
    }

    public boolean addJpgSignatureToGallery(Bitmap signature) {
        boolean result = false;
        try {
            File photo = new File(getAlbumStorageDir("SignaturePad"),
                    String.format(Locale.getDefault(), "Signature_%d.jpg", System.currentTimeMillis()));
            saveBitmapToJPG(signature, photo);
            scanMediaFile(photo);
            signUri = Uri.fromFile(photo);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void scanMediaFile(File photo) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(photo);
        mediaScanIntent.setData(contentUri);
        SigningPad.this.sendBroadcast(mediaScanIntent);
    }

}
