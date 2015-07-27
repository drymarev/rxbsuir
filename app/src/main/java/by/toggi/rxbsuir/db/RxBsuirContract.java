package by.toggi.rxbsuir.db;

import android.provider.BaseColumns;

public class RxBsuirContract {

    public static class EmployeeEntry {

        public static final String TABLE_NAME = "employees";

        public static final String COL_ID = "id";
        public static final String COL_ACADEMIC_DEPARTMENT_LIST = "academic_department_list";
        public static final String COL_FIRST_NAME = "first_name";
        public static final String COL_MIDDLE_NAME = "middle_name";
        public static final String COL_LAST_NAME = "last_name";

    }

    public static class StudentGroupEntry {

        public static final String TABLE_NAME = "students_groups";

        public static final String COL_ID = "id";
        public static final String COL_NAME = "name";
        public static final String COL_COURSE = "course";
        public static final String COL_FACULTY_ID = "faculty_id";
        public static final String COL_SPECIALITY_DEPARTMENT_EDUCATION_FORM_ID = "speciality_department_education_form_id";

    }

    public static class LessonEntry implements BaseColumns {

        public static final String TABLE_NAME = "lessons";

        public static final String COL_WEEKDAY = "weekday";
        public static final String COL_WEEK_NUMBER_LIST = "week_number_list";
        public static final String COL_SUBJECT = "subject";
        public static final String COL_STUDENT_GROUP_LIST = "student_group_list";
        public static final String COL_NUM_SUBGROUP = "num_subgroup";
        public static final String COL_NOTE = "note";
        public static final String COL_LESSON_TIME = "lesson_time";
        public static final String COL_LESSON_TYPE = "lesson_type";
        public static final String COL_EMPLOYEE_LIST = "employee_list";
        public static final String COL_AUDITORY_LIST = "auditory_list";
        public static final String COL_IS_GROUP_SCHEDULE = "is_group_schedule";

        public static String filterByWeek(int weekNumber) {
            return COL_WEEK_NUMBER_LIST + " like '%" + weekNumber + "%'";
        }

        public static String filterByGroup(String groupNumber) {
            return COL_STUDENT_GROUP_LIST + " like '%" + groupNumber + "%'";
        }

        public static String filterBySubgroup(int subgroupNumber) {
            String commonQuery = COL_NUM_SUBGROUP + " = 0";
            String subgroup1Query = COL_NUM_SUBGROUP + " = 1";
            String subgroup2Query = COL_NUM_SUBGROUP + " = 2";
            switch (subgroupNumber) {
                case 0:
                    return commonQuery + " or " + subgroup1Query + " or " + subgroup2Query;
                case 1:
                    return commonQuery + " or " + subgroup1Query;
                case 2:
                    return commonQuery + " or " + subgroup2Query;
                case 3:
                    return commonQuery;
                default:
                    throw new IllegalArgumentException("Unknown subgroup number: " + subgroupNumber);
            }
        }

        public static String filterByGroupAndWeek(String groupNumber, int weekNumber) {
            return filterByGroup(groupNumber) + " and " + filterByWeek(weekNumber);
        }

        public static String filterByGroupSubgroupAndWeek(String groupNumber, int subgroupNumber, int weekNumber) {
            return filterByGroupAndWeek(groupNumber, weekNumber) + " and (" + filterBySubgroup(subgroupNumber) + ")";
        }

    }

}
