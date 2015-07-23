package by.toggi.rxbsuir.mvp.presenter;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.queries.Query;

import java.util.List;

import javax.inject.Inject;

import by.toggi.rxbsuir.db.RxBsuirContract;
import by.toggi.rxbsuir.mvp.Presenter;
import by.toggi.rxbsuir.mvp.view.AddDialogView;
import by.toggi.rxbsuir.rest.BsuirService;
import by.toggi.rxbsuir.rest.model.StudentGroup;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddDialogPresenter implements Presenter<AddDialogView> {

    private final Observable<List<StudentGroup>> mGroupListObservable;
    private List<String> mGroupNumberList;
    private AddDialogView mAddDialogView;
    private BsuirService mService;
    private StorIOSQLite mStorIOSQLite;
    private Subscription mSubscription;

    @Inject
    public AddDialogPresenter(BsuirService service, StorIOSQLite storIOSQLite) {
        mService = service;
        mStorIOSQLite = storIOSQLite;
        mGroupListObservable = storIOSQLite.get()
                .listOfObjects(StudentGroup.class)
                .withQuery(Query.builder()
                        .table(RxBsuirContract.StudentGroupEntry.TABLE_NAME)
                        .build())
                .prepare()
                .createObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .cache();
    }

    @Override
    public void onCreate() {
        mSubscription = mGroupListObservable.subscribe(studentGroups -> {
            if (studentGroups == null || studentGroups.isEmpty()) {
                getStudentGroupsFromNetwork();
            } else {
                updateStudentGroupListInView(studentGroups);
            }
        });
    }

    private void updateStudentGroupListInView(List<StudentGroup> studentGroupList) {
        Observable.from(studentGroupList)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .map(StudentGroup::toString)
                .toList()
                .subscribe(strings -> {
                    mGroupNumberList = strings;
                    if (isViewAttached()) {
                        mAddDialogView.updateStudentGroupList(mGroupNumberList);
                    }
                });
    }

    private boolean isViewAttached() {
        return mAddDialogView != null;
    }

    private void getStudentGroupsFromNetwork() {
        mService.getStudentGroups()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .map(studentGroupXmlModels -> studentGroupXmlModels.studentGroupList)
                .subscribe(this::saveStudentGroupsToDisk);
    }

    private void saveStudentGroupsToDisk(List<StudentGroup> studentGroupList) {
        mStorIOSQLite.put()
                .objects(studentGroupList)
                .prepare()
                .createObservable()
                .subscribe();
    }

    @Override
    public void onDestroy() {
        if (!mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        detachView();
    }

    @Override
    public void attachView(AddDialogView addDialogView) {
        mAddDialogView = addDialogView;
    }

    @Override
    public void detachView() {
        mAddDialogView = null;
    }

    /**
     * Validates group number.
     *
     * @param groupNumber the group number
     * @return true is group number is valid, false otherwise
     */
    public boolean isValidGroupNumber(String groupNumber) {
        return mGroupNumberList == null || mGroupNumberList.contains(groupNumber);
    }
}