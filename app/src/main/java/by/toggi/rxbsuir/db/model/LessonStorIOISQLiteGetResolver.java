package by.toggi.rxbsuir.db.model;

import android.database.Cursor;
import android.support.annotation.NonNull;
import by.toggi.rxbsuir.rest.model.Employee;
import com.google.gson.Gson;
import com.pushtorefresh.storio.sqlite.operations.get.DefaultGetResolver;
import java.util.ArrayList;
import java.util.Arrays;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalTime;

import static by.toggi.rxbsuir.db.RxBsuirContract.LessonEntry;

public class LessonStorIOISQLiteGetResolver extends DefaultGetResolver<Lesson> {

  private final Gson mGson;

  public LessonStorIOISQLiteGetResolver(Gson gson) {
    this.mGson = gson;
  }

  @NonNull @Override public Lesson mapFromCursor(@NonNull Cursor cursor) {
    String[] auditoryArray =
        mGson.fromJson(cursor.getString(cursor.getColumnIndex(LessonEntry.COL_AUDITORY_LIST)),
            String[].class);
    if (auditoryArray == null) {
      auditoryArray = new String[] {};
    }
    Employee[] employeeArray =
        mGson.fromJson(cursor.getString(cursor.getColumnIndex(LessonEntry.COL_EMPLOYEE_LIST)),
            Employee[].class);
    if (employeeArray == null) {
      employeeArray = new Employee[] {};
    }
    String[] studentGroupArray =
        mGson.fromJson(cursor.getString(cursor.getColumnIndex(LessonEntry.COL_STUDENT_GROUP_LIST)),
            String[].class);
    Integer[] weekNumberArray =
        mGson.fromJson(cursor.getString(cursor.getColumnIndex(LessonEntry.COL_WEEK_NUMBER_LIST)),
            Integer[].class);

    return new Lesson(cursor.getLong(cursor.getColumnIndex(LessonEntry._ID)),
        cursor.getString(cursor.getColumnIndex(LessonEntry.COL_SYNC_ID)),
        new ArrayList<>(Arrays.asList(auditoryArray)),
        new ArrayList<>(Arrays.asList(employeeArray)),
        LocalTime.parse(cursor.getString(cursor.getColumnIndex(LessonEntry.COL_LESSON_TIME_START))),
        LocalTime.parse(cursor.getString(cursor.getColumnIndex(LessonEntry.COL_LESSON_TIME_END))),
        cursor.getString(cursor.getColumnIndex(LessonEntry.COL_LESSON_TYPE)),
        cursor.getString(cursor.getColumnIndex(LessonEntry.COL_NOTE)),
        cursor.getInt(cursor.getColumnIndex(LessonEntry.COL_NUM_SUBGROUP)),
        new ArrayList<>(Arrays.asList(studentGroupArray)),
        cursor.getString(cursor.getColumnIndex(LessonEntry.COL_SUBJECT)),
        new ArrayList<>(Arrays.asList(weekNumberArray)),
        DayOfWeek.valueOf(cursor.getString(cursor.getColumnIndex(LessonEntry.COL_WEEKDAY))),
        cursor.getInt(cursor.getColumnIndex(LessonEntry.COL_IS_GROUP_SCHEDULE)) != 0);
  }
}
