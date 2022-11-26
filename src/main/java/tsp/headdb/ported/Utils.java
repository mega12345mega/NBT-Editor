package tsp.headdb.ported;

import java.util.regex.Pattern;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.util.Formatting;

public class Utils {


    public static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    /**
     * Validate a UUID (version 4)
     *
     * @param uuid UUID to be validated
     * @return Returns true if the string is a valid UUID
     */
    public static boolean validateUniqueId(String uuid) {
        return UUID_PATTERN.matcher(uuid).matches();
    }

    public static void sendMessage(String message) {
        MainUtil.client.player.sendMessage(TextInst.of(colorize(message)), false);
    }

    public static String colorize(String string) {
        return MainUtil.colorize(Formatting.GRAY + string);
    }

}
