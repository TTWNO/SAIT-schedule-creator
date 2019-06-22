import java.util.ArrayList;
import java.time.DayOfWeek;

class ClassInfo {
    public String classCode;
    public String teacher;
    public double creditsDue;

    public ArrayList<DayOfWeek> classDays;
    public ArrayList<String> meetingTimes;

    public ClassInfo(){
        classCode = new String();
        teacher = new String();
        creditsDue = 0.0;

        classDays = new ArrayList<DayOfWeek>();
        meetingTimes = new ArrayList<String>();
    }
}
