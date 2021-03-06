package by.toggi.rxbsuir;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.IntentCompat;
import android.text.TextUtils;
import android.widget.Toast;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Month;
import org.threeten.bp.ZoneId;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.concurrent.TimeUnit;

import by.toggi.rxbsuir.activity.WeekScheduleActivity;
import by.toggi.rxbsuir.receiver.AppWidgetScheduleProvider;
import by.toggi.rxbsuir.receiver.BootReceiver;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Utils class with all RxBsuir goodies.
 */
public class Utils {

    private Utils() {
    }

    /**
     * Opens play store page or shows a toast if play services are missing.
     *
     * @param context the context
     */
    public static void openPlayStorePage(Context context) {
        try {
            context.startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)
            ));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.play_store_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    public static void openPrivacyPolicyPage(Context context) {
        try {
            Uri uri = Uri.parse("https://drymarev.github.io/rxbsuir/docs/privacy_policy.html");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        } catch (ActivityNotFoundException ignored) {
        }
    }

    /**
     * Gets formatted title.
     *
     * @param titleFormat title format
     * @param strings     strings
     * @return formatted title
     */
    public static String getFormattedTitle(String titleFormat, String... strings) {
        if (strings.length == 3) {
            return String.format(titleFormat, strings);
        } else {
            return TextUtils.join(" ", strings);
        }
    }

    /**
     * Checks for network connection.
     *
     * @param context the context
     * @return the boolean
     */
    public static boolean hasNetworkConnection(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    /**
     * Sets notification alarm.
     *
     * @param context   the context
     * @param localTime the local time
     */
    public static void setNotificationAlarm(Context context, LocalTime localTime) {
        PendingIntent pendingIntent = IntentUtils.getNotificationPendingIntent(context);

        LocalDateTime dateTime;
        if (LocalTime.now().isAfter(localTime)) {
            dateTime = LocalDateTime.of(LocalDate.now().plusDays(1), localTime);
        } else {
            dateTime = LocalDateTime.of(LocalDate.now(), localTime);
        }
        getAlarmManager(context).setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                convertLocalDateTimeToMillis(dateTime),
                TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS),
                pendingIntent
        );
        setBootReceiverEnabled(context, true);
    }

    /**
     * Cancels notification alarm.
     *
     * @param context the context
     */
    public static void cancelNotificationAlarm(Context context) {
        setBootReceiverEnabled(context, false);
        cancelAlarm(context, IntentUtils.getNotificationPendingIntent(context));
    }

    /**
     * Sets widget update alarm.
     *
     * @param context the context
     */
    public static void setWidgetUpdateAlarm(Context context) {
        if (getAppWidgetCount(context) > 0) {
            Timber.d("Setting WidgetUpdateAlarm...");
            LocalDateTime dateTime = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT);

            AlarmManager manager = getAlarmManager(context);
            manager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    convertLocalDateTimeToMillis(dateTime),
                    TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS),
                    IntentUtils.getWidgetUpdatePendingIntent(context)
            );
            setBootReceiverEnabled(context, true);
        }
    }


    /**
     * Cancel widget update alarm.
     *
     * @param context the context
     */
    public static void cancelWidgetUpdateAlarm(Context context) {
        cancelAlarm(context, IntentUtils.getWidgetUpdatePendingIntent(context));
    }


    /**
     * Restarts app.
     *
     * @param activity the activity
     */
    public static void restartApp(Activity activity) {
        activity.finish();
        ComponentName componentName = new ComponentName(activity, WeekScheduleActivity.class);
        final Intent intent = IntentCompat.makeMainActivity(componentName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }

    /**
     * Gets current week number.
     *
     * @return the current week number
     */
    public static int getCurrentWeekNumber() {
        return getWeekNumber(LocalDate.now());
    }

    /**
     * Gets week number.
     *
     * @param localDate the local date
     * @return the week number
     */
    public static int getWeekNumber(LocalDate localDate) {
        Long weeks = ChronoUnit.WEEKS.between(getStartYear(), localDate);
        return weeks.intValue() % 4 + 1;
    }

    /**
     * Convert weekday to {@link DayOfWeek}.
     *
     * @param weekday the weekday
     * @return the day of week
     */
    public static DayOfWeek convertWeekdayToDayOfWeek(@NonNull String weekday) {
        switch (weekday.toLowerCase()) {
            case "понедельник":
                return DayOfWeek.MONDAY;
            case "вторник":
                return DayOfWeek.TUESDAY;
            case "среда":
                return DayOfWeek.WEDNESDAY;
            case "четверг":
                return DayOfWeek.THURSDAY;
            case "пятница":
                return DayOfWeek.FRIDAY;
            case "суббота":
                return DayOfWeek.SATURDAY;
            case "воскресенье":
                return DayOfWeek.SUNDAY;
            default:
                throw new IllegalArgumentException("Unknown weekday: " + weekday);
        }
    }

    /**
     * RxJava unsubscribe helper.
     *
     * @param subscription the subscription
     */
    public static void unsubscribe(Subscription subscription) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    /**
     * RxJava composite subscription unsubscribe helper.
     *
     * @param compositeSubscription {@link CompositeSubscription}
     */
    public static void unsubscribeComposite(CompositeSubscription compositeSubscription) {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed() && compositeSubscription.hasSubscriptions()) {
            compositeSubscription.unsubscribe();
        }
    }

    /**
     * Get app widget ids.
     *
     * @param context the context
     * @return the int [ ]
     */
    public static int[] getAppWidgetIds(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        return manager.getAppWidgetIds(new ComponentName(context, AppWidgetScheduleProvider.class));
    }

    private static LocalDate getStartYear() {
        LocalDate localDate = LocalDate.now();
        if (localDate.getMonth().compareTo(Month.SEPTEMBER) < 0) {
            return LocalDate.of(localDate.getYear() - 1, Month.SEPTEMBER, 1).with(DayOfWeek.MONDAY);
        }
        return LocalDate.of(localDate.getYear(), Month.SEPTEMBER, 1).with(DayOfWeek.MONDAY);
    }

    private static AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    private static void setBootReceiverEnabled(Context context, boolean enabled) {
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager manager = context.getPackageManager();
        manager.setComponentEnabledSetting(
                receiver,
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );
    }

    private static int getAppWidgetCount(Context context) {
        return getAppWidgetIds(context).length;
    }

    private static long convertLocalDateTimeToMillis(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000;
    }

    private static void cancelAlarm(Context context, PendingIntent pendingIntent) {
        getAlarmManager(context).cancel(pendingIntent);
        pendingIntent.cancel();
    }

}
