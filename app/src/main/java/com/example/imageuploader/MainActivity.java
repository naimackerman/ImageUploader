package com.example.imageuploader;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnGo = findViewById(R.id.buttonGo);

        btnGo.setOnClickListener(operasi);
    }

    View.OnClickListener operasi = v -> {
        switch (v.getId()) {
            case R.id.buttonGo:openCapture();break;
        }
    };

    private void openCapture() {
        Intent intentku = new Intent(getBaseContext(), CaptureActivity.class);
        startActivityForResult(intentku, 0);
    }
}