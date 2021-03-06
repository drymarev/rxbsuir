package by.toggi.rxbsuir.db.model;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.operations.delete.DefaultDeleteResolver;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;

import static by.toggi.rxbsuir.db.RxBsuirContract.LessonEntry;

public class LessonStorIOSQLiteDeleteResolver extends DefaultDeleteResolver<Lesson> {

    @NonNull
    @Override
    protected DeleteQuery mapToDeleteQuery(@NonNull Lesson object) {
        return DeleteQuery.builder()
                .table(LessonEntry.TABLE_NAME)
                .where(LessonEntry._ID + " = ?")
                .whereArgs(object.getId())
                .build();
    }

}
