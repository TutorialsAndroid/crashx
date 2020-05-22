package com.developer.crashx.crash.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.developer.crashx.CrashActivity;
import com.developer.crashx.config.CrashConfig;
import com.developer.crashx.crash.R;

public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_custom_error);

        TextView errorDetailsText = findViewById(R.id.error_details);
        errorDetailsText.setText(CrashActivity.getStackTraceFromIntent(getIntent()));

        Button restartButton = findViewById(R.id.restart_button);

        final CrashConfig config = CrashActivity.getConfigFromIntent(getIntent());

        if (config == null) {
            finish();
            return;
        }

        if (config.isShowRestartButton() && config.getRestartActivityClass() != null) {
            restartButton.setText(R.string.restart_app);
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CrashActivity.restartApplication(ErrorActivity.this, config);
                }
            });
        } else {
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CrashActivity.closeApplication(ErrorActivity.this, config);
                }
            });
        }
    }
}
