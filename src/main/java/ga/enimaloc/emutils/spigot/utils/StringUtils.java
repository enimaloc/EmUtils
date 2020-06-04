package ga.enimaloc.emutils.spigot.utils;

import ga.enimaloc.emutils.spigot.EmUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {

    public static String formatProgressBar(int actual, int width) {
        return formatProgressBar(actual, 100, width, '⬛', '⬜');
    }

    public static String formatProgressBar(int actual, int max, int width) {
        return formatProgressBar(actual, max, width, '⬛', '⬜');
    }

    public static String formatProgressBar(int actual, int max, int width, char completedChar, char uncompletedChar) {
        StringBuilder stringBuilder = new StringBuilder();
        actual = Math.round((float) actual/max*width);
        max = width;
        for (int i = 0; i < width; i++) {
            if (i < actual) stringBuilder.append(completedChar);
            else stringBuilder.append(uncompletedChar);
        }
        return stringBuilder.toString();
    }

    public static String getFormattedDate(long timestamp) {
        return getFormattedDate(new Date(timestamp));
    }

    public static String getFormattedDate(Date date) {
        FileConfiguration config = EmUtils.getInstance().getConfig();
        return new SimpleDateFormat(
            config.contains("date-format") ?
            config.getString("date-format") :
            ""
        ).format(date);
    }

}
