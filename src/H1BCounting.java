import java.util.Scanner;
import java.io.*;
import java.util.HashMap;
import java.util.PriorityQueue;

public class H1BCounting {
    
    public static void main(String[] args) throws Exception {
        
        String path = "./";
        String output_path = path + "output/";
        
        //read input
        Scanner sc = new Scanner(System.in);
        String headings = sc.nextLine(); //read the first line containing the column names
        String[] cols = headings.split(";"); //split the entries using ";" delimiter
         
        //find the column number of the state, certification, and job title
        int state = 0; int cert = 0; int job = 0;
        
        //Assumption: the columns of interest contain
        //key words "work" and "state"; "status"; "soc" and "name
        
        boolean found = false; //is primary work location found
        
        for (int i = 0; i < cols.length; i++) {
            String col = cols[i].toLowerCase();
            if (!found && col.contains("work") && col.contains("state")) {
                state = i;
                found = true;
            }
            else if (col.contains("status")) cert = i;
            else if (col.contains("soc") && col.contains("name")) job = i;
        }
        
        int totCertified = 0; //keeps track of the total number of certified h-1b visas
        
        //certJobMap: key - job title, value - humber of certified positions with that job title
        HashMap<String, Integer> certJobMap = new HashMap<String, Integer>();
        
        //certStateMap: key - state, value - number of certified applications associated with that state
        HashMap<String, Integer> certStateMap = new HashMap<String, Integer>();
        
        
        //continue reading the input and fill in the information into the hashmaps
        while (sc.hasNextLine()) {
            String data_row = sc.nextLine(); //read line
            String[] data = data_row.split(";"); //split line using ";" delimiter 
            
            //analyze only data with the desired visa type and only certified applications
            if (data[cert].equalsIgnoreCase("certified")) {  
                //if the application is certified, update the hashmaps and
                //remove any non-alphanumeric characters (except "," and space)
                //from the occupation name and state name
                updateMap(certJobMap, data[job].replaceAll("[^A-Za-z0-9 ,]", ""));
                updateMap(certStateMap, data[state].replaceAll("[^A-Za-z0-9 ,]", ""));
                totCertified++;
            }
        }
        
        //obtain top occupations and states using a priority queue
        int k = 10; 
        H1bObject[] topJob = filterTop(certJobMap, k, totCertified);
        H1bObject[] topState = filterTop(certStateMap, k, totCertified);
        
        
        //write results to a text file
        //output top occupations with certified h1b visas
        FileWriter fw = new FileWriter(output_path + "top_10_occupations.txt");
        BufferedWriter writer = new BufferedWriter(fw);
        writer.write("TOP_OCCUPATIONS;NUMBER_CERTIFIED_APPLICATIONS;PERCENTAGE");
        writer.newLine();
        for (int i = 0; i < topJob.length; i++) {
            writer.write(topJob[i].toString());
            writer.newLine();
        }
        writer.close();
        
        //output top states with certified h1b 
        fw = new FileWriter(output_path + "top_10_states.txt");
        writer = new BufferedWriter(fw);
        writer.write("TOP_STATES;NUMBER_CERTIFIED_APPLICATIONS;PERCENTAGE");
        writer.newLine();
        for (int i = 0; i < topState.length; i++) {
            writer.write(topState[i].toString());
            writer.newLine();
        }
        writer.close();
        
    }
    
    
    //helper function to update hashmap entries
    private static void updateMap(HashMap<String, Integer> map, String key) {
        //if the map contains the given key, increase the value by 1
        if (map.containsKey(key)) {
            map.put(key, map.get(key)+1);
        }
        
        //if the map does not contain the given key,
        //add the key to the map with corresponding value of 1
        else map.put(key, 1);
    }
    
    //helper function to filter out the top k elements with the highest frequency
    private static H1bObject[] filterTop(HashMap<String, Integer> map, int k, int tot_Certified) {
        //create a priority queue 
        PriorityQueue<H1bObject> pq = new PriorityQueue<H1bObject>();
        
        //traverse the hashmap and add data to the priority queue
        //if the priority queue size exceeds k, remove the minimum element
        for (String s : map.keySet()) {
            int value = map.get(s);
            double percent = value*1.0/tot_Certified*100;
            percent = (double) Math.round(percent*10)/10;
            H1bObject obj = new H1bObject(s, value, percent);
            pq.offer(obj);
            if (pq.size() > k) pq.poll();
        }
        
        //transfer the elements from the priority queue to an array in reverse order
        int size = pq.size() < k ? pq.size() : k;
        H1bObject[] a = new H1bObject[size];
        
        int ind = size-1;
        while (!pq.isEmpty()) {
            a[ind] = pq.poll();
            ind--;
        }
        return a;
    }
    
}

//H1bObject data type: helper class to process and sort output in the desired format
class H1bObject implements Comparable<H1bObject>{
        
    private String name; //string representing either the occupation or state
    private int freq; //the number of applications associated with this name
    private double percent; //the percentage of applications out of all certified applications
    
    public H1bObject(String name, int freq, double percent) {
        this.name = name;
        this.freq = freq;
        this.percent = percent;
    }
    
    //get name
    public String getName() { 
        return this.name;
    }
    
    //get number of applications 
    public int getFreq() {
        return this.freq;
    }
    
    //get percentage
    public double getPercent() {
        return this.percent;
    }
    
    //convert to a string
    public String toString() {
        return name + ";" + freq + ";" + percent + "%";
    }
    
    //compare method: compare by freq and 
    //by alphabetical order of the name if freq attributes are the same
    
    public int compareTo(H1bObject that) {
        if (this.freq < that.freq) return -1;
        else if (this.freq > that.freq) return 1;
        else if (this.freq == that.freq) {
            return -this.name.compareTo(that.name);
        }
        return 0;
    }
}


