import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;
import java.time.DayOfWeek;

class ClassInfo {
    public String classDescription;
    public String classCode;
    public String teacher;
    public double creditsDue;

    public ArrayList<DayOfWeek> classDays;
    public ArrayList<String> meetingTimes;
    public ArrayList<String> dateRanges;
    public ArrayList<String> classTypes;
    public ArrayList<String> teachersRaw;
    public ArrayList<ArrayList<String>> teachers;

    public ClassInfo(){
        classDescription = new String();
        classCode = new String();
        teacher = new String();
        creditsDue = 0.0;

        classDays = new ArrayList<DayOfWeek>();
        meetingTimes = new ArrayList<String>();
        dateRanges = new ArrayList<String>();
        classTypes = new ArrayList<String>();
        teachersRaw = new ArrayList<String>();
        teachers = new ArrayList<ArrayList<String>>();
    }

    public void setTeachers(ArrayList<String> teachersPerClasses){
        // for each teacher list / teachers per each schedule time
        for (int i = 0; i < teachersPerClasses.size(); i++){
            String teachersForClass = teachersPerClasses.get(i);
            // for each teacher in the list
            for (String singleTeacher : teachersForClass.split(", ")){
                // if the master teachers list does not have this index
                if (teachers.size() <= i){
                    // initialize it as empty
                    teachers.add(i, new ArrayList<String>());
                } 
                // add the teacher
                teachers.get(i).add(singleTeacher);
            }
            // add into the teachersRaw list (which only stores the list of teachers per class
            // ... not every teacher for each class
            teachersRaw.add(teachersForClass);
        }
    }

    public void setDays(ArrayList<String> daysByScheduleTime){
        for (String day : daysByScheduleTime){
            switch(day){
                case "M":
                    classDays.add(DayOfWeek.MONDAY);
                    break;
                case "T":
                    classDays.add(DayOfWeek.TUESDAY);
                    break;
                case "W":
                    classDays.add(DayOfWeek.WEDNESDAY);
                    break;
                case "R":
                    classDays.add(DayOfWeek.THURSDAY);
                    break;
                case "F":
                    classDays.add(DayOfWeek.FRIDAY);
                    break;
                case "S":
                    classDays.add(DayOfWeek.SATURDAY);
                    break;
                case "U":
                    classDays.add(DayOfWeek.SATURDAY);
                    break;
            }
        }
    }

    // TODO: implement
    public String toString(){
        return ""; 
    }
}
