import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        String fileContents = readFile("DobervichPlanningSession1.vtt");
        String[] lines = fileContents.split("\n");
        for (int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].trim();
        }
        summaryFile(lines);
        condensedTranscript(lines);
    }
    private static void condensedTranscript(String[] lines) {
        try {
            PrintWriter out = new PrintWriter( new FileWriter("condensed.txt"));
            for (int i = 4; i < lines.length-4; i+=4) {
                String name = extractName(lines[i]);
                double secPerLine = calcMin(lines[i-1]) * 60;
                    out.println(name + " " + String.format("%.2f", secPerLine) + " sec\n");
            }
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void summaryFile(String[] lines) {
        try {
            PrintWriter out = new PrintWriter( new FileWriter("summary.txt"));
            out.println("Total # of people: " + numOfPeople(lines));
            out.println("Total length of session: " + totalMeetingTime(lines));
            out.println("Total # of speaker switches: " + numOfSwitches(lines) + "\n");
            out.println("Total Talk Time: ");
            String[] arr = namesList(lines).toArray(new String[namesList(lines).size()]);
            for (int i = 1; i < arr.length; i++) {
                out.println(arr[i] + " " + peopleTalkTime(lines, arr[i]));
            }
            out.println("\n");
            out.println("Average speaking time: ");
            for (int i = 1; i < arr.length; i++) {
                out.println(arr[i] + " " + talkTimeAverage(lines, arr[i]));
            }
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int numOfPeople(String[] lines) {
        return namesList(lines).size() - 1;
    }
    private static String extractName(String line) {
        int index = line.indexOf(":");
        String name = line.substring(0, index+1);
        return name;
    }
    private static String totalMeetingTime(String[] lines) {
        double totalTime = 0.0;

        String[] arr = namesList(lines).toArray(new String[namesList(lines).size()]);
        for (int i = 1; i < arr.length; i++) {
            String totalString = peopleTalkTime(lines, arr[i]);
            String time = totalString.substring(0, 4);
            double theTime = Double.parseDouble(time);
            totalTime = totalTime + theTime;
        }
        return totalTime + " min";
    }
    private static int numOfSwitches(String[] lines) {
        int counter = 0;
        for (int i = 4; i < lines.length-4; i+=4) {
                int index = lines[i].indexOf(":");
                int index2 = lines[i+4].indexOf(":");
                String firstPerson = lines[i].substring(0, index+1);
                String secondPerson = lines[i+4].substring(0, index2+1);
                if (firstPerson.equals("")) {
                    int oldIndex = lines[i-4].indexOf(":");
                    firstPerson = lines[i-4].substring(0, oldIndex+1);
                }
                if (secondPerson.equals("")) secondPerson = lines[i].substring(0, index+1);
                if (!firstPerson.equals(secondPerson)) {
                    counter++;
            }
        }
        return counter;
    }
    private static String peopleTalkTime(String[] lines, String name) {
        double total_time = 0.0;
        for (int i = 3; i < lines.length; i+=4) {
            if (lines[i+1].contains(name)) {
                double time = calcMin(lines[i]);
                total_time = total_time + time;
            }
        }
        return String.format("%.2f", total_time) + " min";
    }
    private static String talkTimeAverage(String[] lines, String name) {
        double total_time = 0.0;
        int counter = 0;
        for (int i = 3; i < lines.length; i+=4) {
            if (lines[i+1].contains(name)) {
                double time = calcMin(lines[i]);
                total_time = total_time + time;
                counter++;
            }
        }
        return String.format("%.2f", total_time/counter) + " min";
    }
    private static double calcMin(String line) {
        String start = line.substring(0, 11);
        String end = line.substring(17, 29);

        String minStart = start.substring(3, 5);
        String secondStart = start.substring(6, 11);

        String minEnd = end.substring(3, 5);
        String secondEnd = end.substring(6, 11);

        double minS = Double.parseDouble(minStart);
        double secS = Double.parseDouble(secondStart);
        double secStart = secS/60;
        double startTotalMin = minS + secStart;

        double minE = Double.parseDouble(minEnd);
        double secE = Double.parseDouble(secondEnd);
        double secEnd = secE/60;
        double endTotalMin = minE + secEnd;

        double total = endTotalMin - startTotalMin;

        return total;
    }
    private static Set<String> namesList(String[] lines) {
        ArrayList<String> names = new ArrayList<>();
        for (int i = 4; i < lines.length; i+=4) {
            String name = extractName(lines[i]);
            names.add(name);
        }
        Set<String> uniquePeople = new HashSet<>(names);
        return uniquePeople;
    }
    private static String readFile(String filePath) {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath))) {

            String line = br.readLine();
            while ( line != null) {
                sb.append(line).append(System.getProperty("line.separator"));
                line = br.readLine();
            }

        } catch (Exception errorObj) {
            System.err.println("Couldn't read file: " + filePath);
            errorObj.printStackTrace();
        }

        return sb.toString();
    }
}