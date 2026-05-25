package com.developer.crashx.activity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;

import com.developer.crashx.CrashActivity;
import com.developer.crashx.R;
import com.developer.crashx.config.CrashConfig;

/**
 * Default CrashX error screen.
 *
 * <p>Originally derived from CustomActivityOnCrash by Eduard Ereza Martínez.</p>
 * <p>Modernized and maintained as CrashX by TutorialsAndroid.</p>
 */
public final class DefaultErrorActivity extends AppCompatActivity {

    private CrashConfig config;

    @SuppressLint("PrivateResource")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar);
        super.onCreate(savedInstanceState);

        TypedArray typedArray = obtainStyledAttributes(androidx.appcompat.R.styleable.AppCompatTheme);
        if (!typedArray.hasValue(androidx.appcompat.R.styleable.AppCompatTheme_windowActionBar)) {
            setTheme(androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar);
        }
        typedArray.recycle();

        config = CrashActivity.getConfigFromIntent(getIntent());

        setContentView(R.layout.crash_default_error_activity);

        setupWindow();
        setupTextContent();
        setupMetaContent();
        setupImage();
        setupButtons();
    }

    private void setupWindow() {
        View root = findViewById(R.id.crash_error_activity_root);
        CardView card = findViewById(R.id.crash_error_activity_card);

        if (root != null) {
            root.setBackgroundColor(config.getBackgroundColor());
        }

        if (card != null) {
            card.setCardBackgroundColor(config.getCardBackgroundColor());
        }

        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && window != null) {
            window.setStatusBarColor(config.getBackgroundColor());
            window.setNavigationBarColor(config.getBackgroundColor());
        }
    }

    private void setupTextContent() {
        TextView titleView = findViewById(R.id.crash_error_activity_title);
        TextView messageView = findViewById(R.id.crash_error_activity_message);

        if (titleView != null) {
            titleView.setText(config.getErrorTitle());
            titleView.setTextColor(config.getTitleTextColor());
        }

        if (messageView != null) {
            messageView.setText(config.getErrorMessage());
            messageView.setTextColor(config.getMessageTextColor());
        }
    }

    private void setupMetaContent() {
        TextView crashIdView = findViewById(R.id.crash_error_activity_crash_id);
        TextView appInfoView = findViewById(R.id.crash_error_activity_app_info);

        if (crashIdView != null) {
            crashIdView.setTextColor(config.getMetaTextColor());
            if (config.isShowCrashId()) {
                crashIdView.setVisibility(View.VISIBLE);
                crashIdView.setText(getString(R.string.crashx_crash_id_format, CrashActivity.getCrashIdFromIntent(getIntent())));
            } else {
                crashIdView.setVisibility(View.GONE);
            }
        }

        if (appInfoView != null) {
            appInfoView.setTextColor(config.getMetaTextColor());
            if (config.isShowAppInfo()) {
                appInfoView.setVisibility(View.VISIBLE);
                appInfoView.setText(getString(R.string.crashx_version_format, CrashActivity.VERSION));
            } else {
                appInfoView.setVisibility(View.GONE);
            }
        }
    }

    private void setupImage() {
        Integer drawableId = config.getErrorDrawable();
        ImageView errorImageView = findViewById(R.id.crash_error_activity_image);

        if (errorImageView != null && drawableId != null) {
            errorImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), drawableId, getTheme()));
        }
    }

    private void setupButtons() {
        Button restartButton = findViewById(R.id.crash_error_activity_restart_button);
        Button closeButton = findViewById(R.id.crash_error_activity_close_button);
        Button detailsButton = findViewById(R.id.crash_error_activity_more_info_button);
        Button reportButton = findViewById(R.id.crash_error_activity_report_button);

        setupRestartButton(restartButton);
        setupCloseButton(closeButton);
        setupDetailsButton(detailsButton);
        setupReportButton(reportButton);
    }

    private void setupRestartButton(@Nullable Button restartButton) {
        if (restartButton == null) {
            return;
        }

        tintPrimaryButton(restartButton);

        if (config.isShowRestartButton() && config.getRestartActivityClass() != null) {
            restartButton.setVisibility(View.VISIBLE);
            restartButton.setText(config.getRestartButtonText() != null
                    ? config.getRestartButtonText()
                    : getString(R.string.customactivityoncrash_error_activity_restart_app));
            restartButton.setOnClickListener(v -> CrashActivity.restartApplication(DefaultErrorActivity.this, config));
        } else {
            restartButton.setVisibility(View.GONE);
        }
    }

    private void setupCloseButton(@Nullable Button closeButton) {
        if (closeButton == null) {
            return;
        }

        tintSecondaryButton(closeButton);

        if (config.isShowCloseButton()) {
            closeButton.setVisibility(View.VISIBLE);
            closeButton.setText(config.getCloseButtonText() != null
                    ? config.getCloseButtonText()
                    : getString(R.string.customactivityoncrash_error_activity_close_app));
            closeButton.setOnClickListener(v -> CrashActivity.closeApplication(DefaultErrorActivity.this, config));
        } else {
            closeButton.setVisibility(View.GONE);
        }
    }

    private void setupDetailsButton(@Nullable Button detailsButton) {
        if (detailsButton == null) {
            return;
        }

        tintSecondaryButton(detailsButton);

        if (config.isShowErrorDetails()) {
            detailsButton.setVisibility(View.VISIBLE);
            detailsButton.setText(config.getDetailsButtonText() != null
                    ? config.getDetailsButtonText()
                    : getString(R.string.customactivityoncrash_error_activity_error_details));
            detailsButton.setOnClickListener(v -> showErrorDetailsDialog());
        } else {
            detailsButton.setVisibility(View.GONE);
        }
    }

    private void setupReportButton(@Nullable Button reportButton) {
        if (reportButton == null) {
            return;
        }

        tintPrimaryButton(reportButton);

        if (config.isShowReportButton()) {
            reportButton.setVisibility(View.VISIBLE);
            reportButton.setText(config.getReportButtonText());
            reportButton.setOnClickListener(v -> shareCrashReport());
        } else {
            reportButton.setVisibility(View.GONE);
        }
    }

    private void tintPrimaryButton(@Nullable Button button) {
        if (button == null) {
            return;
        }
        button.setTextColor(config.getButtonTextColor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.setBackgroundTintList(ColorStateList.valueOf(config.getPrimaryButtonColor()));
        } else {
            button.setBackgroundColor(config.getPrimaryButtonColor());
        }
    }

    private void tintSecondaryButton(@Nullable Button button) {
        if (button == null) {
            return;
        }
        button.setTextColor(config.getSecondaryButtonTextColor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.setBackgroundTintList(ColorStateList.valueOf(config.getSecondaryButtonColor()));
        } else {
            button.setBackgroundColor(config.getSecondaryButtonColor());
        }
    }

    private void showErrorDetailsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DefaultErrorActivity.this)
                .setTitle(R.string.customactivityoncrash_error_activity_error_details_title)
                .setMessage(CrashActivity.getAllErrorDetailsFromIntent(DefaultErrorActivity.this, getIntent()))
                .setPositiveButton(R.string.customactivityoncrash_error_activity_error_details_close, null);

        if (config.isShowCopyButtonInDetails()) {
            String copyText = config.getCopyButtonText() != null
                    ? config.getCopyButtonText()
                    : getString(R.string.customactivityoncrash_error_activity_error_details_copy);
            builder.setNeutralButton(copyText, (dialog, which) -> copyErrorToClipboard());
        }

        AlertDialog dialog = builder.show();
        TextView textView = dialog.findViewById(android.R.id.message);
        if (textView != null) {
            textView.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.customactivityoncrash_error_activity_error_details_text_size)
            );
        }
    }

    private void copyErrorToClipboard() {
        String errorInformation = CrashActivity.getAllErrorDetailsFromIntent(DefaultErrorActivity.this, getIntent());
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(
                    getString(R.string.customactivityoncrash_error_activity_error_details_clipboard_label),
                    errorInformation
            );
            clipboard.setPrimaryClip(clip);
            Toast.makeText(
                    DefaultErrorActivity.this,
                    R.string.customactivityoncrash_error_activity_error_details_copied,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void shareCrashReport() {
        String errorInformation = CrashActivity.getAllErrorDetailsFromIntent(DefaultErrorActivity.this, getIntent());
        String subject = config.getReportSubject() != null
                ? config.getReportSubject()
                : getString(R.string.crashx_default_report_subject, getPackageName(), CrashActivity.getCrashIdFromIntent(getIntent()));
        String chooserTitle = config.getReportChooserTitle() != null
                ? config.getReportChooserTitle()
                : config.getReportButtonText();

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.putExtra(Intent.EXTRA_TEXT, errorInformation);

        if (config.getSupportEmail() != null) {
            sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{config.getSupportEmail()});
        }

        try {
            startActivity(Intent.createChooser(sendIntent, chooserTitle));
        } catch (ActivityNotFoundException e) {
            copyErrorToClipboard();
            Toast.makeText(this, R.string.crashx_no_share_app_found, Toast.LENGTH_SHORT).show();
        }
    }
}
