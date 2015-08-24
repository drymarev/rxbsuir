package by.toggi.rxbsuir.module;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import javax.inject.Named;
import javax.inject.Singleton;

import by.toggi.rxbsuir.PreferenceHelper;
import by.toggi.rxbsuir.R;
import by.toggi.rxbsuir.RxBsuirApplication;
import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private final RxBsuirApplication mApplication;

    public AppModule(RxBsuirApplication application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    RxBsuirApplication provideAppContext() {
        return mApplication;
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mApplication);
    }

    @Provides
    @Named(PreferenceHelper.IS_GROUP_SCHEDULE)
    boolean provideIsGroupSchedule(SharedPreferences preferences) {
        return preferences.getBoolean(PreferenceHelper.IS_GROUP_SCHEDULE, true);
    }

    @Provides
    @Named(PreferenceHelper.IS_DARK_THEME)
    boolean provideIsDarkTheme(SharedPreferences preferences) {
        return preferences.getBoolean(PreferenceHelper.IS_DARK_THEME, false);
    }

    @Nullable
    @Provides
    @Named(PreferenceHelper.SYNC_ID)
    String provideSyncId(SharedPreferences preferences) {
        return preferences.getString(PreferenceHelper.SYNC_ID, null);
    }

    @Provides
    @Singleton
    RxSharedPreferences provideRxSharedPreferences(SharedPreferences preferences) {
        return RxSharedPreferences.create(preferences);
    }

    @Provides
    @Singleton
    @Named(PreferenceHelper.SYNC_ID)
    Preference<String> provideRxSyncId(RxSharedPreferences preferences) {
        return preferences.getString(PreferenceHelper.SYNC_ID, null);
    }

    @Provides
    @Singleton
    Preference<Boolean> provideRxIsGroupSchedule(RxSharedPreferences preferences) {
        return preferences.getBoolean(PreferenceHelper.IS_GROUP_SCHEDULE, true);
    }

    @Provides
    @Singleton
    @Named(PreferenceHelper.TITLE)
    Preference<String> provideRxTitle(RxSharedPreferences preferences) {
        return preferences.getString(PreferenceHelper.TITLE, mApplication.getString(R.string.app_name));
    }

}
