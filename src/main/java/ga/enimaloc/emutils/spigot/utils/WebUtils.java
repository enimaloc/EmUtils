package ga.enimaloc.emutils.spigot.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public class WebUtils {

    /**
     * Check if minecraft user is premium <b>Not fully work</b>
     *
     * @param uuid uuid of player to check
     * @return true if minecraft account is premium otherwise return false
     * @throws IOException if <a href="https://sessionserver.mojang.com">sessionserver.mojang.com</a> can't be reach
     */
    public static boolean isUsernamePremium(UUID uuid) throws IOException {
        URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid); // Ask minecraft api
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream())); // Get output of the url
        String line;
        StringBuilder result = new StringBuilder();
        while ((line = in.readLine()) != null) {
            result.append(line); // Build result into string
        }
        return !result.toString().equals(""); // Check if result is empty(=cracked account)
    }

}
