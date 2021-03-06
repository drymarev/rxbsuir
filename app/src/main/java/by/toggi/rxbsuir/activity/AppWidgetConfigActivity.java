package by.toggi.rxbsuir.activity;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RemoteViews;
import butterknife.BindView;
import butterknife.ButterKnife;
import by.toggi.rxbsuir.PreferenceHelper;
import by.toggi.rxbsuir.R;
import by.toggi.rxbsuir.SyncIdItem;
import by.toggi.rxbsuir.dagger.PerActivity;
import by.toggi.rxbsuir.fragment.AppWidgetConfigFragment;
import by.toggi.rxbsuir.receiver.AppWidgetScheduleProvider;
import com.f2prateek.rx.preferences.Preference;
import com.trello.rxlifecycle.android.RxLifecycleAndroid;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.ContributesAndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import javax.inject.Inject;
import javax.inject.Named;
import timber.log.Timber;

public class AppWidgetConfigActivity extends RxAppCompatActivity
    implements HasSupportFragmentInjector {

  private final Intent mResultIntent = new Intent();
  @BindView(R.id.toolbar) Toolbar mToolbar;
  @Inject DispatchingAndroidInjector<Fragment> mDispatchingFragmentInjector;
  @Inject @Named(PreferenceHelper.NIGHT_MODE) Preference<String> mNightModePreference;
  @Inject Preference.Adapter<SyncIdItem> mAdapter;
  private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
  private int mResult = RESULT_CANCELED;

  @Override protected void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_appwidget_config);

    ButterKnife.bind(this);

    setupToolbar();

    Intent intent = getIntent();
    Bundle extras = intent.getExtras();
    if (extras != null) {
      mAppWidgetId =
          extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    mResultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
    setResult(mResult, mResultIntent);

    if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish();
    }

    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_fragment_settings,
            AppWidgetConfigFragment.newInstance(mAppWidgetId))
        .commit();

    mNightModePreference.asObservable()
        .skip(1)
        .compose(RxLifecycleAndroid.bindActivity(lifecycle()))
        .subscribe(mode -> recreate());
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_appwidget_config_activity, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_done:
        RemoteViews remoteViews = AppWidgetScheduleProvider.getRemoteViews(this, mAppWidgetId, mAdapter);
        if (remoteViews != null) {
          AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
          appWidgetManager.updateAppWidget(mAppWidgetId, remoteViews);
        }

        setResult(mResult = RESULT_OK, mResultIntent);
        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (mResult == RESULT_CANCELED) {
      try {
        // Phantom widget fix (kinda)
        AppWidgetHost host = new AppWidgetHost(getApplicationContext(), Integer.MAX_VALUE);
        host.deleteAppWidgetId(mAppWidgetId);
      } catch (Exception e) {
        Timber.e(e, "Widget configuration canceled, id deletion went wrong");
      }
    }
  }

  @Override public void onBackPressed() {
    super.onBackPressed();
    finish();
  }

  @Override public AndroidInjector<Fragment> supportFragmentInjector() {
    return mDispatchingFragmentInjector;
  }

  private void setupToolbar() {
    getDelegate().setSupportActionBar(mToolbar);
    getDelegate().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    mToolbar.setNavigationIcon(R.drawable.ic_action_discard);
    mToolbar.setNavigationOnClickListener(view -> finish());
  }

  @dagger.Module(includes = AppWidgetConfigFragment.Module.class) public interface Module {

    @PerActivity @ContributesAndroidInjector AppWidgetConfigActivity contribute();
  }
}
