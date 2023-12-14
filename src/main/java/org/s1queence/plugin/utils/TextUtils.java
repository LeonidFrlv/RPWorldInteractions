package org.s1queence.plugin.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.libs.YamlDocument;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.s1queence.api.S1TextUtils.getConvertedTextFromConfig;

public class TextUtils {

    public static void sendPlayerViewToPlayer(Player receiver, String holderName, RPWorldInteractions plugin) {
        YamlDocument lookAtConfig = plugin.getLookAtConfig();
        String permView = ofNullable(lookAtConfig.getString(String.join(".", "players", holderName, "perm"))).orElse(getConvertedTextFromConfig(plugin.getTextConfig(),"lookat.no_perm", plugin.getName()));
        String tempView = ofNullable(lookAtConfig.getString(String.join(".", "players", holderName, "temp"))).orElse(getConvertedTextFromConfig(plugin.getTextConfig(),"lookat.no_temp", plugin.getName()));
        receiver.sendMessage(getConvertedTextFromConfig(plugin.getTextConfig(),"lookat.name_text", plugin.getName()) + ChatColor.RESET + holderName);
        receiver.sendMessage(getConvertedTextFromConfig(plugin.getTextConfig(),"lookat.perm_view_text", plugin.getName()) + ChatColor.RESET + permView);
        receiver.sendMessage(getConvertedTextFromConfig(plugin.getTextConfig(),"lookat.temp_view_text", plugin.getName()) + ChatColor.RESET + tempView);
        receiver.playSound(receiver.getLocation(), plugin.getOptionsConfig().getString("sounds.look_at"), 0.9f, 1.0f);
    }

    public static void sendEntityViewToPlayer(Player receiver, String eType, RPWorldInteractions plugin) {
        String eView = ofNullable(plugin.getLookAtConfig().getString(String.join(".", "default_entities", eType))).orElse(getConvertedTextFromConfig(plugin.getTextConfig(),"lookat.no_entity_view", plugin.getName()));
        receiver.sendMessage(getConvertedTextFromConfig(plugin.getTextConfig(),"lookat.entity_view_text", plugin.getName()) + ChatColor.RESET + eView);
        receiver.playSound(receiver.getLocation(), plugin.getOptionsConfig().getString("sounds.look_at"), 0.9f, 1.0f);
    }

    public static List<String> getClearedFormatList(List<String> toParse) {
        toParse.replaceAll(row -> ("§r§f" + row.replace("&", "").replace("§", "")));
        return toParse;
    }

    private static String removeFirstSpace(String str) {
        return str.indexOf(" ") == 0 ? str.replaceFirst(" ", "") : str;
    }

    public static List<String> getRowsList(String toParse, int maxRowLength) {
        List<String> stringedCharacters = new ArrayList<>();
        StringBuilder character = new StringBuilder();

        for (int i = 0; i < toParse.length(); i++) {
            Character c1 = toParse.charAt(i);
            Character next = i != toParse.length() - 1 ? toParse.charAt(i + 1) : null;

            if (next != null && c1.equals(' ') || !Character.isLetterOrDigit(c1) && !c1.equals('\'')) {
                if (character.length() != 0) stringedCharacters.add(character.toString());
                character = new StringBuilder();
                stringedCharacters.add(c1.toString());
                continue;
            }

            character.append(c1);

            if (i == toParse.length() - 1) stringedCharacters.add(character.toString());
        }

        character = new StringBuilder();
        List<String> buffer = new ArrayList<>();

        if (maxRowLength < 5) maxRowLength = 13;

        for (int i = 0; i < stringedCharacters.size(); i++) {
            String current = stringedCharacters.get(i);
            String next = i != stringedCharacters.size() - 1 ? stringedCharacters.get(i + 1) : null;
            if (current.length() > maxRowLength) {
                for (int k = 0; k < current.length(); k++) {
                    Character c1 = current.charAt(k);
                    if (character.length() == maxRowLength) {
                        buffer.add(removeFirstSpace(character.toString()));
                        character = new StringBuilder();
                    }

                    character.append(c1);
                    if (k == character.length() - 1) buffer.add(removeFirstSpace(character.toString()));
                }
                continue;
            }

            if (character.length() + current.length() > maxRowLength) {
                buffer.add(removeFirstSpace(character.toString()));

                character = new StringBuilder();
            }

            if (next != null && next.length() == 1 && !Character.isLetterOrDigit(next.toCharArray()[0])) {
                character.append(current).append(next);
                i++;
                if (i == stringedCharacters.size() - 1) buffer.add(removeFirstSpace(character.toString()));
                continue;
            }

            character.append(current);
            if (i == stringedCharacters.size() - 1) buffer.add(removeFirstSpace(character.toString()));
        }

        return buffer;
    }
}
