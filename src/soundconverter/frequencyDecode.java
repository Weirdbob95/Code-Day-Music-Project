package soundconverter;

import data.Note;
import java.util.*;

//Methods in static because testing purposes.
public class frequencyDecode {

    public static final double BASE_CONSTANT = 261.63;
    public static final double A_CONSTANT = Math.pow(2, (double) 1 / 12);
    public static final int ERROR_CONSTANT = 2;

    //returns Note in an arraylist. Not done yet.
    public static ArrayList<Note> convert(double[] array) {
        int[] halfsteps = getNote(array);
        Map<String, Double> noteMap = getNoteTime(getNote(array));
        int quarter = findQuarterNote(noteMap);
        int size = noteMap.keySet().size();
        int count = 0;
        ArrayList<Note> ans = new ArrayList<Note>();
        for (String key : noteMap.keySet()) {
            int difference = parString(key, "-");
            if (difference > ERROR_CONSTANT) {
                double ithSecond = noteMap.get(key); //difference.40.00
                double closestSecond = 0;
                Note note;
                double quarterSecond = (double) quarter / 40.00;
                //System.out.println("this is quarter second: " + quarterSecond);
                if (ithSecond < (double) quarter / 40.00) {
                    closestSecond = getCloseNote(ithSecond, 2 * quarterSecond, true, quarterSecond);
                    //System.out.println("this is closestSecond: " + closestSecond);
                    note = new Note(halfsteps[count], (int) (closestSecond / quarterSecond));
                } else {
                    closestSecond = getCloseNote(ithSecond, quarterSecond / 2, false, quarterSecond);
                    //System.out.println("this is closestSecond: " + closestSecond);
                    note = new Note(halfsteps[count], (int) (quarterSecond / closestSecond));
                }
                count = count + difference;
                ans.add(note);
                //Note note = new Note(halfsteps[count], , 1);
            }
        }
        return ans;
    }

    //returns int[] of halfsteps from C4.
    public static int[] getNote(double[] array) {
        int[] ans = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            double frequency = array[i];
            if (frequency != 0) {
                int halfsteps = (int) Math.round((Math.log(frequency / BASE_CONSTANT) / Math.log(A_CONSTANT)));
                ans[i] = halfsteps;
            } else {
                ans[i] = 100000;
            }
        }
        return ans;
    }

    //gets the time in Map form where the key is the positions of array and value
    //is the time in 40th of a second.
    public static Map<String, Double> getNoteTime(int[] input) {
        Map<String, Double> ans = new HashMap<String, Double>();
        int start = 0;
        int prev = input[0];
        double difference = 0;
        for (int i = 1; i < input.length; i++) {
            if (prev != input[i]) {
                difference = i - start - 1;
                ans.put(start + "-" + (i - 1), difference / 40.00);
                start = i - 1;
                prev = input[i];
            }
        }
        difference = input.length - start - 1;
        ans.put(start + "-" + (input.length), difference / 40.00);
        return ans;
    }

    //finds the quarter note such that x/40 where x is the most common note length.
    public static int findQuarterNote(Map<String, Double> noteMap) {
        Set<String> keys = noteMap.keySet();
        Map<Integer, Integer> countMap = new HashMap<Integer, Integer>();
        Set<Integer> countKeys = new HashSet<Integer>();
        int ans = 0;
        int asdf = 0;
        for (String key : keys) {
            int difference = parString(key, "-");
            if (countMap.containsKey(difference)) {
                int value = countMap.get(difference);
                countMap.remove(difference);
                countMap.put(difference, value + 1);
            } else {
                countMap.put(difference, 1);
            }
        }
        countKeys = countMap.keySet();
        for (int key : countKeys) {
            int value = countMap.get(key);
            if (value > ans) {
                ans = value;
                asdf = key;
            }
        }
        return asdf;
    }

    //parese a string.
    private static int parString(String input, String par) {
        String[] temp = input.split(par);
        return Integer.parseInt(temp[1]) - Integer.parseInt(temp[0]);
    }

    //returns note time closest to a note where quarter note is the base. (needs to be checked)
    private static double getCloseNote(double value, double current, boolean type, double difference) {
        if (type) {
            if (Math.abs(value - current) > difference) {
                return current / 2;
            } else {
                return getCloseNote(value, current * 2, type, Math.abs(value - current));
            }
        } else {
            if (Math.abs(value - current) > difference) {
                return current * 2;
            } else {
                return getCloseNote(value, current / 2, type, Math.abs(value - current));
            }
        }
    }
}
