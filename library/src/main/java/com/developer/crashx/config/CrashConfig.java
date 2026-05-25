package com.developer.crashx.config;

import android.app.Activity;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.developer.crashx.CrashActivity;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Modifier;

/**
 * CrashX configuration.
 *
 * <p>Originally derived from CustomActivityOnCrash by Eduard Ereza Martínez.</p>
 * <p>Modernized and maintained as CrashX by TutorialsAndroid.</p>
 */
public class CrashConfig implements Serializable {

    private static final long serialVersionUID = 7000000L;

    @IntDef({BACKGROUND_MODE_SILENT, BACKGROUND_MODE_SHOW_CUSTOM, BACKGROUND_MODE_CRASH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface BackgroundMode {
    }

    /** Silently closes the app when a background crash happens. */
    public static final int BACKGROUND_MODE_SILENT = 0;

    /** Shows the custom CrashX screen even if the app crashes in the background. */
    public static final int BACKGROUND_MODE_SHOW_CUSTOM = 1;

    /** Lets the original/default uncaught exception handler handle background crashes. */
    public static final int BACKGROUND_MODE_CRASH = 2;

    private int backgroundMode = BACKGROUND_MODE_SHOW_CUSTOM;
    private boolean enabled = true;
    private boolean showErrorDetails = true;
    private boolean showRestartButton = true;
    private boolean showCloseButton = true;
    private boolean logErrorOnRestart = true;
    private boolean trackActivities = false;
    private int minTimeBetweenCrashesMs = 3000;
    private Integer errorDrawable = null;
    private Class<? extends Activity> errorActivityClass = null;
    private Class<? extends Activity> restartActivityClass = null;
    private CrashActivity.EventListener eventListener = null;

    // CrashX v7 UI options
    private String errorTitle = "Something went wrong";
    private String errorMessage = "The app ran into an unexpected problem. You can restart the app or send a crash report.";
    private String restartButtonText = null;
    private String closeButtonText = null;
    private String detailsButtonText = null;
    private String reportButtonText = "Send crash report";
    private String copyButtonText = null;
    private String supportEmail = null;
    private String reportSubject = null;
    private String reportChooserTitle = null;
    private String additionalReportInfo = null;
    private boolean showReportButton = false;
    private boolean showCopyButtonInDetails = true;
    private boolean showCrashId = true;
    private boolean showAppInfo = true;
    private boolean includeDeviceInfo = true;
    private boolean includeActivityLog = true;
    private boolean includeStackTrace = true;
    private boolean includeBuildDate = true;
    private boolean includeCrashId = true;
    private int maxActivityLogEntries = 50;
    private int maxStackTraceSize = 131071;
    private String crashIdPrefix = "CRASHX";

    @ColorInt
    private int backgroundColor = 0xFFD50000;
    @ColorInt
    private int cardBackgroundColor = 0xFFFFFFFF;
    @ColorInt
    private int primaryButtonColor = 0xFFD50000;
    @ColorInt
    private int secondaryButtonColor = 0xFFF3F4F6;
    @ColorInt
    private int titleTextColor = 0xFF111827;
    @ColorInt
    private int messageTextColor = 0xFF4B5563;
    @ColorInt
    private int buttonTextColor = 0xFFFFFFFF;
    @ColorInt
    private int secondaryButtonTextColor = 0xFF111827;
    @ColorInt
    private int metaTextColor = 0xFF6B7280;

    @BackgroundMode
    public int getBackgroundMode() {
        return backgroundMode;
    }

    public void setBackgroundMode(@BackgroundMode int backgroundMode) {
        this.backgroundMode = backgroundMode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isShowErrorDetails() {
        return showErrorDetails;
    }

    public void setShowErrorDetails(boolean showErrorDetails) {
        this.showErrorDetails = showErrorDetails;
    }

    public boolean isShowRestartButton() {
        return showRestartButton;
    }

    public void setShowRestartButton(boolean showRestartButton) {
        this.showRestartButton = showRestartButton;
    }

    public boolean isShowCloseButton() {
        return showCloseButton;
    }

    public void setShowCloseButton(boolean showCloseButton) {
        this.showCloseButton = showCloseButton;
    }

    public boolean isLogErrorOnRestart() {
        return logErrorOnRestart;
    }

    public void setLogErrorOnRestart(boolean logErrorOnRestart) {
        this.logErrorOnRestart = logErrorOnRestart;
    }

    public boolean isTrackActivities() {
        return trackActivities;
    }

    public void setTrackActivities(boolean trackActivities) {
        this.trackActivities = trackActivities;
    }

    public int getMinTimeBetweenCrashesMs() {
        return minTimeBetweenCrashesMs;
    }

    public void setMinTimeBetweenCrashesMs(int minTimeBetweenCrashesMs) {
        this.minTimeBetweenCrashesMs = Math.max(0, minTimeBetweenCrashesMs);
    }

    @Nullable
    @DrawableRes
    public Integer getErrorDrawable() {
        return errorDrawable;
    }

    public void setErrorDrawable(@Nullable @DrawableRes Integer errorDrawable) {
        this.errorDrawable = errorDrawable;
    }

    @Nullable
    public Class<? extends Activity> getErrorActivityClass() {
        return errorActivityClass;
    }

    public void setErrorActivityClass(@Nullable Class<? extends Activity> errorActivityClass) {
        this.errorActivityClass = errorActivityClass;
    }

    @Nullable
    public Class<? extends Activity> getRestartActivityClass() {
        return restartActivityClass;
    }

    public void setRestartActivityClass(@Nullable Class<? extends Activity> restartActivityClass) {
        this.restartActivityClass = restartActivityClass;
    }

    @Nullable
    public CrashActivity.EventListener getEventListener() {
        return eventListener;
    }

    public void setEventListener(@Nullable CrashActivity.EventListener eventListener) {
        this.eventListener = eventListener;
    }

    @NonNull
    public String getErrorTitle() {
        return safeString(errorTitle, "Something went wrong");
    }

    public void setErrorTitle(@Nullable String errorTitle) {
        this.errorTitle = safeString(errorTitle, "Something went wrong");
    }

    @NonNull
    public String getErrorMessage() {
        return safeString(errorMessage, "The app ran into an unexpected problem. You can restart the app or send a crash report.");
    }

    public void setErrorMessage(@Nullable String errorMessage) {
        this.errorMessage = safeString(errorMessage, "The app ran into an unexpected problem. You can restart the app or send a crash report.");
    }

    @Nullable
    public String getRestartButtonText() {
        return emptyToNull(restartButtonText);
    }

    public void setRestartButtonText(@Nullable String restartButtonText) {
        this.restartButtonText = emptyToNull(restartButtonText);
    }

    @Nullable
    public String getCloseButtonText() {
        return emptyToNull(closeButtonText);
    }

    public void setCloseButtonText(@Nullable String closeButtonText) {
        this.closeButtonText = emptyToNull(closeButtonText);
    }

    @Nullable
    public String getDetailsButtonText() {
        return emptyToNull(detailsButtonText);
    }

    public void setDetailsButtonText(@Nullable String detailsButtonText) {
        this.detailsButtonText = emptyToNull(detailsButtonText);
    }

    @NonNull
    public String getReportButtonText() {
        return safeString(reportButtonText, "Send crash report");
    }

    public void setReportButtonText(@Nullable String reportButtonText) {
        this.reportButtonText = safeString(reportButtonText, "Send crash report");
    }

    @Nullable
    public String getCopyButtonText() {
        return emptyToNull(copyButtonText);
    }

    public void setCopyButtonText(@Nullable String copyButtonText) {
        this.copyButtonText = emptyToNull(copyButtonText);
    }

    @Nullable
    public String getSupportEmail() {
        return emptyToNull(supportEmail);
    }

    public void setSupportEmail(@Nullable String supportEmail) {
        this.supportEmail = emptyToNull(supportEmail);
    }

    @Nullable
    public String getReportSubject() {
        return emptyToNull(reportSubject);
    }

    public void setReportSubject(@Nullable String reportSubject) {
        this.reportSubject = emptyToNull(reportSubject);
    }

    @Nullable
    public String getReportChooserTitle() {
        return emptyToNull(reportChooserTitle);
    }

    public void setReportChooserTitle(@Nullable String reportChooserTitle) {
        this.reportChooserTitle = emptyToNull(reportChooserTitle);
    }

    @Nullable
    public String getAdditionalReportInfo() {
        return emptyToNull(additionalReportInfo);
    }

    public void setAdditionalReportInfo(@Nullable String additionalReportInfo) {
        this.additionalReportInfo = emptyToNull(additionalReportInfo);
    }

    public boolean isShowReportButton() {
        return showReportButton;
    }

    public void setShowReportButton(boolean showReportButton) {
        this.showReportButton = showReportButton;
    }

    public boolean isShowCopyButtonInDetails() {
        return showCopyButtonInDetails;
    }

    public void setShowCopyButtonInDetails(boolean showCopyButtonInDetails) {
        this.showCopyButtonInDetails = showCopyButtonInDetails;
    }

    public boolean isShowCrashId() {
        return showCrashId;
    }

    public void setShowCrashId(boolean showCrashId) {
        this.showCrashId = showCrashId;
    }

    public boolean isShowAppInfo() {
        return showAppInfo;
    }

    public void setShowAppInfo(boolean showAppInfo) {
        this.showAppInfo = showAppInfo;
    }

    public boolean isIncludeDeviceInfo() {
        return includeDeviceInfo;
    }

    public void setIncludeDeviceInfo(boolean includeDeviceInfo) {
        this.includeDeviceInfo = includeDeviceInfo;
    }

    public boolean isIncludeActivityLog() {
        return includeActivityLog;
    }

    public void setIncludeActivityLog(boolean includeActivityLog) {
        this.includeActivityLog = includeActivityLog;
    }

    public boolean isIncludeStackTrace() {
        return includeStackTrace;
    }

    public void setIncludeStackTrace(boolean includeStackTrace) {
        this.includeStackTrace = includeStackTrace;
    }

    public boolean isIncludeBuildDate() {
        return includeBuildDate;
    }

    public void setIncludeBuildDate(boolean includeBuildDate) {
        this.includeBuildDate = includeBuildDate;
    }

    public boolean isIncludeCrashId() {
        return includeCrashId;
    }

    public void setIncludeCrashId(boolean includeCrashId) {
        this.includeCrashId = includeCrashId;
    }

    public int getMaxActivityLogEntries() {
        return maxActivityLogEntries;
    }

    public void setMaxActivityLogEntries(int maxActivityLogEntries) {
        this.maxActivityLogEntries = Math.max(1, maxActivityLogEntries);
    }

    public int getMaxStackTraceSize() {
        return maxStackTraceSize;
    }

    public void setMaxStackTraceSize(int maxStackTraceSize) {
        this.maxStackTraceSize = Math.max(1024, maxStackTraceSize);
    }

    @NonNull
    public String getCrashIdPrefix() {
        return safeString(crashIdPrefix, "CRASHX");
    }

    public void setCrashIdPrefix(@Nullable String crashIdPrefix) {
        this.crashIdPrefix = safeString(crashIdPrefix, "CRASHX");
    }

    @ColorInt
    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @ColorInt
    public int getCardBackgroundColor() {
        return cardBackgroundColor;
    }

    public void setCardBackgroundColor(@ColorInt int cardBackgroundColor) {
        this.cardBackgroundColor = cardBackgroundColor;
    }

    @ColorInt
    public int getPrimaryButtonColor() {
        return primaryButtonColor;
    }

    public void setPrimaryButtonColor(@ColorInt int primaryButtonColor) {
        this.primaryButtonColor = primaryButtonColor;
    }

    @ColorInt
    public int getSecondaryButtonColor() {
        return secondaryButtonColor;
    }

    public void setSecondaryButtonColor(@ColorInt int secondaryButtonColor) {
        this.secondaryButtonColor = secondaryButtonColor;
    }

    @ColorInt
    public int getTitleTextColor() {
        return titleTextColor;
    }

    public void setTitleTextColor(@ColorInt int titleTextColor) {
        this.titleTextColor = titleTextColor;
    }

    @ColorInt
    public int getMessageTextColor() {
        return messageTextColor;
    }

    public void setMessageTextColor(@ColorInt int messageTextColor) {
        this.messageTextColor = messageTextColor;
    }

    @ColorInt
    public int getButtonTextColor() {
        return buttonTextColor;
    }

    public void setButtonTextColor(@ColorInt int buttonTextColor) {
        this.buttonTextColor = buttonTextColor;
    }

    @ColorInt
    public int getSecondaryButtonTextColor() {
        return secondaryButtonTextColor;
    }

    public void setSecondaryButtonTextColor(@ColorInt int secondaryButtonTextColor) {
        this.secondaryButtonTextColor = secondaryButtonTextColor;
    }

    @ColorInt
    public int getMetaTextColor() {
        return metaTextColor;
    }

    public void setMetaTextColor(@ColorInt int metaTextColor) {
        this.metaTextColor = metaTextColor;
    }

    @NonNull
    private static String safeString(@Nullable String value, @NonNull String fallback) {
        String normalized = emptyToNull(value);
        return normalized == null ? fallback : normalized;
    }

    @Nullable
    private static String emptyToNull(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() == 0 ? null : trimmed;
    }

    /**
     * Fluent builder for CrashX configuration.
     */
    public static class Builder {
        private CrashConfig config;

        @NonNull
        public static Builder create() {
            Builder builder = new Builder();
            CrashConfig currentConfig = CrashActivity.getConfig();
            builder.config = copyOf(currentConfig);
            return builder;
        }

        @NonNull
        private static CrashConfig copyOf(@NonNull CrashConfig source) {
            CrashConfig target = new CrashConfig();
            target.backgroundMode = source.backgroundMode;
            target.enabled = source.enabled;
            target.showErrorDetails = source.showErrorDetails;
            target.showRestartButton = source.showRestartButton;
            target.showCloseButton = source.showCloseButton;
            target.logErrorOnRestart = source.logErrorOnRestart;
            target.trackActivities = source.trackActivities;
            target.minTimeBetweenCrashesMs = source.minTimeBetweenCrashesMs;
            target.errorDrawable = source.errorDrawable;
            target.errorActivityClass = source.errorActivityClass;
            target.restartActivityClass = source.restartActivityClass;
            target.eventListener = source.eventListener;
            target.errorTitle = source.errorTitle;
            target.errorMessage = source.errorMessage;
            target.restartButtonText = source.restartButtonText;
            target.closeButtonText = source.closeButtonText;
            target.detailsButtonText = source.detailsButtonText;
            target.reportButtonText = source.reportButtonText;
            target.copyButtonText = source.copyButtonText;
            target.supportEmail = source.supportEmail;
            target.reportSubject = source.reportSubject;
            target.reportChooserTitle = source.reportChooserTitle;
            target.additionalReportInfo = source.additionalReportInfo;
            target.showReportButton = source.showReportButton;
            target.showCopyButtonInDetails = source.showCopyButtonInDetails;
            target.showCrashId = source.showCrashId;
            target.showAppInfo = source.showAppInfo;
            target.includeDeviceInfo = source.includeDeviceInfo;
            target.includeActivityLog = source.includeActivityLog;
            target.includeStackTrace = source.includeStackTrace;
            target.includeBuildDate = source.includeBuildDate;
            target.includeCrashId = source.includeCrashId;
            target.maxActivityLogEntries = source.maxActivityLogEntries;
            target.maxStackTraceSize = source.maxStackTraceSize;
            target.crashIdPrefix = source.crashIdPrefix;
            target.backgroundColor = source.backgroundColor;
            target.cardBackgroundColor = source.cardBackgroundColor;
            target.primaryButtonColor = source.primaryButtonColor;
            target.secondaryButtonColor = source.secondaryButtonColor;
            target.titleTextColor = source.titleTextColor;
            target.messageTextColor = source.messageTextColor;
            target.buttonTextColor = source.buttonTextColor;
            target.secondaryButtonTextColor = source.secondaryButtonTextColor;
            target.metaTextColor = source.metaTextColor;
            return target;
        }

        @NonNull
        public Builder backgroundMode(@BackgroundMode int backgroundMode) {
            config.backgroundMode = backgroundMode;
            return this;
        }

        @NonNull
        public Builder enabled(boolean enabled) {
            config.enabled = enabled;
            return this;
        }

        @NonNull
        public Builder showErrorDetails(boolean showErrorDetails) {
            config.showErrorDetails = showErrorDetails;
            return this;
        }

        @NonNull
        public Builder showRestartButton(boolean showRestartButton) {
            config.showRestartButton = showRestartButton;
            return this;
        }

        @NonNull
        public Builder showCloseButton(boolean showCloseButton) {
            config.showCloseButton = showCloseButton;
            return this;
        }

        @NonNull
        public Builder logErrorOnRestart(boolean logErrorOnRestart) {
            config.logErrorOnRestart = logErrorOnRestart;
            return this;
        }

        @NonNull
        public Builder trackActivities(boolean trackActivities) {
            config.trackActivities = trackActivities;
            return this;
        }

        @NonNull
        public Builder minTimeBetweenCrashesMs(int minTimeBetweenCrashesMs) {
            config.setMinTimeBetweenCrashesMs(minTimeBetweenCrashesMs);
            return this;
        }

        @NonNull
        public Builder errorDrawable(@Nullable @DrawableRes Integer errorDrawable) {
            config.errorDrawable = errorDrawable;
            return this;
        }

        @NonNull
        public Builder errorActivity(@Nullable Class<? extends Activity> errorActivityClass) {
            config.errorActivityClass = errorActivityClass;
            return this;
        }

        @NonNull
        public Builder restartActivity(@Nullable Class<? extends Activity> restartActivityClass) {
            config.restartActivityClass = restartActivityClass;
            return this;
        }

        @NonNull
        public Builder eventListener(@Nullable CrashActivity.EventListener eventListener) {
            if (eventListener != null
                    && eventListener.getClass().getEnclosingClass() != null
                    && !Modifier.isStatic(eventListener.getClass().getModifiers())) {
                throw new IllegalArgumentException("The event listener cannot be an anonymous or non-static inner class because it must be serialized. Use a top-level class or static inner class.");
            }
            config.eventListener = eventListener;
            return this;
        }

        @NonNull
        public Builder errorTitle(@Nullable String errorTitle) {
            config.setErrorTitle(errorTitle);
            return this;
        }

        @NonNull
        public Builder errorMessage(@Nullable String errorMessage) {
            config.setErrorMessage(errorMessage);
            return this;
        }

        @NonNull
        public Builder restartButtonText(@Nullable String restartButtonText) {
            config.setRestartButtonText(restartButtonText);
            return this;
        }

        @NonNull
        public Builder closeButtonText(@Nullable String closeButtonText) {
            config.setCloseButtonText(closeButtonText);
            return this;
        }

        @NonNull
        public Builder detailsButtonText(@Nullable String detailsButtonText) {
            config.setDetailsButtonText(detailsButtonText);
            return this;
        }

        @NonNull
        public Builder reportButtonText(@Nullable String reportButtonText) {
            config.setReportButtonText(reportButtonText);
            return this;
        }

        @NonNull
        public Builder copyButtonText(@Nullable String copyButtonText) {
            config.setCopyButtonText(copyButtonText);
            return this;
        }

        @NonNull
        public Builder supportEmail(@Nullable String supportEmail) {
            config.setSupportEmail(supportEmail);
            return this;
        }

        @NonNull
        public Builder reportSubject(@Nullable String reportSubject) {
            config.setReportSubject(reportSubject);
            return this;
        }

        @NonNull
        public Builder reportChooserTitle(@Nullable String reportChooserTitle) {
            config.setReportChooserTitle(reportChooserTitle);
            return this;
        }

        @NonNull
        public Builder additionalReportInfo(@Nullable String additionalReportInfo) {
            config.setAdditionalReportInfo(additionalReportInfo);
            return this;
        }

        @NonNull
        public Builder showReportButton(boolean showReportButton) {
            config.showReportButton = showReportButton;
            return this;
        }

        @NonNull
        public Builder showCopyButtonInDetails(boolean showCopyButtonInDetails) {
            config.showCopyButtonInDetails = showCopyButtonInDetails;
            return this;
        }

        @NonNull
        public Builder showCrashId(boolean showCrashId) {
            config.showCrashId = showCrashId;
            return this;
        }

        @NonNull
        public Builder showAppInfo(boolean showAppInfo) {
            config.showAppInfo = showAppInfo;
            return this;
        }

        @NonNull
        public Builder includeDeviceInfo(boolean includeDeviceInfo) {
            config.includeDeviceInfo = includeDeviceInfo;
            return this;
        }

        @NonNull
        public Builder includeActivityLog(boolean includeActivityLog) {
            config.includeActivityLog = includeActivityLog;
            return this;
        }

        @NonNull
        public Builder includeStackTrace(boolean includeStackTrace) {
            config.includeStackTrace = includeStackTrace;
            return this;
        }

        @NonNull
        public Builder includeBuildDate(boolean includeBuildDate) {
            config.includeBuildDate = includeBuildDate;
            return this;
        }

        @NonNull
        public Builder includeCrashId(boolean includeCrashId) {
            config.includeCrashId = includeCrashId;
            return this;
        }

        @NonNull
        public Builder maxActivityLogEntries(int maxActivityLogEntries) {
            config.setMaxActivityLogEntries(maxActivityLogEntries);
            return this;
        }

        @NonNull
        public Builder maxStackTraceSize(int maxStackTraceSize) {
            config.setMaxStackTraceSize(maxStackTraceSize);
            return this;
        }

        @NonNull
        public Builder crashIdPrefix(@Nullable String crashIdPrefix) {
            config.setCrashIdPrefix(crashIdPrefix);
            return this;
        }

        @NonNull
        public Builder backgroundColor(@ColorInt int backgroundColor) {
            config.backgroundColor = backgroundColor;
            return this;
        }

        @NonNull
        public Builder cardBackgroundColor(@ColorInt int cardBackgroundColor) {
            config.cardBackgroundColor = cardBackgroundColor;
            return this;
        }

        @NonNull
        public Builder primaryButtonColor(@ColorInt int primaryButtonColor) {
            config.primaryButtonColor = primaryButtonColor;
            return this;
        }

        @NonNull
        public Builder secondaryButtonColor(@ColorInt int secondaryButtonColor) {
            config.secondaryButtonColor = secondaryButtonColor;
            return this;
        }

        @NonNull
        public Builder titleTextColor(@ColorInt int titleTextColor) {
            config.titleTextColor = titleTextColor;
            return this;
        }

        @NonNull
        public Builder messageTextColor(@ColorInt int messageTextColor) {
            config.messageTextColor = messageTextColor;
            return this;
        }

        @NonNull
        public Builder buttonTextColor(@ColorInt int buttonTextColor) {
            config.buttonTextColor = buttonTextColor;
            return this;
        }

        @NonNull
        public Builder secondaryButtonTextColor(@ColorInt int secondaryButtonTextColor) {
            config.secondaryButtonTextColor = secondaryButtonTextColor;
            return this;
        }

        @NonNull
        public Builder metaTextColor(@ColorInt int metaTextColor) {
            config.metaTextColor = metaTextColor;
            return this;
        }

        @NonNull
        public CrashConfig get() {
            return config;
        }

        public void apply() {
            CrashActivity.setConfig(config);
        }
    }
}
