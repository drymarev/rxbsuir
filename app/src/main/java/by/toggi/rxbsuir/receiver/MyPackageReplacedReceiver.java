package by.toggi.rxbsuir.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import by.toggi.rxbsuir.PreferenceHelper;
import by.toggi.rxbsuir.service.ReplaceSyncIdService;

public class MyPackageReplacedReceiver extends BroadcastReceiver {

  @Override public void onReceive(@NonNull Context context, @NonNull Intent intent) {
    if (!Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
      return;
    }
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    boolean isFixApplied = prefs.getBoolean(PreferenceHelper.IS_SURPRISE_API_FIX_APPLIED, false);
    if (!isFixApplied) {
      context.startService(new Intent(context, ReplaceSyncIdService.class));
    }
    String isDarkThemeKey = "is_dark_theme";
    boolean isDarkTheme = prefs.getBoolean(isDarkThemeKey, false);
    if (isDarkTheme) {
      prefs.edit()
          .putString(PreferenceHelper.NIGHT_MODE, String.valueOf(AppCompatDelegate.MODE_NIGHT_YES))
          .remove(isDarkThemeKey)
          .apply();
    }
  }
}
