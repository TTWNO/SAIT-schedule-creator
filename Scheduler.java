import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

class Scheduler {
    public static void main(String[] args) throws IOException {
        ArrayList<ClassInfo> classesInfo = new ArrayList<>();
        // have a temp variable to store the info being worked on in
        ClassInfo tempClassInfo = new ClassInfo();

        // this reads an entire file into one String in one line. Java 7+ AFAIK
        String file_content = new String(Files.readAllBytes(Paths.get("./mySAIT_files/bwskfshd.html")));
        // make a Jsoup Document object that has parsed the String as an HTML file
        Document doc = Jsoup.parse(file_content);
        // to keep track if I am on an even table (course info), or an odd table (schedule info)
        int tableIndex = 0;

        // get all elements that have class="datadisplaytable" in their HTML tags
        Elements tables = doc.getElementsByClass("datadisplaytable");
        // get the title of the document (probably: Student full schedule or something simmilar)
        String title = doc.title();

        System.out.println(title);

        // for each table (of class datadisplaytable) in the schedule HTML
        for (Element el : tables){
            // if it is an even table
            if (tableIndex % 2 == 0){
                // set the temp ClassInfo to a new one
                tempClassInfo = new ClassInfo();
            }
            // get the description by getting the text of first element that has class="captiontext" in its tags in this table
            String classDescription = el.getElementsByClass("captiontext").first().text();
            System.out.println(classDescription);
            // pass the entire table too getTableData, which returns a Dictionary:
            // { "HeaderName": ["value under header 1", "value under header 2", ...] }
            HashMap<String, ArrayList<String>> tableData = getTableData(el, tableIndex%2==0);
            // speicalized function to print the table data
            printTableData(tableData);
            // increment the table index
            tableIndex++;
            
            // for each entry in the table
            for (Map.Entry<String, ArrayList<String>> entry : tableData.entrySet()){
                // check the header
                switch(entry.getKey()){
                    case "Date Range":
                        tempClassInfo.dateRanges = entry.getValue();
                        break;
                    case "Type":
                        tempClassInfo.classTypes = entry.getValue();
                        break;
                    case "Instructors":
                        tempClassInfo.setTeachers(entry.getValue());
                    case "Days":
                        tempClassInfo.setDays(entry.getValue());
                        break;
                }
            }
            // if it is an odd element
            if (tableIndex%2==1){
                // add ClassInfo to list
                classesInfo.add(tempClassInfo);
            }
        } // end for Element

        // for each class 
        for (ClassInfo classInfo : classesInfo){
            System.out.println("Range: " + classInfo.dateRanges);
            System.out.println("Days: " + classInfo.classDays);
            System.out.println("Teachers: " + classInfo.teachers);
            System.out.println("Teachers [RAW]: " + classInfo.teachersRaw);
        }
    } // end main method

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
