package com.enjin.bukkit.util.text;

import com.enjin.core.Enjin;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class TextUtils {
    public static final int MINECRAFT_CONSOLE_WIDTH = 320;

    public static int getWidth(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        text = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', text));

        return GlyphUtil.getTextWidth(text) + (text.length() - 1);
    }

    public static String trim(String text, String ellipses) {
        String output = text;
        Enjin.getLogger().debug("Text Width: " + getWidth(output));
        if (getWidth(output) > MINECRAFT_CONSOLE_WIDTH) {
            output = output.substring(0, output.length() - 1);

            while (getWidth(output + (ellipses == null ? "" : ellipses)) > MINECRAFT_CONSOLE_WIDTH) {
                output = output.substring(0, output.length() - 1);
            }

            return output + (ellipses == null ? "" : ellipses);
        } else {
            return output;
        }
    }

    public static List<String> splitToListWithPrefix(String text, int length, String prefix) {
        List<String> result = new ArrayList<>();
        String[]     parts  = text.split(" ");

        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (builder.length() + part.length() > length) {
                result.add(prefix + builder.toString().trim());
                builder = new StringBuilder();
            } else {
                builder.append(" ").append(part);
            }
        }

        if (builder.length() > 0) {
            result.add(prefix + builder.toString().trim());
        }

        return result;
    }

    public static String concat(List<String> list, String glue) {
        return concat(list, glue, 0);
    }

    public static String concat(List<String> list, String glue, int startIndex) {
        StringBuilder builder = new StringBuilder();

        for (int i = startIndex; i < list.size(); i++) {
            if (i > startIndex)
                builder.append(glue);
            builder.append(list.get(i));
        }

        return builder.toString();
    }

    public static String colorize(char prefix, String text) {
        return ChatColor.translateAlternateColorCodes(prefix, text);
    }

    public static String colorize(String text) {
        return colorize('&', text);
    }
}
