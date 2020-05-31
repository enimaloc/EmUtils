package ga.enimaloc.emutils.spigot.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public class WebUtils {

    public static boolean isUsernamePremium(UUID id) throws IOException {
        URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/"+id);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String line;
        StringBuilder result = new StringBuilder();
        while ((line = in.readLine())!=null){
            result.append(line);
        }
        return !result.toString().equals("");
    }

}
