package MacroModelJon.utils;

import java.util.Collection;

public class StringUtils {

    public static String mkString(String[] strings, String link) {
        if (strings.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(strings[0]);

        for (int i = 1; i < strings.length; i++) {
            sb.append(link).append(strings[i]);
        }

        return sb.toString();
    }

    public static String mkString(Collection<String> strings, String link) {
        return mkString(strings.toArray(new String[0]), link);
    }

}
