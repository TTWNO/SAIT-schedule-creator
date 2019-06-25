import java.util.Map;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import javax.swing.JOptionPane;
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

    public static PrintWriter logFile;

    public static void main(String[] args) throws IOException {
        // This is the pre-program warning.
        int decision = JOptionPane.showConfirmDialog(null,
                     "THIS IS NOT AUTHORIZED BY SAIT. I HAVE NO RESPONSIBLITY FOR ANY CONSQUENSES. Do you wish to continue?", "WARNING!",
                     JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        // if the user pressed no
        if (decision == 1){
            System.exit(0);
        }
        // if user emtered "No, thanks.
        String file_content = "";
        logFile = new PrintWriter("log.txt");
        // store all class info in an arraylist of info
        ArrayList<ClassInfo> classesInfo = new ArrayList<>();
        // have a temp variable to store the info being worked on in
        ClassInfo classInfoTemp = new ClassInfo();

        String student_id;
        String birthdate;

        //System.out.print("Enter student ID: ");
        //student_id = input.nextLine();
        student_id = JOptionPane.showInputDialog("Please enter your SAIT student ID: ");
        //System.out.print("Enter password: ");
        //birthdate = input.nextLine();
        birthdate = JOptionPane.showInputDialog("Please enter your birthdate (or mySAIT password if it differs): ");
        // get UUID from main login page
        String uuid = getLoginUID();
        // get login cookies by using student ID, passowrd, and uuid
        Map<String, String> cookies = getLoginCookies(uuid, student_id, birthdate);
        // get new cookies, and all possible terms in the form of <Option> Elements
        Map.Entry<Map<String, String>, Elements> cookies_and_term_options =  getTermPossibilities(cookies);
        // update cookies
        cookies.putAll(cookies_and_term_options.getKey());
        // the Elements are options for terms
        Elements termOptions = cookies_and_term_options.getValue();

        // for each one in reverse order
        ArrayList<String> optionStrings = new ArrayList<>();
        for (int i = termOptions.size()-1; i >= 0; i--){
            //logFile.printf("%d) %s%n", i+1, termOptions.get(i).text());
            optionStrings.add(termOptions.get(i).text());
        }
        //logFile.printf("Pick a term (%d-%d): ", 1, termOptions.size());
        //int termId = keyboardInput.nextInt();
        Object termObj = JOptionPane.showInputDialog(null,
                "Please choose your term:", "Input",
                JOptionPane.INFORMATION_MESSAGE, null,
                optionStrings.toArray(), optionStrings.get(optionStrings.size()-1));
        logFile.println(termObj);
        int termId = 0;
        for (int i = 0; i < termOptions.size(); i++){
            if (termObj.equals(termOptions.get(i).text())){
                termId = i;
                break;
            }
        }

        // this will be something like 201920 (Fall 2019)
        String termIdToPost = termOptions.get(termId).val();
        // finally, get the schedule of the student, with the cookies, and the termId
        Document d4 = get_student_schedule(cookies, termIdToPost);
        
        file_content = d4.toString();
        // make a Jsoup Document object that has parsed the String as an HTML file
        Document doc = Jsoup.parse(file_content);
        classesInfo = getClassInfoFromDocument(doc);
        // get the title of the document (probably: Student full schedule or something simmilar)
        //String title = doc.title();
        //logFile.println(title);

        // for each class 
        for (ClassInfo classInfo : classesInfo){
			int file_index = 1;
			for (String calFile : classInfo.toIcsFiles()){
				PrintWriter pw = new PrintWriter(classInfo.classCode + "_" + file_index + ".ics");
				//logFile.println(calFile);
            	pw.print(calFile);
				pw.close();
				file_index++;
			}
        }
        JOptionPane.showMessageDialog(null, "Ran sucsessfully!", "alert", JOptionPane.INFORMATION_MESSAGE);
    } // end main method

    /** Document get_student_schedule(Map<String, String> cookies, String term_id)
     * this function returns the schedule of the student.
     * If all other functions have run succsessfully, this should work too.
     */
    public static Document get_student_schedule(Map<String, String> cookies, String term_id){
            try {
                Response r4 = Jsoup.connect("https://bss.mysait.ca/prod/bwskfshd.P_CrseSchdDetl")
                                   .userAgent("Mozilla/5.0")
                                   .followRedirects(true)
                                   .timeout(10*1000)
                                   .cookies(cookies)
                                   .method(Method.POST)
                                   // WATCH OUT! It IS "term_in", NOT "term_id"
                                   .data("term_in", term_id)
                                   .execute();
                Document d4 = r4.parse();
                return d4;
            }catch (Exception e){
                logFile.println("There was an error getting the schedule.");
                logFile.println("Exiting.");
                logFile.println(e);
                JOptionPane.showMessageDialog(null, "Error getting student schedule", "alert", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
                Document compiler_pleaser = new Document("");
                return compiler_pleaser;
            }
    }

    /** Map.Entry<Map<String, String>, Elements> getTermPossibilities(Map<String, String> cookies)
     * this function grabs the options for the terms (e.g. Fall 2019, Winter 2019, Appretice 2020, etc..)
     * It also returns cookies that can be used on the next page in the firs entry on the map
     */
    public static Map.Entry<Map<String, String>, Elements> getTermPossibilities(Map<String, String> cookies){
        try{
			Response r3 = Jsoup.connect("https://www.mysait.ca/cp/ip/login?sys=sctssb&url=https%3A%2F%2Fbss.mysait.ca%2Fprod%2Fbwskfshd.P_CrseSchdDetl")
                               .userAgent("Mozilla/5.0")
                               .followRedirects(true)
							   .timeout(10*10000)
							   .cookies(cookies)
							   .execute();
			Document d3 = r3.parse();
            //logFile.println(d3);
            Elements termOptions = d3.getElementById("term_id").getElementsByTag("option");
            AbstractMap.Entry<Map<String, String>, Elements> result = new AbstractMap.SimpleEntry<Map<String, String>, Elements>(r3.cookies(), termOptions);
            return result;
        }catch(Exception e){
            logFile.println("Error getting term options. Shutting down");
            logFile.println(e);
            logFile.close();
            JOptionPane.showMessageDialog(null, "Error getting term options.", "alert", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            // this is all to keep the compiler from yellow at me. :(
            // Compiler is mean, please help me get out of this abusive relationship.
            Map<String, String> re_cookies = new HashMap<String, String>();
            Elements re_elemts = new Elements();
            AbstractMap.Entry<Map<String, String>, Elements> result = new AbstractMap.SimpleEntry<Map<String, String>, Elements>(re_cookies, re_elemts);
            return result;
        }
    }

    /** Map<String, String> getLoginCookies(String uuid, String student_id, String birthdate)
     * This function takes the uuid (unique for each login), the student's username (000XXXXXX), and their birthday (or whatever their SAIT password is
     */
    public static Map<String, String> getLoginCookies(String uuid, String student_id, String birthdate){
        try{
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
            return cookies;
        } catch (Exception e){
            logFile.println("Error getting cookies. Shutting down");
            logFile.println(e);
            logFile.close();
            JOptionPane.showMessageDialog(null, "Error logging in.", "alert", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            Map<String, String> fake_result = new HashMap<String, String>();
            return fake_result;
        }
    }

    /** String getLoginUID()
     * This function returns the UUID of the login, as a unique token.
     */
    public static String getLoginUID(){
        try{
            Response uuid_page = Jsoup.connect("https://mysait.ca/cp/home/displaylogin")
                                      .userAgent("Mozilla/5.0")
                                      .execute();
            Document uuid_doc = uuid_page.parse();
            String uuid = uuid_doc.getElementById("pass").nextElementSibling().nextElementSibling().val();
            return uuid;
        } catch (Exception e){
            logFile.println("There was an error getting the login id.");
            logFile.println(e);
            JOptionPane.showMessageDialog(null, "Error getting login UID.", "alert", JOptionPane.ERROR_MESSAGE);
            logFile.close();
            System.exit(1);
            return ""; // to stop compiler form complaining
        }
    }

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
                //logFile.println("th: " + header.text());
                // add header text to headers list
                headers.add(header.text());
            }

            // make temp array to store the data associated with the headers ( the values_
            ArrayList<String> tempDataPoints = new ArrayList<>();
            // find every <td> (table data) tag in the row
            Elements elementDataPoints = row.getElementsByTag("td");
            // for all <td> tags
            for (Element elementDataPoint : elementDataPoints){
                //logFile.println("td: " + elementDataPoint.text());
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
            //logFile.println("Header: " + headers.get(i));
            // if this has multiple data points associated with the header
            if (multi){
                // for each datapoint that matches the header
                for (int j = 0; j < dataPoints.get(i).size(); j++){
                    //logFile.println("Data: " + dataPoints.get(i));
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
            logFile.printf("%s: ", me.getKey());
            // for each value
            for (String val : me.getValue()){
                // print the value
                logFile.printf("%s, ", val);
            }
            logFile.printf("%n");
        }
        logFile.println("---------------------------------");
    }
}
