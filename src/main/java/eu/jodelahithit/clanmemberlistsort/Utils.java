package eu.jodelahithit.clanmemberlistsort;

public class Utils {
    public static String removeDecorationsFromString(String name) {
        return name.replaceAll("\\<[^>]*>", "");
    }
}
