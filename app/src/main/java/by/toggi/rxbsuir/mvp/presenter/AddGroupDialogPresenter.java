package by.toggi.rxbsuir.mvp.presenter;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.operations.put.PutResults;
import com.pushtorefresh.storio.sqlite.queries.Query;

import java.util.List;

import javax.inject.Inject;

import by.toggi.rxbsuir.Utils;
import by.toggi.rxbsuir.db.RxBsuirContract;
import by.toggi.rxbsuir.mvp.Presenter;
import by.toggi.rxbsuir.mvp.view.AddGroupDialogView;
import by.toggi.rxbsuir.rest.BsuirService;
import by.toggi.rxbsuir.rest.model.StudentGroup;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class AddGroupDialogPresenter extends Presenter<AddGroupDialogView> {

    private final Observable<List<StudentGroup>> mGroupListObservable;
    private final BsuirService mService;
    private final StorIOSQLite mStorIOSQLite;
    private List<String> mStudentGroupStringList;
    private Subscription mSubscription;

    @Inject
    public AddGroupDialogPresenter(BsuirService service, StorIOSQLite storIOSQLite) {
        mService = service;
        mStorIOSQLite = storIOSQLite;
        mGroupListObservable = storIOSQLite.get()
                .listOfObjects(StudentGroup.class)
                .withQuery(Query.builder()
                        .table(RxBsuirContract.StudentGroupEntry.TABLE_NAME)
                        .build())
                .prepare()
                .createObservable()
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void onCreate() {
        mSubscription = mGroupListObservable.subscribe(studentGroupList -> {
            if (studentGroupList == null || studentGroupList.isEmpty()) {
                getStudentGroupsFromNetwork();
            } else {
                updateStudentGroupListInView(studentGroupList);
            }
        });
    }

    @Override
    public void onDestroy() {
        Utils.unsubscribe(mSubscription);
        detachView();
    }

    /**
     * Validates group number.
     *
     * @param groupNumber the group number
     * @return true is group number is valid, false otherwise
     */
    public boolean isValidGroupNumber(String groupNumber) {
        return mStudentGroupStringList != null && mStudentGroupStringList.contains(groupNumber);
    }

    private void updateStudentGroupListInView(List<StudentGroup> studentGroupList) {
        Observable.from(studentGroupList)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .map(StudentGroup::toString)
                .toList()
                .subscribe(strings -> {
                    mStudentGroupStringList = strings;
                    if (isViewAttached()) {
                        getView().updateStudentGroupList(studentGroupList);
                    }
                });
    }

    private void getStudentGroupsFromNetwork() {
        mService.getStudentGroups()
                .doOnEach(notification -> Timber.d(notification.toString()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(this::getStudentGroupPutObservable)
                .subscribe(studentGroupPutResults -> Timber.d("Insert count: %d", studentGroupPutResults.numberOfInserts()), this::onError);
    }

    private Observable<PutResults<StudentGroup>> getStudentGroupPutObservable(List<StudentGroup> studentGroupList) {
        return mStorIOSQLite.put()
                .objects(studentGroupList)
                .prepare()
                .createObservable();
    }

    private void onError(Throwable throwable) {
        if (throwable.getCause().toString().contains("java.net.UnknownHostException")) {
            if (isViewAttached()) {
                getView().showError(SchedulePresenter.Error.NETWORK);
            }
        }
    }
}
