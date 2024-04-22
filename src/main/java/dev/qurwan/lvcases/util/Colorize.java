package dev.qurwan.lvcases.util;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

@UtilityClass
public class Colorize {
    public @NotNull String a(String input) {
        String text = Pattern.compile("#[a-fA-F0-9]{6}")
                .matcher(input)
                .replaceAll(match -> ChatColor.of(match.group()).toString());
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
