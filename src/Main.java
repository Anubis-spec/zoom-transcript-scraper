import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        String fileContents = readFile("sample01.vtt");
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
                double timePerLine = calcSec(lines[i-1]);
                out.println(name + " " + String.format("%.2f", timePerLine) + " sec \n");
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
            ArrayList<String> names = new ArrayList<>();
            for (int i = 4; i < lines.length; i+=4) {
                String name = extractName(lines[i]);
                names.add(name);
            }
            Set<String> uniquePeople = new HashSet<String>(names);
            String arr[] = uniquePeople.toArray(new String[uniquePeople.size()]);
            for (int i = 1; i < arr.length; i++) {
                out.println(arr[i] + " " + totalTalkTime(lines, arr[i]));
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
        ArrayList<String> names = new ArrayList<>();
        for (int i = 4; i < lines.length; i+=4) {
            String name = extractName(lines[i]);
            names.add(name);
        }
        Set<String> uniquePeople = new HashSet<String>(names);
        return uniquePeople.size() - 1;
    }
    private static String extractName(String line) {
        int index = line.indexOf(":");
        String name = line.substring(0, index+1);
        return name;
    }
    private static String totalMeetingTime(String[] lines) {
        int len = lines.length;
        String timeDuration = lines[len-2];
        String minutes = timeDuration.substring(20, 22);
        String seconds = timeDuration.substring(23, 25);
        Integer sec = Integer.valueOf(seconds);
        Integer min = Integer.valueOf(minutes);
        double partOfMinute = (double) sec/60;
        double time = min + partOfMinute;
        String newTime = String.format("%.2f", time) + " min";

        return newTime;
    }
    private static int numOfSwitches(String[] lines) {
        int counter = 0;
        for (int i = 4; i < lines.length-4; i+=4) {
            int index = lines[i].indexOf(":");
            int index2 = lines[i+4].indexOf(":");
            String firstPerson = lines[i].substring(0, index+1);
            String secondPerson = lines[i+4].substring(0, index2+1);
            if (!firstPerson.equals(secondPerson)) {
                counter++;
            }
        }
        return counter;
    }
    private static String totalTalkTime(String[] lines, String name) {
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
    public static double calcSec(String line) {
        String start = line.substring(0, 11);
        String end = line.substring(17, 29);

        String minuteStart = start.substring(3, 5);
        String secondStart = start.substring(6, 11);

        String minuteEnd = end.substring(3, 5);
        String secondEnd = end.substring(6, 11);

        double minS = Double.parseDouble(minuteStart);
        double secS = Double.parseDouble(secondStart);
        double secStart = secS/60;
        double startTotalMin = minS + secStart;

        double minE = Double.parseDouble(minuteEnd);
        double secE = Double.parseDouble(secondEnd);
        double secEnd = secE/60;
        double endTotalMin = minE + secEnd;

        double total_min = endTotalMin - startTotalMin;
        double total_sec = total_min * 60;
        return total_sec;
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