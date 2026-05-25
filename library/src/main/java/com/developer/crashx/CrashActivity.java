package com.developer.crashx;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.developer.crashx.activity.DefaultErrorActivity;
import com.developer.crashx.config.CrashConfig;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * CrashX runtime crash handler.
 *
 * <p>Originally derived from CustomActivityOnCrash by Eduard Ereza Martínez.</p>
 * <p>Modernized and maintained as CrashX by TutorialsAndroid.</p>
 */
public final class CrashActivity {

    public static final String VERSION = "7.0.0";

    private static final String TAG = "CrashX";

    private static final String EXTRA_CONFIG = "com.developer.crashx.EXTRA_CONFIG";
    private static final String EXTRA_STACK_TRACE = "com.developer.crashx.EXTRA_STACK_TRACE";
    private static final String EXTRA_ACTIVITY_LOG = "com.developer.crashx.EXTRA_ACTIVITY_LOG";
    private static final String EXTRA_THREAD_NAME = "com.developer.crashx.EXTRA_THREAD_NAME";
    private static final String EXTRA_CRASH_ID = "com.developer.crashx.EXTRA_CRASH_ID";
    private static final String EXTRA_THROWABLE_CLASS = "com.developer.crashx.EXTRA_THROWABLE_CLASS";
    private static final String EXTRA_THROWABLE_MESSAGE = "com.developer.crashx.EXTRA_THROWABLE_MESSAGE";
    private static final String EXTRA_CRASH_DATE = "com.developer.crashx.EXTRA_CRASH_DATE";

    private static final String INTENT_ACTION_ERROR_ACTIVITY = "com.developer.crashx.ERROR";
    private static final String INTENT_ACTION_RESTART_ACTIVITY = "com.developer.crashx.RESTART";
    private static final String DEFAULT_HANDLER_PACKAGE_NAME = "com.android.internal.os";

    private static final String SHARED_PREFERENCES_FILE = "crashx";
    private static final String SHARED_PREFERENCES_FIELD_TIMESTAMP = "last_crash_timestamp";

    private static Application application;
    private static CrashConfig config = new CrashConfig();
    private static final Deque<String> activityLog = new ArrayDeque<>();
    private static WeakReference<Activity> lastActivityCreated = new WeakReference<>(null);
    private static boolean isInBackground = true;
    private static boolean lifecycleCallbacksRegistered = false;

    private CrashActivity() {
        // No instances.
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static synchronized void install(@Nullable final Context context) {
        try {
            if (context == null) {
                Log.e(TAG, "Install failed: context is null.");
                return;
            }

            application = (Application) context.getApplicationContext();
            final Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();

            if (oldHandler instanceof CrashXUncaughtExceptionHandler) {
                Log.i(TAG, "CrashX is already installed.");
                return;
            }

            if (oldHandler != null && !oldHandler.getClass().getName().startsWith(DEFAULT_HANDLER_PACKAGE_NAME)) {
                Log.w(TAG, "Another UncaughtExceptionHandler is already installed. CrashX will preserve it and call it when needed.");
            }

            Thread.setDefaultUncaughtExceptionHandler(new CrashXUncaughtExceptionHandler(oldHandler));

            if (!lifecycleCallbacksRegistered) {
                application.registerActivityLifecycleCallbacks(createLifecycleCallbacks());
                lifecycleCallbacksRegistered = true;
            }

            Log.i(TAG, "CrashX " + VERSION + " has been installed.");
        } catch (Throwable t) {
            Log.e(TAG, "An unknown error occurred while installing CrashX.", t);
        }
    }

    private static final class CrashXUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Nullable
        private final Thread.UncaughtExceptionHandler oldHandler;

        CrashXUncaughtExceptionHandler(@Nullable Thread.UncaughtExceptionHandler oldHandler) {
            this.oldHandler = oldHandler;
        }

        @Override
        public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
            handleUncaughtException(oldHandler, thread, throwable);
        }
    }

    private static void handleUncaughtException(@Nullable Thread.UncaughtExceptionHandler oldHandler,
                                                @NonNull Thread thread,
                                                @NonNull Throwable throwable) {
        if (!config.isEnabled()) {
            callOldHandlerOrKill(oldHandler, thread, throwable);
            return;
        }

        Log.e(TAG, "App crashed. CrashX is preparing the error screen.", throwable);

        try {
            if (application == null) {
                callOldHandlerOrKill(oldHandler, thread, throwable);
                return;
            }

            if (hasCrashedInTheLastSeconds(application)) {
                Log.e(TAG, "Recent crash detected. Skipping CrashX UI to avoid a restart loop.", throwable);
                callOldHandlerOrKill(oldHandler, thread, throwable);
                return;
            }

            setLastCrashTimestamp(application, new Date().getTime());

            Class<? extends Activity> errorActivityClass = config.getErrorActivityClass();
            if (errorActivityClass == null) {
                errorActivityClass = guessErrorActivityClass(application);
            }

            if (isStackTraceLikelyConflictive(throwable, errorActivityClass)) {
                Log.e(TAG, "The Application class or error activity crashed. CrashX UI will not be launched.", throwable);
                callOldHandlerOrKill(oldHandler, thread, throwable);
                return;
            }

            if (config.getBackgroundMode() == CrashConfig.BACKGROUND_MODE_SHOW_CUSTOM || !isInBackground) {
                launchErrorActivity(thread, throwable, errorActivityClass);
            } else if (config.getBackgroundMode() == CrashConfig.BACKGROUND_MODE_CRASH) {
                callOldHandlerOrKill(oldHandler, thread, throwable);
                return;
            }

            finishLastActivity();
            killCurrentProcess();
        } catch (Throwable crashXFailure) {
            Log.e(TAG, "CrashX failed while handling the crash.", crashXFailure);
            callOldHandlerOrKill(oldHandler, thread, throwable);
        }
    }

    private static void launchErrorActivity(@NonNull Thread thread,
                                            @NonNull Throwable throwable,
                                            @NonNull Class<? extends Activity> errorActivityClass) {
        final Intent intent = new Intent(application, errorActivityClass);

        String crashId = createCrashId();
        String crashDate = createDateFormat().format(new Date());

        intent.putExtra(EXTRA_THREAD_NAME, thread.getName());
        intent.putExtra(EXTRA_CRASH_ID, crashId);
        intent.putExtra(EXTRA_THROWABLE_CLASS, throwable.getClass().getName());
        intent.putExtra(EXTRA_THROWABLE_MESSAGE, throwable.getMessage());
        intent.putExtra(EXTRA_CRASH_DATE, crashDate);

        if (config.isIncludeStackTrace()) {
            intent.putExtra(EXTRA_STACK_TRACE, throwableToString(throwable, config.getMaxStackTraceSize()));
        }

        if (config.isTrackActivities() && config.isIncludeActivityLog()) {
            intent.putExtra(EXTRA_ACTIVITY_LOG, snapshotActivityLog());
        }

        if (config.isShowRestartButton() && config.getRestartActivityClass() == null) {
            config.setRestartActivityClass(guessRestartActivityClass(application));
        }

        intent.putExtra(EXTRA_CONFIG, config);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        notifyLaunchErrorActivity();

        application.startActivity(intent);
    }

    @NonNull
    private static Application.ActivityLifecycleCallbacks createLifecycleCallbacks() {
        return new Application.ActivityLifecycleCallbacks() {
            int currentlyStartedActivities = 0;
            final DateFormat dateFormat = createDateFormat();

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                if (!isCrashXErrorActivity(activity)) {
                    lastActivityCreated = new WeakReference<>(activity);
                }
                appendActivityLog(dateFormat.format(new Date()) + ": " + activity.getClass().getSimpleName() + " created\n");
            }

            @Override
            public void onActivityStarted(Activity activity) {
                currentlyStartedActivities++;
                isInBackground = false;
            }

            @Override
            public void onActivityResumed(Activity activity) {
                appendActivityLog(dateFormat.format(new Date()) + ": " + activity.getClass().getSimpleName() + " resumed\n");
            }

            @Override
            public void onActivityPaused(Activity activity) {
                appendActivityLog(dateFormat.format(new Date()) + ": " + activity.getClass().getSimpleName() + " paused\n");
            }

            @Override
            public void onActivityStopped(Activity activity) {
                currentlyStartedActivities = Math.max(0, currentlyStartedActivities - 1);
                isInBackground = currentlyStartedActivities == 0;
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                // No-op.
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                appendActivityLog(dateFormat.format(new Date()) + ": " + activity.getClass().getSimpleName() + " destroyed\n");
            }
        };
    }

    private static boolean isCrashXErrorActivity(@NonNull Activity activity) {
        Class<? extends Activity> configuredErrorActivity = config.getErrorActivityClass();
        return activity.getClass() == DefaultErrorActivity.class
                || (configuredErrorActivity != null && activity.getClass() == configuredErrorActivity);
    }

    private static void appendActivityLog(@NonNull String line) {
        if (!config.isTrackActivities() || !config.isIncludeActivityLog()) {
            return;
        }
        while (activityLog.size() >= config.getMaxActivityLogEntries()) {
            activityLog.pollFirst();
        }
        activityLog.offerLast(line);
    }

    @NonNull
    private static String snapshotActivityLog() {
        StringBuilder builder = new StringBuilder();
        for (String entry : activityLog) {
            builder.append(entry);
        }
        return builder.toString();
    }

    @NonNull
    private static String throwableToString(@NonNull Throwable throwable, int maxSize) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String stackTraceString = sw.toString();

        if (stackTraceString.length() > maxSize) {
            String disclaimer = "\n[stack trace trimmed by CrashX]";
            int safeLength = Math.max(0, maxSize - disclaimer.length());
            stackTraceString = stackTraceString.substring(0, safeLength) + disclaimer;
        }
        return stackTraceString;
    }

    @Nullable
    public static String getStackTraceFromIntent(@NonNull Intent intent) {
        return intent.getStringExtra(EXTRA_STACK_TRACE);
    }

    @Nullable
    public static String getActivityLogFromIntent(@NonNull Intent intent) {
        return intent.getStringExtra(EXTRA_ACTIVITY_LOG);
    }

    @NonNull
    public static String getThreadNameFromIntent(@NonNull Intent intent) {
        String threadName = intent.getStringExtra(EXTRA_THREAD_NAME);
        return threadName == null ? "unknown" : threadName;
    }

    @NonNull
    public static String getCrashIdFromIntent(@NonNull Intent intent) {
        String crashId = intent.getStringExtra(EXTRA_CRASH_ID);
        return crashId == null ? "unknown" : crashId;
    }

    @NonNull
    public static String getThrowableClassFromIntent(@NonNull Intent intent) {
        String throwableClass = intent.getStringExtra(EXTRA_THROWABLE_CLASS);
        return throwableClass == null ? "unknown" : throwableClass;
    }

    @Nullable
    public static String getThrowableMessageFromIntent(@NonNull Intent intent) {
        return intent.getStringExtra(EXTRA_THROWABLE_MESSAGE);
    }

    @NonNull
    public static String getCrashDateFromIntent(@NonNull Intent intent) {
        String crashDate = intent.getStringExtra(EXTRA_CRASH_DATE);
        return crashDate == null ? createDateFormat().format(new Date()) : crashDate;
    }

    @NonNull
    public static CrashConfig getConfigFromIntent(@NonNull Intent intent) {
        Serializable serializable = intent.getSerializableExtra(EXTRA_CONFIG);
        CrashConfig crashConfig = serializable instanceof CrashConfig ? (CrashConfig) serializable : null;

        if (crashConfig == null) {
            crashConfig = getConfig();
        }

        if (crashConfig.isLogErrorOnRestart()) {
            String stackTrace = getStackTraceFromIntent(intent);
            if (stackTrace != null) {
                Log.e(TAG, "The previous app process crashed. Stack trace:\n" + stackTrace);
            }
        }

        return crashConfig;
    }

    @NonNull
    public static String getAllErrorDetailsFromIntent(@NonNull Context context, @NonNull Intent intent) {
        CrashConfig crashConfig = getConfigFromIntent(intent);
        StringBuilder errorDetails = new StringBuilder();

        if (crashConfig.isIncludeCrashId()) {
            errorDetails.append("Crash ID: ").append(getCrashIdFromIntent(intent)).append("\n");
        }

        errorDetails.append("Crash date: ").append(getCrashDateFromIntent(intent)).append("\n");
        errorDetails.append("Crash thread: ").append(getThreadNameFromIntent(intent)).append("\n");
        errorDetails.append("Exception: ").append(getThrowableClassFromIntent(intent)).append("\n");

        String throwableMessage = getThrowableMessageFromIntent(intent);
        if (throwableMessage != null && throwableMessage.trim().length() > 0) {
            errorDetails.append("Message: ").append(throwableMessage).append("\n");
        }

        errorDetails.append("\n");

        if (crashConfig.isIncludeDeviceInfo()) {
            errorDetails.append("Package: ").append(context.getPackageName()).append("\n");
            errorDetails.append("App version: ").append(getVersionName(context)).append(" (").append(getVersionCode(context)).append(")\n");
            errorDetails.append("CrashX version: ").append(VERSION).append("\n");
            if (crashConfig.isIncludeBuildDate()) {
                String buildDate = getBuildDateAsString(context, createDateFormat());
                if (buildDate != null) {
                    errorDetails.append("Build date: ").append(buildDate).append("\n");
                }
            }
            errorDetails.append("Android: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
            errorDetails.append("Device: ").append(getDeviceModelName()).append("\n");
            errorDetails.append("Brand: ").append(Build.BRAND).append("\n");
            errorDetails.append("Manufacturer: ").append(Build.MANUFACTURER).append("\n");
            errorDetails.append("\n");
        }

        String additionalInfo = crashConfig.getAdditionalReportInfo();
        if (additionalInfo != null) {
            errorDetails.append("Additional information:\n");
            errorDetails.append(additionalInfo).append("\n\n");
        }

        if (crashConfig.isIncludeStackTrace()) {
            errorDetails.append("Stack trace:\n");
            String stackTrace = getStackTraceFromIntent(intent);
            errorDetails.append(stackTrace == null ? "No stack trace available." : stackTrace);
            errorDetails.append("\n");
        }

        String activityLog = getActivityLogFromIntent(intent);
        if (crashConfig.isIncludeActivityLog() && activityLog != null && activityLog.trim().length() > 0) {
            errorDetails.append("\nActivity log:\n");
            errorDetails.append(activityLog);
        }

        return errorDetails.toString();
    }

    /**
     * Backward-compatible overload. Prefer {@link #getAllErrorDetailsFromIntent(Context, Intent)}.
     */
    @NonNull
    @Deprecated
    public static String getAllErrorDetailsFromIntent(@NonNull Intent intent) {
        StringBuilder errorDetails = new StringBuilder();
        errorDetails.append("Crash ID: ").append(getCrashIdFromIntent(intent)).append("\n");
        errorDetails.append("Crash date: ").append(getCrashDateFromIntent(intent)).append("\n");
        errorDetails.append("Crash thread: ").append(getThreadNameFromIntent(intent)).append("\n");
        errorDetails.append("Exception: ").append(getThrowableClassFromIntent(intent)).append("\n\n");
        errorDetails.append("Stack trace:\n");
        String stackTrace = getStackTraceFromIntent(intent);
        errorDetails.append(stackTrace == null ? "No stack trace available." : stackTrace);
        return errorDetails.toString();
    }

    private static void restartApplicationWithIntentInternal(@NonNull Activity activity,
                                                             @NonNull Intent intent,
                                                             @NonNull CrashConfig crashConfig) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        if (intent.getComponent() != null) {
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
        }

        notifyRestartAppFromErrorActivity(crashConfig);

        activity.finish();
        activity.startActivity(intent);
        killCurrentProcess();
    }

    public static void restartApplication(@NonNull Activity activity, @NonNull CrashConfig crashConfig) {
        Class<? extends Activity> restartActivityClass = crashConfig.getRestartActivityClass();
        if (restartActivityClass == null) {
            restartActivityClass = guessRestartActivityClass(activity.getApplicationContext());
        }

        if (restartActivityClass == null) {
            closeApplication(activity, crashConfig);
            return;
        }

        Intent intent = new Intent(activity, restartActivityClass);
        restartApplicationWithIntentInternal(activity, intent, crashConfig);
    }

    public static void restartApplicationWithIntent(@NonNull Activity activity,
                                                    @NonNull Intent intent,
                                                    @NonNull CrashConfig crashConfig) {
        restartApplicationWithIntentInternal(activity, intent, crashConfig);
    }

    public static void closeApplication(@NonNull Activity activity, @NonNull CrashConfig crashConfig) {
        notifyCloseAppFromErrorActivity(crashConfig);
        activity.finish();
        killCurrentProcess();
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    @NonNull
    public static CrashConfig getConfig() {
        return config;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void setConfig(@NonNull CrashConfig crashConfig) {
        CrashActivity.config = crashConfig;
    }

    private static boolean isStackTraceLikelyConflictive(@NonNull Throwable throwable,
                                                         @NonNull Class<? extends Activity> activityClass) {
        Throwable currentThrowable = throwable;
        do {
            StackTraceElement[] stackTrace = currentThrowable.getStackTrace();
            for (StackTraceElement element : stackTrace) {
                if (("android.app.ActivityThread".equals(element.getClassName())
                        && "handleBindApplication".equals(element.getMethodName()))
                        || element.getClassName().equals(activityClass.getName())) {
                    return true;
                }
            }
            currentThrowable = currentThrowable.getCause();
        } while (currentThrowable != null);
        return false;
    }

    @Nullable
    private static String getBuildDateAsString(@NonNull Context context, @NonNull DateFormat dateFormat) {
        ZipFile zipFile = null;
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            zipFile = new ZipFile(applicationInfo.sourceDir);
            ZipEntry zipEntry = zipFile.getEntry("classes.dex");
            if (zipEntry == null) {
                return null;
            }
            long buildDate = zipEntry.getTime();
            if (buildDate > 312764400000L) {
                return dateFormat.format(new Date(buildDate));
            }
        } catch (Exception ignored) {
            return null;
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (Exception ignored) {
                    // No-op.
                }
            }
        }
        return null;
    }

    @NonNull
    private static String getVersionName(@NonNull Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName == null ? "Unknown" : packageInfo.versionName;
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private static long getVersionCode(@NonNull Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return packageInfo.getLongVersionCode();
            }
            return packageInfo.versionCode;
        } catch (Exception e) {
            return -1;
        }
    }

    @NonNull
    private static String getDeviceModelName() {
        String manufacturer = Build.MANUFACTURER == null ? "" : Build.MANUFACTURER;
        String model = Build.MODEL == null ? "" : Build.MODEL;
        if (model.toLowerCase(Locale.US).startsWith(manufacturer.toLowerCase(Locale.US))) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    @NonNull
    private static String capitalize(@Nullable String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        char first = value.charAt(0);
        if (Character.isUpperCase(first)) {
            return value;
        }
        return Character.toUpperCase(first) + value.substring(1);
    }

    @Nullable
    private static Class<? extends Activity> guessRestartActivityClass(@NonNull Context context) {
        Class<? extends Activity> resolvedActivityClass = getRestartActivityClassWithIntentFilter(context);
        if (resolvedActivityClass == null) {
            resolvedActivityClass = getLauncherActivity(context);
        }
        return resolvedActivityClass;
    }

    @Nullable
    private static Class<? extends Activity> getRestartActivityClassWithIntentFilter(@NonNull Context context) {
        Intent searchedIntent = new Intent().setAction(INTENT_ACTION_RESTART_ACTIVITY).setPackage(context.getPackageName());
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(
                searchedIntent,
                PackageManager.GET_RESOLVED_FILTER
        );

        if (resolveInfos != null && resolveInfos.size() > 0) {
            ResolveInfo resolveInfo = resolveInfos.get(0);
            try {
                return (Class<? extends Activity>) Class.forName(resolveInfo.activityInfo.name);
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Failed to resolve restart activity class from intent filter.", e);
            }
        }
        return null;
    }

    @Nullable
    private static Class<? extends Activity> getLauncherActivity(@NonNull Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (intent != null && intent.getComponent() != null) {
            try {
                return (Class<? extends Activity>) Class.forName(intent.getComponent().getClassName());
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Failed to resolve launcher activity class.", e);
            }
        }
        return null;
    }

    @NonNull
    private static Class<? extends Activity> guessErrorActivityClass(@NonNull Context context) {
        Class<? extends Activity> resolvedActivityClass = getErrorActivityClassWithIntentFilter(context);
        return resolvedActivityClass == null ? DefaultErrorActivity.class : resolvedActivityClass;
    }

    @Nullable
    private static Class<? extends Activity> getErrorActivityClassWithIntentFilter(@NonNull Context context) {
        Intent searchedIntent = new Intent().setAction(INTENT_ACTION_ERROR_ACTIVITY).setPackage(context.getPackageName());
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(
                searchedIntent,
                PackageManager.GET_RESOLVED_FILTER
        );

        if (resolveInfos != null && resolveInfos.size() > 0) {
            ResolveInfo resolveInfo = resolveInfos.get(0);
            try {
                return (Class<? extends Activity>) Class.forName(resolveInfo.activityInfo.name);
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Failed to resolve error activity class from intent filter.", e);
            }
        }
        return null;
    }

    private static void finishLastActivity() {
        Activity lastActivity = lastActivityCreated.get();
        if (lastActivity != null) {
            lastActivity.finish();
            lastActivityCreated.clear();
        }
    }

    private static void callOldHandlerOrKill(@Nullable Thread.UncaughtExceptionHandler oldHandler,
                                             @NonNull Thread thread,
                                             @NonNull Throwable throwable) {
        if (oldHandler != null) {
            oldHandler.uncaughtException(thread, throwable);
            return;
        }
        killCurrentProcess();
    }

    private static void notifyLaunchErrorActivity() {
        try {
            if (config.getEventListener() != null) {
                config.getEventListener().onLaunchErrorActivity();
            }
        } catch (Throwable listenerError) {
            Log.e(TAG, "CrashX event listener failed on launch.", listenerError);
        }
    }

    private static void notifyRestartAppFromErrorActivity(@NonNull CrashConfig crashConfig) {
        try {
            if (crashConfig.getEventListener() != null) {
                crashConfig.getEventListener().onRestartAppFromErrorActivity();
            }
        } catch (Throwable listenerError) {
            Log.e(TAG, "CrashX event listener failed on restart.", listenerError);
        }
    }

    private static void notifyCloseAppFromErrorActivity(@NonNull CrashConfig crashConfig) {
        try {
            if (crashConfig.getEventListener() != null) {
                crashConfig.getEventListener().onCloseAppFromErrorActivity();
            }
        } catch (Throwable listenerError) {
            Log.e(TAG, "CrashX event listener failed on close.", listenerError);
        }
    }

    @SuppressLint("ApplySharedPref")
    private static void setLastCrashTimestamp(@NonNull Context context, long timestamp) {
        context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
                .edit()
                .putLong(SHARED_PREFERENCES_FIELD_TIMESTAMP, timestamp)
                .commit();
    }

    private static long getLastCrashTimestamp(@NonNull Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
                .getLong(SHARED_PREFERENCES_FIELD_TIMESTAMP, -1);
    }

    private static boolean hasCrashedInTheLastSeconds(@NonNull Context context) {
        int minTime = config.getMinTimeBetweenCrashesMs();
        if (minTime <= 0) {
            return false;
        }
        long lastTimestamp = getLastCrashTimestamp(context);
        long currentTimestamp = new Date().getTime();
        return lastTimestamp <= currentTimestamp && currentTimestamp - lastTimestamp < minTime;
    }

    @NonNull
    private static String createCrashId() {
        String prefix = config.getCrashIdPrefix();
        String shortId = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.US);
        return prefix + "-" + shortId;
    }

    @NonNull
    private static DateFormat createDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    }

    private static void killCurrentProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    public interface EventListener extends Serializable {
        void onLaunchErrorActivity();

        void onRestartAppFromErrorActivity();

        void onCloseAppFromErrorActivity();
    }
}
