package searchengine.utils;

public class Utils {

    public static String[] concatTwoStringArrays(String[] array1, String[] array2){
        String[] result = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }
}
