package by.toggi.rxbsuir.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import by.toggi.rxbsuir.PreferenceHelper;
import by.toggi.rxbsuir.service.ReplaceSyncIdService;

public class MyPackageReplacedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        boolean isFixApplied = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PreferenceHelper.IS_SURPRISE_API_FIX_APPLIED, false);
        if (!isFixApplied) {
            context.startService(new Intent(context, ReplaceSyncIdService.class));
        }
    }

}
