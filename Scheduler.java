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

        String file_content = new String(Files.readAllBytes(Paths.get("./mySAIT_files/bwskfshd.html")));
        Document doc = Jsoup.parse(file_content);
        int classIndex = 0;

        Elements tables = doc.getElementsByClass("datadisplaytable");
        String title = doc.title();

        System.out.println(title);

        for (Element el : tables){
            String classDescription = el.getElementsByClass("captiontext").first().text();
            System.out.println(classDescription);
            HashMap<String, ArrayList<String>> tableData = getTableData(el);
            printTableData(tableData);
            for (Map.Entry<String, ArrayList<String>> entry : tableData.entrySet()){
                ClassInfo classInfo = new ClassInfo();
            } // end for
        } // end for
    } // end main

    /**
     * getTableInfo() takes an Element that it assumes is an HTML table, and returns the data in the following format
     *
     * @param table A table that is an HTML table <tr> <table>, etc...
     *
     * @return An Array, with headers as the indexing String, and an ArryaList of data points for that header
     *
     */
    public static HashMap<String, ArrayList<String>> getTableData(Element table){
        HashMap<String, ArrayList<String>> result = new HashMap<>();
        ArrayList<String> headers = new ArrayList<>();
        ArrayList<ArrayList<String>> dataPoints = new ArrayList<>();

        Elements rows = table.getElementsByTag("tr");
        for (Element row : rows){
            Elements tempHeaders = row.getElementsByTag("th");

            // for each ddheader in the table
            for (Element header : tempHeaders){
                headers.add(header.text());
            }

            ArrayList<String> tempDataPoints = new ArrayList<>();
            Elements elementDataPoints = row.getElementsByTag("td");
            // for all dddefault items in the table
            for (Element elementDataPoint : elementDataPoints){
                tempDataPoints.add(elementDataPoint.text());
            }
            dataPoints.add(tempDataPoints);
        }

        // assuming properly formed table :)
        // TODO: instrual proof this
        // for each header
        for (int i = 0; i < headers.size(); i++){
            ArrayList<String> iDataPoints = new ArrayList<>();
            System.out.println(headers.get(i));
            for (int j = 0; j < dataPoints.size(); j++){
                System.out.println(dataPoints.get(j));
                iDataPoints.add(dataPoints.get(j).get(i));
            } 
            result.put(headers.get(i), iDataPoints);
        }
        return result;
    }

    public static void printTableData(HashMap<String, ArrayList<String>> table){
        for (Map.Entry<String, ArrayList<String>> me : table.entrySet()){
            System.out.printf("%s: ", me.getKey());
            for (String val : me.getValue()){
                System.out.printf("%s, ", val);
            }
            System.out.printf("%n");
        }
        System.out.println("---------------------------------");
    }
}
