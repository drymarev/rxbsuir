package by.toggi.rxbsuir.fragment;

import android.appwidget.AppWidgetManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import by.toggi.rxbsuir.PreferenceHelper;
import by.toggi.rxbsuir.R;
import by.toggi.rxbsuir.RxBsuirApplication;
import by.toggi.rxbsuir.SyncIdItem;
import by.toggi.rxbsuir.Utils;
import by.toggi.rxbsuir.mvp.presenter.AppWidgetConfigPresenter;
import by.toggi.rxbsuir.mvp.view.AppWidgetConfigView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class AppWidgetConfigFragment extends PreferenceFragment implements AppWidgetConfigView, Preference.OnPreferenceClickListener {

    @Inject AppWidgetConfigPresenter mPresenter;

    private int mAppWidgetId;
    private List<SyncIdItem> mSyncIdItemList = new ArrayList<>();
    private com.f2prateek.rx.preferences.Preference<SyncIdItem> mSyncIdItemPreference;
    private Subscription mSubscription;

    public static AppWidgetConfigFragment newInstance(int id) {
        AppWidgetConfigFragment fragment = new AppWidgetConfigFragment();
        Bundle args = new Bundle();
        args.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ((RxBsuirApplication) getActivity().getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mAppWidgetId = args.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        getPreferenceManager().setSharedPreferencesName(PreferenceHelper.getWidgetPreferencesName(mAppWidgetId));

        mSyncIdItemPreference = PreferenceHelper.getSyncIdItemRxPreference(getActivity(), mAppWidgetId);

        addPreferencesFromResource(R.xml.appwidget_preferences);

        findPreference(PreferenceHelper.WIDGET_SYNC_ID_ITEM).setOnPreferenceClickListener(this);

        mPresenter.attachView(this);
        mPresenter.onCreate();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSubscription = mSyncIdItemPreference.asObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(syncIdItem -> {
                    if (syncIdItem != null) {
                        findPreference(PreferenceHelper.WIDGET_SYNC_ID_ITEM).setSummary(syncIdItem.getTitle());
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.unsubscribe(mSubscription);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.preference_list_view, container, false);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case PreferenceHelper.WIDGET_SYNC_ID_ITEM:
                new MaterialDialog.Builder(getActivity())
                        .title("Select group or employee")
                        .items(getItems())
                        .itemsCallbackSingleChoice(findItemIndex(), (materialDialog, view, i, charSequence) -> {
                            SyncIdItem item = mSyncIdItemList.get(i);
                            mSyncIdItemPreference.set(item);
                            return true;
                        }).build()
                        .show();
                return true;
        }
        return false;
    }

    @Override
    public void updateSyncIdList(List<SyncIdItem> syncIdItemList) {
        mSyncIdItemList.clear();
        mSyncIdItemList.addAll(syncIdItemList);
        mSyncIdItemPreference.set(mSyncIdItemList.get(0));
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

}
