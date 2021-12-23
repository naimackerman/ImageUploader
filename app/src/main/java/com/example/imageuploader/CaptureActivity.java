package com.example.imageuploader;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.clans.fab.FloatingActionMenu;
import com.github.clans.fab.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CaptureActivity extends AppCompatActivity {

    ImageView iv;
    Bitmap bitmap;
    TextView tv;
    WebView wv;

    FloatingActionMenu materialDesignFAM;
    FloatingActionButton floatingActionButton1, floatingActionButton3;

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS= 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        checkAndroidVersion();

        iv = findViewById(R.id.imageView);
        tv = findViewById(R.id.textId);
        wv = findViewById(R.id.webView);
        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        floatingActionButton1 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton3 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item3);

        floatingActionButton1.setOnClickListener(view -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file = new File(Environment.getExternalStorageDirectory()+ File.separator + "img.jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            startActivityForResult(intent, 1);
        });

        floatingActionButton3.setOnClickListener(view -> finish());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1){
            File file = new File(Environment.getExternalStorageDirectory()+File.separator + "img.jpg");
            try {
                cropCapturedImage(Uri.fromFile(file));
            }
            catch(ActivityNotFoundException aNFE){
                String errorMessage = "Sorry - your device doesn't support the crop action!";
                Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        if(requestCode == 2){
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            Bitmap thePic = extras.getParcelable("data");
            iv.setImageBitmap(thePic);

            try {
                uploadToServer();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void cropCapturedImage(Uri picUri){
        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        cropIntent.setDataAndType(picUri, "image/*");
        cropIntent.putExtra("crop", "true");

        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);

        cropIntent.putExtra("outputX", 256);
        cropIntent.putExtra("outputY", 256);

        cropIntent.putExtra("return-data", true);

        startActivityForResult(cropIntent, 2);
    }

    public String BitMapToString() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] imgByte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgByte, Base64.DEFAULT);
    }

    public void uploadToServer() {
        String image = BitMapToString();
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<ImgRes> call = apiInterface.uploadImage(image);

        call.enqueue(new Callback<ImgRes>() {
            @Override
            public void onResponse(@NonNull Call<ImgRes> call, @NonNull Response<ImgRes> response) {
                ImgRes imgRes = response.body();
                assert imgRes != null;
                Toast.makeText(getApplicationContext(), ""+imgRes.getResponse(), Toast.LENGTH_LONG).show();
                String nopol = imgRes.getNopol();
                nopol.toUpperCase(Locale.ROOT);
                tv.setText(nopol);
                wv.loadUrl("https://103.146.203.95/pajak/pkb.php?nopol=" + nopol);
                Log.d("Server Response",""+imgRes.getResponse());
            }

            @Override
            public void onFailure(@NonNull Call<ImgRes> call, @NonNull Throwable t) {
                Toast.makeText(getBaseContext(),""+t.toString(),Toast.LENGTH_LONG).show();
                Log.d("Server Response",""+t.toString());
            }
        });
    }

    private void checkAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();

        } else {
            // code for lollipop and pre-lollipop devices
        }

    }


    private boolean checkAndRequestPermissions() {
        int camera = ContextCompat.checkSelfPermission(CaptureActivity.this,
                Manifest.permission.CAMERA);
        int write = ContextCompat.checkSelfPermission(CaptureActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read = ContextCompat.checkSelfPermission(CaptureActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (write != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (read != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(CaptureActivity.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("in fragment on request", "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Log.d("in fragment on request", "CAMERA & WRITE_EXTERNAL_STORAGE READ_EXTERNAL_STORAGE permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d("in fragment on request", "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(CaptureActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(CaptureActivity.this, Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(CaptureActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            showDialogOK(
                                    (dialog, which) -> {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                checkAndRequestPermissions();
                                                break;
                                            case DialogInterface.BUTTON_NEGATIVE:
                                                // proceed with logic by disabling the related features or quit the app.
                                                break;
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(CaptureActivity.this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(CaptureActivity.this)
                .setMessage("Camera and Storage Permission required for this app")
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }
}