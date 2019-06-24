import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.Connection.Method;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

class Scheduler {
    public static void main(String[] args) throws IOException {
        // store all class info in an arraylist of info
        ArrayList<ClassInfo> classesInfo = new ArrayList<>();
        // have a temp variable to store the info being worked on in
        ClassInfo classInfoTemp = new ClassInfo();

        /*
         /* This code can log you into your mysait page.
         /* Unfotunately, it cannot execute the javascript that the server returns.
         /* This functionality would require a seperate library... I don't want to do that yet.
         /* Plus mySAIT is revamping right away, so there's no point.
        
        
        String student_id;
        String birthdate;

        Scanner input = new Scanner(System.in);
        System.out.print("Enter student ID: ");
        student_id = input.nextLine();
        System.out.print("Enter password: ");
        birthdate = input.nextLine();
        try {
            Response uuid_page = Jsoup.connect("https://mysait.ca/cp/home/displaylogin")
                                      .userAgent("Mozilla/5.0")
                                      .execute();

            Document uuid_doc = uuid_page.parse();
            String uuid = uuid_doc.getElementById("pass").nextElementSibling().nextElementSibling().val();

            Response response = Jsoup.connect("https://mysait.ca/cp/home/login")
                                     .userAgent("Mozilla/5.0")
                                     .timeout(10*1000)
                                     .method(Method.POST)
                                     .data("user", student_id)
                                     .data("pass", birthdate)
                                     .data("uuid", uuid)
                                     .followRedirects(true)
                                     .execute();
            Map<String, String> cookies = response.cookies();
            Document doc1 = response.parse();
            //System.out.println(doc1);

            String main_page_url = "https://www.mysait.ca/cp/render.UserLayoutRootNode.uP?uP_tparam=utf&utf=https%3A%2F%2Fwww.mysait.ca%2Fcp%2Fip%2Flogin%3Fsys%3Dsctssb%26url%3Dhttps%3A%2F%2Fbss.mysait.ca%2Fprod%2Ftwbkwbis.P_GenMenu%3Fname%3Dbmenu.P_RegMnu"; 
            Response main_page = Jsoup.connect(main_page_url)
                                     .userAgent("Mozzila/5.0")
                                     .timeout(10*10000)
                                     .cookies(cookies)
                                     .followRedirects(true)
                                     .execute();
            Document main_page_doc = main_page.parse();
            System.out.println(main_page_doc);

        } catch (IOException e){
            System.out.println("Exception: " + e);
        }*/

        // this reads an entire file into one String in one line. Java 7+ AFAIK
        String file_content = new String(Files.readAllBytes(Paths.get("bwskfshd.html")));
        // make a Jsoup Document object that has parsed the String as an HTML file
        Document doc = Jsoup.parse(file_content);
        classesInfo = getClassInfoFromDocument(doc);
        // get the title of the document (probably: Student full schedule or something simmilar)
        String title = doc.title();
        System.out.println(title);

        // for each class 
        for (ClassInfo classInfo : classesInfo){
			int file_index = 1;
			for (String calFile : classInfo.toIcsFiles()){
				PrintWriter pw = new PrintWriter(classInfo.classCode + "_" + file_index + ".ics");
				System.out.println(calFile);
            	pw.print(calFile);
				pw.close();
				file_index++;
			}
        }
    } // end main method

//
    /** getClassInfoFromDocument(Document mySAITPage)
     * This function takes a jsoup Document input, and extracts all the necessary data from it to form a list of ClassInfo objects.
     *
     * @param mySAITPage a jsoup Document of the bwskfshd.html SAIT file
     *
     * @return A list of ClassInfo objects
     */
    public static ArrayList<ClassInfo> getClassInfoFromDocument(Document mySAITDocument){
        ArrayList<ClassInfo> result = new ArrayList<>();
        // simple temp class to store data between tables
        ClassInfo classInfoTemp = new ClassInfo();
        // to keep track if I am on an even table (course info), or an odd table (schedule info)
        int tableIndex = 0;
        
        // get all elements that have class="datadisplaytable" in their HTML tags
        Elements tables = mySAITDocument.getElementsByClass("datadisplaytable");

        // for each table (of class datadisplaytable) in the schedule HTML
        for (Element el : tables){
            
            // if it is an even table
            if (tableIndex % 2 == 0){
                // set the temp ClassInfo to a new one
                classInfoTemp = new ClassInfo();
                // set caption text that contains course info
                String classDescriptionNameAndGroup = el.getElementsByClass("captiontext").first().text();
                classInfoTemp.setDescriptionAndNameAndGroup(classDescriptionNameAndGroup);
            }
            // get the description by getting the text of first element that has class="captiontext" in its tags in this table
            // pass the entire table too getTableData, which returns a Dictionary:
            // { "HeaderName": ["value under header 1", "value under header 2", ...] }
            HashMap<String, ArrayList<String>> tableData = getTableData(el, tableIndex%2==0);
            // speicalized function to print the table data
            //printTableData(tableData);
            // increment the table index
            tableIndex++;
            
            // for each entry in the table
            for (Map.Entry<String, ArrayList<String>> entry : tableData.entrySet()){
                // check the header
                switch(entry.getKey()){
                    case "Date Range":
                        classInfoTemp.setStartAndEndDates(entry.getValue());
                        break;
                    case "Type":
                        classInfoTemp.types = entry.getValue();
                        break;
                    case "Instructors":
                        classInfoTemp.setTeachers(entry.getValue());
                        break;
                    case "Days":
                        classInfoTemp.setDays(entry.getValue());
                        break;
                    case "Time":
                        classInfoTemp.setTimes(entry.getValue());
                        break;
                    case "Schedule Type":
                        classInfoTemp.classTypes = entry.getValue();
                        break;
                    case "Where":
                        classInfoTemp.setMeetingLocations(entry.getValue());
                        break;
                    case "CRN:":
                        classInfoTemp.CRN = entry.getValue().get(0);
                        break;
                    case "Credits:":
                        classInfoTemp.setCredits(entry.getValue());
                        break;
                    case "Associated Term:":
                        classInfoTemp.term = entry.getValue().get(0);
                        break;
                    case "Assigned Instructor:":
                        classInfoTemp.setMainInstructors(entry.getValue());
                        break;
                    case "Status:":
                        classInfoTemp.status = entry.getValue().get(0);
                        break;
                    case "Level:":
                        classInfoTemp.level = entry.getValue().get(0);
                        break;
                    case "Campus:":
                        classInfoTemp.campus = entry.getValue().get(0);
                        break;
                    case "Grade Mode:":
                        classInfoTemp.gradeMode = entry.getValue().get(0);
                        break;
                }
            }
            // if it is an odd element
            if (tableIndex%2==1){
                // add ClassInfo to list
                result.add(classInfoTemp);
            }
        } // end for Element
        return result;
    }

    /**
     * getTableInfo() takes an Element that it assumes is an HTML table, and returns the data in the following format
     *
     * @param table A table that is an HTML table <tr> <table>, etc...
     *
     * @return An Array, with headers as the indexing String, and an ArryaList of data points for that header
     *
     */
    public static HashMap<String, ArrayList<String>> getTableData(Element table, boolean multi){
        // the returned HashMap
        HashMap<String, ArrayList<String>> result = new HashMap<>();
        // the headers in a list
        ArrayList<String> headers = new ArrayList<>();
        // the data associated with the headers
        ArrayList<ArrayList<String>> dataPoints = new ArrayList<>();

        // find each <tr> (table row) in the table
        Elements rows = table.getElementsByTag("tr");
        for (Element row : rows){
            // find each <th> (table header) tag in the row
            Elements tempHeaders = row.getElementsByTag("th");

            // for each ddheader in the table
            for (Element header : tempHeaders){
                //System.out.println("th: " + header.text());
                // add header text to headers list
                headers.add(header.text());
            }

            // make temp array to store the data associated with the headers ( the values_
            ArrayList<String> tempDataPoints = new ArrayList<>();
            // find every <td> (table data) tag in the row
            Elements elementDataPoints = row.getElementsByTag("td");
            // for all <td> tags
            for (Element elementDataPoint : elementDataPoints){
                //System.out.println("td: " + elementDataPoint.text());
                // add to temporary list
                tempDataPoints.add(elementDataPoint.text());
            }
            // if we have results from this row
            if (tempDataPoints.size() != 0){
                // add the data to the main dataPoints variable
                dataPoints.add(tempDataPoints); 
            }
        } // end for every <tr>

        // *** CONSTRUCTING THE RESULT DATA ***
        // assuming properly formed table :)
        // TODO: instrual proof this
        // for each header
        for (int i = 0; i < headers.size(); i++){
            // store data points temporarily because they have to be grabbed
            // in a different order depending on if this is a
            // A) course info table (non-multi), or
            // B) if it is a course schedule table (multi)
            // multi refers to having multiple data points associated with one header
            ArrayList<String> iDataPoints = new ArrayList<>();
            //System.out.println("Header: " + headers.get(i));
            // if this has multiple data points associated with the header
            if (multi){
                // for each datapoint that matches the header
                for (int j = 0; j < dataPoints.get(i).size(); j++){
                    //System.out.println("Data: " + dataPoints.get(i));
                    // add it to the temp datapoints
                    iDataPoints.add(dataPoints.get(i).get(j));
                } 
            // otherwise
            } else {
                // for each datapoint
                for (int j = 0; j < dataPoints.size(); j++){
                    // add the datapoint associated with this header
                    iDataPoints.add(dataPoints.get(j).get(i));
                }
            }
            // put it all together!
            // associate the header with the data points
            result.put(headers.get(i), iDataPoints);
        }
        return result;
    }

    public static void printTableData(HashMap<String, ArrayList<String>> table){
        // for each value in the dictionary (HashMap)
        for (Map.Entry<String, ArrayList<String>> me : table.entrySet()){
            // print the key
            System.out.printf("%s: ", me.getKey());
            // for each value
            for (String val : me.getValue()){
                // print the value
                System.out.printf("%s, ", val);
            }
            System.out.printf("%n");
        }
        System.out.println("---------------------------------");
    }
}
