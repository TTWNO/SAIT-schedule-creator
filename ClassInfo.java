import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;

class ClassInfo {
    public String classDescription;
    public String classCode;
    // this is usually something like A, AA, B, H, etc...
    // this distinguishes the same class being done at differnet times
    public String classGroup;
    public String teacher;
    // Mandatory / Campus Education
    public String level;
    public String CRN;
    public String term;
	public Date startDate;
	public Date startTimeDate;
	public Date endTimeDate;
	public Date endDate;
    public ArrayList<String> mainTeachers;
    public String status;
    public String campus;
    public String gradeMode;
    public double creditsDue;

    public ArrayList<DayOfWeek> classDays;
    public ArrayList<String> meetingTimes;
	public ArrayList<Date> startTimes;
	public ArrayList<Date> endTimes;
    public ArrayList<String> startDates;
    public ArrayList<String> endDates;
    public ArrayList<String> dateRanges;
    public ArrayList<String> classTypes;
    public ArrayList<String> teachersRaw;
    public ArrayList<String> meetingRooms;
    public ArrayList<String> meetingBuildings;
    public ArrayList<ArrayList<String>> teachers;
    public ArrayList<String> types;

    public ClassInfo(){
        classDescription = new String();
        classCode = new String();
        classGroup = new String();
        teacher = new String();
        creditsDue = 0.0;
        level = new String();
        CRN = new String();
        term = new String();
        status = new String();
        mainTeachers = new ArrayList<String>();
        campus = new String();
        gradeMode = new String();

		startDate = new Date();
		endDate = new Date();
		startTimeDate = new Date();
		endTimeDate = new Date();

        classDays = new ArrayList<DayOfWeek>();
        meetingTimes = new ArrayList<String>();
        startDates = new ArrayList<String>();
        endDates = new ArrayList<String>();
        dateRanges = new ArrayList<String>();
        classTypes = new ArrayList<String>();
        teachersRaw = new ArrayList<String>();
        meetingRooms = new ArrayList<String>();
        meetingBuildings = new ArrayList<String>();
        teachers = new ArrayList<ArrayList<String>>();
        types = new ArrayList<String>();
		startTimes = new ArrayList<Date>();
		endTimes = new ArrayList<Date>();
    }
    
	/**
	  * Must run this AFTER running setTimes()
	  */
    public void setStartAndEndDates(ArrayList<String> dateRangesInput){
        for (String dateRange : dateRangesInput){
            dateRanges.add(dateRange);
            String[] dateParts = dateRange.split(" - ");
            startDates.add(dateParts[0]);
            endDates.add(dateParts[1]);
        }
		

		// setting startDate, endDate, startTimeDate, and endTimeDate
		try {
			startDate = new SimpleDateFormat("dd-MMM-yyyy").parse(startDates.get(0));
			endDate = new SimpleDateFormat("dd-MMM-yyyy").parse(endDates.get(endDates.size()-1));
		} catch (Exception e){
			System.out.println(e);
		}
    }

	public void setStartAndEndTimes(){
		for (int i = 0; i < startDates.size(); i++){
			try {
				Date startTimeForThisClass = new SimpleDateFormat("dd-MMM-yyyy hh:mm aa").parse(startDates.get(i) + " " + meetingTimes.get(i).split(" - ")[0]);
				// use start date as we only want to start ime and end time on thefirst day of class
				Date endTimeForThisClass = new SimpleDateFormat("dd-MMM-yyyy hh:mm aa").parse(startDates.get(i) + " " + meetingTimes.get(i).split(" - ")[1]);
				startTimes.add(startTimeForThisClass);
				endTimes.add(endTimeForThisClass);
			} catch (Exception e){
				System.err.println("Error setting start and end times. Setting no time, just a day.");
				System.err.println(e);
				try {
					Date startAndEnd = new SimpleDateFormat("dd-MMM-yyy").parse(startDates.get(i)); 
					startTimes.add(startAndEnd);
					endTimes.add(startAndEnd);
					System.out.println("That worked, don't worry :)");
				} catch (Exception e2){
					System.err.println("ERROR!!! File will be invalid.");
				}
			}
		}
	}

    public void setMeetingLocations(ArrayList<String> meetingLocations){
        for (String fullLocation : meetingLocations){
            // find last occurance of " "
            int splitIndex = fullLocation.lastIndexOf(" ");
            // the string right of the space is the room
            // the +1 makes sure we don't include the space itself in the room
            String room = fullLocation.substring(splitIndex+1);
            // the string to the left of the space is the building
            String building = fullLocation.substring(0, splitIndex);

            // set values in class
            meetingRooms.add(room);
            meetingBuildings.add(building);
        }
    }

    public void setMainInstructors(ArrayList<String> mainInstructors){
        for (String teacher : mainInstructors.get(0).split(", ")){
            mainTeachers.add(teacher);
        }
    }

    public void setCredits(ArrayList<String> creditString){
        creditsDue = Double.parseDouble(creditString.get(0));
    }

	/**
	  * this function must be run BEFORE you run setStartAndEndDates()
	  */
    public void setTimes(ArrayList<String> times){
        meetingTimes = times;
		// this is to check that the times ahve already has some input.
		if (startDates.isEmpty() || endDates.isEmpty()){
			System.err.println("Must run setDates() first. Continuing on without setting startTimes and endTimes.");
		} else {
			setStartAndEndTimes();
		}
    }

    public void setDescriptionAndNameAndGroup(String descNameGrp){
        String[] descriptionNameGroup = descNameGrp.split(" - ");
//        for (String s : descriptionNameGroup){
//            System.out.println(s);
//        }
//        System.out.println("========");
        classDescription = descriptionNameGroup[0];
        classCode = descriptionNameGroup[1];
        classGroup = descriptionNameGroup[2];
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
                // if teacher name isn't empty (it is in some cases for some online courses)
                // TODO: fix, this doesn't work right now for some reason
                if (!teachers.get(i).equals("")){
                     teachers.get(i).add(singleTeacher);
                }
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
                // TODO: may be inaccurate
                case "U":
                    classDays.add(DayOfWeek.SATURDAY);
                    break;
            }
        }
    }

    // TODO: implement
    public String toString(){
        String topInfoFormat = "\t\"%s\": \"%s\",%n";
        String result = String.format("{%n");
        String listWithCommaFormat = "\"%s\", ";
        String listWithoutCommaFormat = "\"%s\"";
        result += String.format(topInfoFormat, "Name", classDescription);
        result += String.format(topInfoFormat, "Class ID", classCode);
        result += String.format(topInfoFormat, "Class Group", classGroup);
        result += String.format(topInfoFormat, "Term", term);
        result += String.format(topInfoFormat, "CRN", CRN);
        result += String.format(topInfoFormat, "Status", status);
        result += String.format("\t\"%s\": [", "Assigned Instructors");
        for (int i = 0; i < mainTeachers.size(); i++){
            // if it is the last item to be printed
            if (i == mainTeachers.size()-1){
                // print without the comma at the end
                result += String.format(listWithoutCommaFormat, mainTeachers.get(i));
            } else {
                result += String.format(listWithCommaFormat, mainTeachers.get(i));
            }
        }
        result += String.format("]%n");
        result += String.format(topInfoFormat, "Grade Mode", gradeMode);
        result += String.format("\t\"Credits\": \"%.3f\"%n", creditsDue);
        result += String.format(topInfoFormat, "Level", level);
        result += String.format(topInfoFormat, "Campus", campus);
        result += String.format("\t\"Meeting Times\": [");
        for (int i = 0; i < classDays.size(); i++){
            result += String.format("%n\t\t{ %n");
            result += String.format("\t\t\t\"%s\": \"%s\"%n", "Class Type", classTypes.get(i));
            result += String.format("\t\t\t\"%s\": \"%s\"%n", "Time", meetingTimes.get(i));
            result += String.format("\t\t\t\"%s\": \"%s\"%n", "Day", classDays.get(i));
            result += String.format("\t\t\t\"%s\": \"%s\"%n", "Building", meetingBuildings.get(i));
            result += String.format("\t\t\t\"%s\": \"%s\"%n", "Room", meetingRooms.get(i));
            result += String.format("\t\t\t\"%s\": \"%s\"%n", "Start Date", startDates.get(i));
            result += String.format("\t\t\t\"%s\": \"%s\"%n", "End Date", endDates.get(i));
			//result += String.format("\t\t\t\"%s\": \"%s\"%n", "", startTime);
            result += String.format("\t\t\t\"%s\": \"%s\"%n", "Type", types.get(i));
            result += String.format("\t\t\t\"%s\": [", "Teachers");
            for (int j = 0; j < teachers.get(i).size(); j++){
                // if last item
                if (j == teachers.get(i).size()-1){
                    result += String.format(listWithoutCommaFormat, teachers.get(i).get(j));
                } else {
                    result += String.format(listWithCommaFormat, teachers.get(i).get(j));
                }
            } 
            result += String.format("]%n");
            // if last meeting time
            if (i == classDays.size()-1){
                // print closing curly brace without comma
                // and add tab to clean up look when printing the square bracket below
                result += String.format("\t\t}%n\t"); 
            } else {
                result += String.format("\t\t},%n");
            }
        }
        result += String.format("]%n");
        result += String.format("}");
        return result;
    }


	public ArrayList<String> toIcsFiles(){
		ArrayList<String> resultArray = new ArrayList<>();
		
		String special_newline = "\r\n";
		SimpleDateFormat dateOutput = new SimpleDateFormat("yyyyMMdd'T'kkmmss");
		for (int i = 0; i < startTimes.size(); i++){
			String result = "";
			// headers
			result += String.format("BEGIN:VCALENDAR%s", special_newline);
			result += String.format("VERSION:2.0%s", special_newline);
			result += String.format("PRODID:-//hacksw/handcal//NONSGML v1.0//EN%s", special_newline);
			result += String.format("BEGIN:VEVENT%s", special_newline);
			// body of event
			result += String.format("UID:uid@example.com%s", special_newline);
			result += String.format("DTSTAMP:%s%s", dateOutput.format(startTimes.get(i)), special_newline);
			result += String.format("DTSTART:%s%s", dateOutput.format(startTimes.get(i)), special_newline);
			result += String.format("DTEND:%s%s", dateOutput.format(endTimes.get(i)), special_newline);
			result += String.format("SUMMARY:%s%s", classDescription + " @ " + meetingBuildings.get(i) + " " + meetingRooms.get(i), special_newline);
			result += String.format("RRULE:FREQ=WEEKLY;UNTIL=%s%s", dateOutput.format(endDate), special_newline);
			// end of body
			result += String.format("END:VEVENT%s", special_newline);
			result += String.format("END:VCALENDAR%s", special_newline);
			resultArray.add(result);
		}
		return resultArray;
	}
}
