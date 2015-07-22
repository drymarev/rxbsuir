package by.toggi.rxbsuir.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import by.toggi.rxbsuir.R;
import by.toggi.rxbsuir.RxBsuirApplication;
import by.toggi.rxbsuir.adapter.LessonAdapter;
import by.toggi.rxbsuir.component.DaggerWeekFragmentComponent;
import by.toggi.rxbsuir.db.model.Lesson;
import by.toggi.rxbsuir.module.WeekFragmentModule;
import by.toggi.rxbsuir.mvp.presenter.WeekPresenter;
import by.toggi.rxbsuir.mvp.view.WeekView;

public class WeekFragment extends Fragment implements WeekView {

    public static final String ARGS_WEEK_NUMBER = "week_number";

    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;

    @Inject LinearLayoutManager mLayoutManager;
    @Inject LessonAdapter mAdapter;
    @Inject WeekPresenter mPresenter;

    private int mWeekNumber;

    public static WeekFragment newInstance(int weekNumber) {
        Bundle args = new Bundle();
        args.putInt(ARGS_WEEK_NUMBER, weekNumber);
        WeekFragment fragment = new WeekFragment();
        fragment.setArguments(args);
        return fragment;
    }

    // TODO Save and restore scroll position on orientation change

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle args = getArguments();
        if (args != null) {
            mWeekNumber = args.getInt(ARGS_WEEK_NUMBER);
        }

        DaggerWeekFragmentComponent.builder()
                .appComponent(((RxBsuirApplication) getActivity().getApplication()).getAppComponent())
                .weekFragmentModule(new WeekFragmentModule(this))
                .build().inject(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mPresenter.attachView(this);
        mPresenter.onCreate();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public void showLessonList(List<Lesson> lessonList) {
        ProgressBar progressBar = ButterKnife.findById(getActivity(), R.id.progress_bar);
        TextView textView = ButterKnife.findById(getActivity(), R.id.error_text_view);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (textView != null) {
            textView.setVisibility(View.GONE);
        }
        mAdapter.setLessonList(lessonList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
    }

    /**
     * Gets week number of the fragment.
     *
     * @return the week number
     */
    public int getWeekNumber() {
        return mWeekNumber;
    }
}
