package ga.enimaloc.emutils.spigot.utils;

import ga.enimaloc.emutils.spigot.EmUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {

    /**
     * @param actual actual level
     * @param width width of bar
     * @return formatted progressbar (ex. ⬛⬛⬛⬛⬜⬜⬜⬜⬜⬜)
     */
    public static String formatProgressBar(int actual, int width) {
        return formatProgressBar(actual, 100, width, '⬛', '⬜');
    }

    /**
     * @param actual actual level
     * @param max max level (def. {@code '100'})
     * @param width width of bar
     * @return formatted progressbar (ex. ⬛⬛⬛⬛⬜⬜⬜⬜⬜⬜)
     */
    public static String formatProgressBar(int actual, int max, int width) {
        return formatProgressBar(actual, max, width, '⬛', '⬜');
    }

    /**
     * @param actual actual level
     * @param max max level (def. {@code '100'})
     * @param width width of bar
     * @param completedChar completed char (def. {@code '⬛'})
     * @param uncompletedChar uncompletedChar (def. {@code '⬜'})
     * @return formatted progressbar (ex. ⬛⬛⬛⬛⬜⬜⬜⬜⬜⬜)
     */
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

    /**
     * @param timestamp to convert into Date for format with {@link StringUtils#getFormattedDate(Date)}
     * @return Formatted {@link Date} according to field {@code date-format} config.yml
     */
    public static String getFormattedDate(long timestamp) {
        return getFormattedDate(new Date(timestamp));
    }

    /**
     * @param date to format
     * @return Formatted {@link Date} according to field {@code date-format} config.yml
     */
    public static String getFormattedDate(Date date) {
        FileConfiguration config = EmUtils.getInstance().getConfig();
        return new SimpleDateFormat(
            config.contains("date-format") ?
            config.getString("date-format") :
            ""
        ).format(date);
    }

}
