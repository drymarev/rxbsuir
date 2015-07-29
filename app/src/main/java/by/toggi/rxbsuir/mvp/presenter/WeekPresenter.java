package by.toggi.rxbsuir.mvp.presenter;

import android.support.annotation.Nullable;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.queries.Query;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import by.toggi.rxbsuir.activity.ScheduleActivity;
import by.toggi.rxbsuir.db.model.Lesson;
import by.toggi.rxbsuir.mvp.Presenter;
import by.toggi.rxbsuir.mvp.view.WeekView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

import static by.toggi.rxbsuir.db.RxBsuirContract.LessonEntry;

public class WeekPresenter implements Presenter<WeekView> {

    private final StorIOSQLite mStorIOSQLite;
    private final int mWeekNumber;
    private WeekView mWeekView;
    private Observable<List<Lesson>> mScheduleObservable;
    private int mSubgroupNumber = 0;
    private String mGroupNumber;
    private String mEmployeeId;
    private Subscription mSubscription;

    @Inject
    public WeekPresenter(boolean isGroupSchedule, @Nullable @Named(ScheduleActivity.KEY_GROUP_NUMBER) String groupNumber, @Nullable @Named(ScheduleActivity.KEY_EMPLOYEE_ID) String employeeId, int weekNumber, StorIOSQLite storIOSQLite) {
        mWeekNumber = weekNumber;
        mEmployeeId = employeeId;
        mStorIOSQLite = storIOSQLite;
        mGroupNumber = groupNumber;
        if (isGroupSchedule) {
            mScheduleObservable = getGroupScheduleObservable(mGroupNumber, mSubgroupNumber, mWeekNumber);
        } else {
            mScheduleObservable = getEmployeeListObservable(mEmployeeId, mSubgroupNumber, mWeekNumber);
        }
    }


    /**
     * Sets group number and updates the list.
     *
     * @param groupNumber the group number
     */
    public void setGroupNumber(String groupNumber) {
        mGroupNumber = groupNumber;
        mScheduleObservable = getGroupScheduleObservable(mGroupNumber, mSubgroupNumber, mWeekNumber);
        onCreate();
    }

    /**
     * Sets employee id and updates the list.
     *
     * @param employeeId the employee id
     */
    public void setEmployeeId(String employeeId) {
        mEmployeeId = employeeId;
        mScheduleObservable = getEmployeeListObservable(mEmployeeId, mSubgroupNumber, mWeekNumber);
        onCreate();
    }

    /**
     * Sets subgroup number.
     *
     * @param subgroup1 the subgroup 1 state
     * @param subgroup2 the subgroup 2 state
     */
    public void setSubgroupNumber(boolean subgroup1, boolean subgroup2, boolean isGroupSchedule) {
        if (subgroup1 && subgroup2) {
            mSubgroupNumber = 0;
        } else if (!subgroup1 && !subgroup2) {
            mSubgroupNumber = 3;
        } else {
            if (subgroup1) {
                mSubgroupNumber = 1;
            } else {
                mSubgroupNumber = 2;
            }
        }
        if (isGroupSchedule) {
            mScheduleObservable = getGroupScheduleObservable(mGroupNumber, mSubgroupNumber, mWeekNumber);
        } else {
            mScheduleObservable = getEmployeeListObservable(mEmployeeId, mSubgroupNumber, mWeekNumber);
        }
        onCreate();
    }

    private Observable<List<Lesson>> getGroupScheduleObservable(@Nullable String groupNumber, int subgroupNumber, int weekNumber) {
        return groupNumber == null ? Observable.empty() : mStorIOSQLite.get()
                .listOfObjects(Lesson.class)
                .withQuery(Query.builder()
                        .table(LessonEntry.TABLE_NAME)
                        .where(LessonEntry.filterByGroupSubgroupAndWeek(groupNumber, subgroupNumber, weekNumber))
                        .build())
                .prepare()
                .createObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .cache();
    }

    private Observable<List<Lesson>> getEmployeeListObservable(@Nullable String employeeId, int subgroupNumber, int weekNumber) {
        return employeeId == null ? Observable.empty() : mStorIOSQLite.get()
                .listOfObjects(Lesson.class)
                .withQuery(Query.builder()
                        .table(LessonEntry.TABLE_NAME)
                        .where(LessonEntry.filterByEmployeeSubgroupAndWeek(employeeId, subgroupNumber, weekNumber))
                        .build())
                .prepare()
                .createObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .cache();
    }

    @Override
    public void onCreate() {
        mSubscription = mScheduleObservable.subscribe(this::showLessonList);
    }

    private void showLessonList(List<Lesson> lessonList) {
        if (isViewAttached()) {
            mWeekView.showLessonList(lessonList);
        }
    }

    @Override
    public void onDestroy() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        detachView();
    }

    @Override
    public void attachView(WeekView weekView) {
        if (weekView == null) {
            throw new NullPointerException("WeekView should not be null");
        }
        mWeekView = weekView;
    }

    @Override
    public void detachView() {
        mWeekView = null;
    }

    private boolean isViewAttached() {
        return mWeekView != null;
    }

}
