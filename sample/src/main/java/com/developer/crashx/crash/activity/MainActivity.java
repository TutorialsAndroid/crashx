package com.developer.crashx.crash.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.developer.crashx.CrashActivity;
import com.developer.crashx.config.CrashConfig;
import com.developer.crashx.crash.R;

public class MainActivity extends AppCompatActivity {

    private TextView tvCurrentMode;
    private Button btnApplyDebugConfig;
    private Button btnApplyProductionConfig;
    private Button btnApplyCustomUiConfig;
    private Button btnSimpleCrash;
    private Button btnDelayedCrash;
    private Button btnNullPointerCrash;
    private Button btnArrayCrash;
    private Button btnCrashWithActivityLog;
    private Button btnDisableCrashX;
    private Button btnEnableCrashX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setClickListeners();
        applyDefaultTestingConfig();
    }

    private void initializeViews() {
        tvCurrentMode = findViewById(R.id.tvCurrentMode);
        btnApplyDebugConfig = findViewById(R.id.btnApplyDebugConfig);
        btnApplyProductionConfig = findViewById(R.id.btnApplyProductionConfig);
        btnApplyCustomUiConfig = findViewById(R.id.btnApplyCustomUiConfig);
        btnSimpleCrash = findViewById(R.id.btnSimpleCrash);
        btnDelayedCrash = findViewById(R.id.btnDelayedCrash);
        btnNullPointerCrash = findViewById(R.id.btnNullPointerCrash);
        btnArrayCrash = findViewById(R.id.btnArrayCrash);
        btnCrashWithActivityLog = findViewById(R.id.btnCrashWithActivityLog);
        btnDisableCrashX = findViewById(R.id.btnDisableCrashX);
        btnEnableCrashX = findViewById(R.id.btnEnableCrashX);
    }

    private void setClickListeners() {
        btnApplyDebugConfig.setOnClickListener(v -> applyDebugConfig());
        btnApplyProductionConfig.setOnClickListener(v -> applyProductionConfig());
        btnApplyCustomUiConfig.setOnClickListener(v -> applyCustomUiConfig());

        btnSimpleCrash.setOnClickListener(v -> {
            throw new RuntimeException("CrashX v7 test crash: simple RuntimeException from MainActivity");
        });

        btnDelayedCrash.setOnClickListener(v -> {
            Toast.makeText(this, "App will crash in 3 seconds...", Toast.LENGTH_SHORT).show();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                throw new RuntimeException("CrashX v7 delayed crash test after 3 seconds");
            }, 3000);
        });

        btnNullPointerCrash.setOnClickListener(v -> {
            String testValue = null;
            int length = testValue.length();
            Toast.makeText(this, "Length: " + length, Toast.LENGTH_SHORT).show();
        });

        btnArrayCrash.setOnClickListener(v -> {
            int[] numbers = {1, 2, 3};
            int value = numbers[10];
            Toast.makeText(this, "Value: " + value, Toast.LENGTH_SHORT).show();
        });

        btnCrashWithActivityLog.setOnClickListener(v -> {
            CrashConfig.Builder.create()
                    .enabled(true)
                    .backgroundMode(CrashConfig.BACKGROUND_MODE_SHOW_CUSTOM)
                    .showErrorDetails(true)
                    .showRestartButton(true)
                    .showCloseButton(true)
                    .logErrorOnRestart(true)
                    .trackActivities(true)
                    .includeDeviceInfo(true)
                    .includeActivityLog(true)
                    .includeStackTrace(true)
                    .includeCrashId(true)
                    .showCrashId(true)
                    .showAppInfo(true)
                    .maxActivityLogEntries(50)
                    .maxStackTraceSize(128 * 1024)
                    .crashIdPrefix("SAMPLE")
                    .errorTitle("Crash with Activity Log")
                    .errorMessage("This crash was triggered with activity tracking enabled. Open details to see lifecycle logs.")
                    .restartButtonText("Restart app")
                    .closeButtonText("Close app")
                    .detailsButtonText("View crash details")
                    .showReportButton(true)
                    .reportButtonText("Send report")
                    .supportEmail("support@example.com")
                    .reportSubject("CrashX sample crash report")
                    .additionalReportInfo("Screen: MainActivity\nMode: Activity log test")
                    .restartActivity(MainActivity.class)
                    .eventListener(new CrashXTestEventListener())
                    .apply();

            Toast.makeText(this, "Activity log enabled. Crashing now...", Toast.LENGTH_SHORT).show();
            throw new IllegalStateException("CrashX v7 test crash with activity lifecycle tracking enabled");
        });

        btnDisableCrashX.setOnClickListener(v -> {
            CrashConfig.Builder.create()
                    .enabled(false)
                    .apply();
            tvCurrentMode.setText("Current mode: CrashX disabled");
            Toast.makeText(this, "CrashX disabled. Next crash will use normal Android crash behavior.", Toast.LENGTH_LONG).show();
        });

        btnEnableCrashX.setOnClickListener(v -> {
            applyDefaultTestingConfig();
            Toast.makeText(this, "CrashX enabled again.", Toast.LENGTH_SHORT).show();
        });
    }

    private void applyDefaultTestingConfig() {
        CrashConfig.Builder.create()
                .enabled(true)
                .backgroundMode(CrashConfig.BACKGROUND_MODE_SHOW_CUSTOM)
                .showErrorDetails(true)
                .showRestartButton(true)
                .showCloseButton(true)
                .logErrorOnRestart(true)
                .trackActivities(true)
                .includeDeviceInfo(true)
                .includeActivityLog(true)
                .includeStackTrace(true)
                .includeCrashId(true)
                .includeBuildDate(true)
                .showCrashId(true)
                .showAppInfo(true)
                .maxActivityLogEntries(50)
                .maxStackTraceSize(128 * 1024)
                .crashIdPrefix("CRASHX")
                .errorTitle("Oops! The app crashed")
                .errorMessage("Something unexpected happened. Please restart the app or send a crash report to help us fix it.")
                .restartButtonText("Restart app")
                .closeButtonText("Close app")
                .detailsButtonText("View technical details")
                .copyButtonText("Copy details")
                .showReportButton(true)
                .reportButtonText("Send crash report")
                .supportEmail("support@example.com")
                .reportSubject("CrashX sample crash report")
                .reportChooserTitle("Share crash report")
                .additionalReportInfo("Environment: Sample app\nBuild type: Testing")
                .backgroundColor(0xFFB91C1C)
                .cardBackgroundColor(0xFFFFFFFF)
                .primaryButtonColor(0xFFB91C1C)
                .secondaryButtonColor(0xFFF3F4F6)
                .titleTextColor(0xFF111827)
                .messageTextColor(0xFF4B5563)
                .buttonTextColor(0xFFFFFFFF)
                .secondaryButtonTextColor(0xFF111827)
                .metaTextColor(0xFF6B7280)
                .restartActivity(MainActivity.class)
                .eventListener(new CrashXTestEventListener())
                .apply();

        tvCurrentMode.setText("Current mode: Default v7 testing config");
    }

    private void applyDebugConfig() {
        CrashConfig.Builder.create()
                .enabled(true)
                .backgroundMode(CrashConfig.BACKGROUND_MODE_SHOW_CUSTOM)
                .showErrorDetails(true)
                .showRestartButton(true)
                .showCloseButton(true)
                .logErrorOnRestart(true)
                .trackActivities(true)
                .includeDeviceInfo(true)
                .includeActivityLog(true)
                .includeStackTrace(true)
                .showReportButton(true)
                .showCrashId(true)
                .showAppInfo(true)
                .crashIdPrefix("DEBUG")
                .errorTitle("Debug Crash")
                .errorMessage("CrashX debug mode is enabled. Error details, device information, and activity logs are visible.")
                .restartButtonText("Restart debug app")
                .closeButtonText("Close")
                .detailsButtonText("Open stack trace")
                .reportButtonText("Share debug report")
                .supportEmail("developer@example.com")
                .backgroundColor(0xFF1F2937)
                .cardBackgroundColor(0xFFFFFFFF)
                .primaryButtonColor(0xFF2563EB)
                .secondaryButtonColor(0xFFE5E7EB)
                .titleTextColor(0xFF111827)
                .messageTextColor(0xFF4B5563)
                .buttonTextColor(0xFFFFFFFF)
                .secondaryButtonTextColor(0xFF111827)
                .restartActivity(MainActivity.class)
                .eventListener(new CrashXTestEventListener())
                .apply();

        tvCurrentMode.setText("Current mode: Debug config");
        Toast.makeText(this, "Debug CrashX config applied.", Toast.LENGTH_SHORT).show();
    }

    private void applyProductionConfig() {
        CrashConfig.Builder.create()
                .enabled(true)
                .backgroundMode(CrashConfig.BACKGROUND_MODE_SHOW_CUSTOM)
                .showErrorDetails(false)
                .showRestartButton(true)
                .showCloseButton(true)
                .logErrorOnRestart(false)
                .trackActivities(false)
                .includeDeviceInfo(true)
                .includeActivityLog(false)
                .includeStackTrace(true)
                .showReportButton(true)
                .showCrashId(true)
                .showAppInfo(false)
                .crashIdPrefix("PROD")
                .maxActivityLogEntries(20)
                .maxStackTraceSize(64 * 1024)
                .errorTitle("Something went wrong")
                .errorMessage("The app ran into an unexpected problem. Please restart the app or report the issue.")
                .restartButtonText("Restart app")
                .closeButtonText("Close app")
                .detailsButtonText("Details")
                .reportButtonText("Report issue")
                .supportEmail("support@example.com")
                .backgroundColor(0xFF991B1B)
                .cardBackgroundColor(0xFFFFFFFF)
                .primaryButtonColor(0xFF991B1B)
                .secondaryButtonColor(0xFFF3F4F6)
                .titleTextColor(0xFF111827)
                .messageTextColor(0xFF4B5563)
                .buttonTextColor(0xFFFFFFFF)
                .secondaryButtonTextColor(0xFF111827)
                .restartActivity(MainActivity.class)
                .eventListener(new CrashXTestEventListener())
                .apply();

        tvCurrentMode.setText("Current mode: Production config");
        Toast.makeText(this, "Production CrashX config applied.", Toast.LENGTH_SHORT).show();
    }

    private void applyCustomUiConfig() {
        CrashConfig.Builder.create()
                .enabled(true)
                .backgroundMode(CrashConfig.BACKGROUND_MODE_SHOW_CUSTOM)
                .showErrorDetails(true)
                .showRestartButton(true)
                .showCloseButton(true)
                .logErrorOnRestart(true)
                .trackActivities(true)
                .includeDeviceInfo(true)
                .includeActivityLog(true)
                .includeStackTrace(true)
                .showReportButton(true)
                .showCrashId(true)
                .showAppInfo(true)
                .crashIdPrefix("PURPLE")
                .maxActivityLogEntries(50)
                .maxStackTraceSize(128 * 1024)
                .errorTitle("CrashX Protected App")
                .errorMessage("No worries. CrashX caught the crash and gave your app a clean recovery screen.")
                .restartButtonText("Launch again")
                .closeButtonText("Exit app")
                .detailsButtonText("Technical info")
                .reportButtonText("Email report")
                .supportEmail("support@example.com")
                .backgroundColor(0xFF0F172A)
                .cardBackgroundColor(0xFFFFFFFF)
                .primaryButtonColor(0xFF7C3AED)
                .secondaryButtonColor(0xFFF1F5F9)
                .titleTextColor(0xFF111827)
                .messageTextColor(0xFF475569)
                .buttonTextColor(0xFFFFFFFF)
                .secondaryButtonTextColor(0xFF111827)
                .metaTextColor(0xFF64748B)
                .restartActivity(MainActivity.class)
                .eventListener(new CrashXTestEventListener())
                .apply();

        tvCurrentMode.setText("Current mode: Custom UI config");
        Toast.makeText(this, "Custom UI CrashX config applied.", Toast.LENGTH_SHORT).show();
    }

    public static class CrashXTestEventListener implements CrashActivity.EventListener {
        @Override
        public void onLaunchErrorActivity() {
            android.util.Log.d("CrashXSample", "CrashX error activity launched");
        }

        @Override
        public void onRestartAppFromErrorActivity() {
            android.util.Log.d("CrashXSample", "App restarted from CrashX error activity");
        }

        @Override
        public void onCloseAppFromErrorActivity() {
            android.util.Log.d("CrashXSample", "App closed from CrashX error activity");
        }
    }
}
