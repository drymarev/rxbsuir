package by.toggi.rxbsuir.fragment;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.widget.Toast;
import by.toggi.rxbsuir.PreferenceHelper;
import by.toggi.rxbsuir.R;
import by.toggi.rxbsuir.SyncIdItem;
import by.toggi.rxbsuir.Utils;
import by.toggi.rxbsuir.dagger.PerFragment;
import by.toggi.rxbsuir.mvp.presenter.AppWidgetConfigPresenter;
import by.toggi.rxbsuir.mvp.presenter.LessonListPresenter.SubgroupFilter;
import by.toggi.rxbsuir.mvp.view.AppWidgetConfigView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;
import dagger.android.ContributesAndroidInjector;
import dagger.android.support.AndroidSupportInjection;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class AppWidgetConfigFragment extends PreferenceFragmentCompat
    implements AppWidgetConfigView, Preference.OnPreferenceClickListener {

  @Inject AppWidgetConfigPresenter mPresenter;
  @Inject com.f2prateek.rx.preferences.Preference.Adapter<SyncIdItem> mAdapter;

  private int mAppWidgetId;
  private final List<SyncIdItem> mSyncIdItemList = new ArrayList<>();
  private com.f2prateek.rx.preferences.Preference<SyncIdItem> mSyncIdItemPreference;
  private com.f2prateek.rx.preferences.Preference<SubgroupFilter> mSubgroupFilterPreference;
  private CompositeSubscription mSubscription;

  public static AppWidgetConfigFragment newInstance(int id) {
    AppWidgetConfigFragment fragment = new AppWidgetConfigFragment();
    Bundle args = new Bundle();
    args.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onAttach(Context context) {
    AndroidSupportInjection.inject(this);
    super.onAttach(context);
  }

  @Override
  public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
    Bundle args = getArguments();
    if (args != null) {
      mAppWidgetId =
          args.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    getPreferenceManager().setSharedPreferencesName(
        PreferenceHelper.getWidgetPreferencesName(mAppWidgetId));

    mSyncIdItemPreference =
        PreferenceHelper.getSyncIdItemRxPreference(getActivity(), mAppWidgetId, mAdapter);
    mSubgroupFilterPreference =
        PreferenceHelper.getSubgroupFilterRxPreference(getActivity(), mAppWidgetId);

    setPreferencesFromResource(R.xml.appwidget_preferences, rootKey);

    findPreference(PreferenceHelper.WIDGET_SYNC_ID_ITEM).setOnPreferenceClickListener(this);
    findPreference(PreferenceHelper.SUBGROUP_FILTER).setOnPreferenceClickListener(this);

    mPresenter.attachView(this);
    mPresenter.onCreate();
  }

  @Override public void onResume() {
    super.onResume();
    mSubscription = new CompositeSubscription(mSyncIdItemPreference.asObservable()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(syncIdItem -> {
          if (syncIdItem != null) {
            findPreference(PreferenceHelper.WIDGET_SYNC_ID_ITEM).setSummary(syncIdItem.getTitle());
          }
        }), mSubgroupFilterPreference.asObservable()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(subgroupFilter -> {
          Preference preference = findPreference(PreferenceHelper.SUBGROUP_FILTER);
          switch (subgroupFilter) {
            case BOTH:
              preference.setSummary(R.string.action_filter_both);
              break;
            case FIRST:
              preference.setSummary(R.string.action_filter_first);
              break;
            case SECOND:
              preference.setSummary(R.string.action_filter_second);
              break;
            case NONE:
              preference.setSummary(R.string.action_filter_none);
              break;
          }
        }));
  }

  @Override public void onPause() {
    super.onPause();
    Utils.unsubscribeComposite(mSubscription);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    mPresenter.onDestroy();
  }

  @Override public boolean onPreferenceClick(Preference preference) {
    switch (preference.getKey()) {
      case PreferenceHelper.WIDGET_SYNC_ID_ITEM:
        new MaterialDialog.Builder(getActivity()).title(R.string.widget_sync_id_item)
            .items(getItems())
            .itemsCallbackSingleChoice(findItemIndex(), (materialDialog, view, i, charSequence) -> {
              SyncIdItem item = mSyncIdItemList.get(i);
              mSyncIdItemPreference.set(item);
              return true;
            })
            .build()
            .show();
        return true;
      case PreferenceHelper.SUBGROUP_FILTER:
        return true;
    }
    return false;
  }

  @Override public void updateSyncIdList(List<SyncIdItem> syncIdItemList) {
    mSyncIdItemList.clear();
    mSyncIdItemList.addAll(syncIdItemList);
    if (mSyncIdItemList.size() > 0) {
      mSyncIdItemPreference.set(mSyncIdItemList.get(0));
    } else {
      Toast.makeText(getActivity(), R.string.widget_empty_synciditem, Toast.LENGTH_LONG).show();
      getActivity().finish();
    }
  }

  private CharSequence[] getItems() {
    int size = mSyncIdItemList.size();
    CharSequence[] charSequences = new CharSequence[size];
    for (int i = 0; i < size; i++) {
      charSequences[i] = mSyncIdItemList.get(i).toString();
    }
    return charSequences;
  }

  private int findItemIndex() {
    return Observable.from(mSyncIdItemList)
        .filter(syncIdItem -> syncIdItem.equals(mSyncIdItemPreference.get()))
        .map(mSyncIdItemList::indexOf)
        .toBlocking()
        .firstOrDefault(0);
  }

  @dagger.Module public interface Module {

    @PerFragment @ContributesAndroidInjector AppWidgetConfigFragment contribute();
  }
}
