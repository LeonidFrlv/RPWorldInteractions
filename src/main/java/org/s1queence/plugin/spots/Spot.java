package org.s1queence.plugin.spots;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.libs.YamlDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.s1queence.api.S1TextUtils.getConvertedTextFromConfig;
import static org.s1queence.api.S1TextUtils.getStringLocation;
import static org.s1queence.plugin.spots.SpotsHandler.spots;

public class Spot {
    private final List<Player> members = new ArrayList<>();
    private final String name;
    private final int radius;
    private String textContent;
    private final World world;
    private final Location centerLocation;
    private final RPWorldInteractions rpwi;
    public Spot(@NotNull String name, int radius, @NotNull Location centerLocation, @NotNull RPWorldInteractions rpwi) {
        this.name = name;
        this.radius = radius;
        this.centerLocation = centerLocation;
        this.rpwi = rpwi;
        textContent = null;
        world = centerLocation.getWorld();
        YamlDocument spotsConfig = rpwi.getSpotsConfig();
        spotsConfig.set(String.join(".", "spots", name, "center_location"), getStringLocation("_", centerLocation));
        spotsConfig.set(String.join(".", "spots", name, "radius"), radius);
        spotsConfig.set(String.join(".", "spots", name, "world"), Objects.requireNonNull(world).getName());
        try {
            spotsConfig.save();
        } catch (Exception ignored) {

        }
        spots.put(name, this);
    }

    public Spot(@NotNull String name, int radius, @NotNull Location centerLocation, @NotNull String textContent, @NotNull RPWorldInteractions rpwi) {
        this.name = name;
        this.radius = radius;
        this.centerLocation = centerLocation;
        this.textContent = textContent;
        this.rpwi = rpwi;
        world = centerLocation.getWorld();
        YamlDocument spotsConfig = rpwi.getSpotsConfig();
        spotsConfig.set(String.join(".", "spots", name, "center_location"), getStringLocation("_", centerLocation));
        spotsConfig.set(String.join(".", "spots", name, "radius"), radius);
        spotsConfig.set(String.join(".", "spots", name, "text_content"), textContent);
        spotsConfig.set(String.join(".", "spots", name, "world"), Objects.requireNonNull(world).getName());
        try {
            spotsConfig.save();
        } catch (Exception ignored) {

        }
        spots.put(name, this);
    }

    public void setTextContent(String newState) {
        textContent = newState;
        rpwi.getSpotsConfig().set(String.join(".", "spots", name, "text_content"), textContent);
        try {
            rpwi.getSpotsConfig().save();
        } catch (Exception ignored) {

        }
    }

    public TextComponent toTextComponent(boolean splitContent) {
        int limit = 40;
        String subContent = textContent != null ? (textContent.length() > limit && splitContent ? textContent.substring(0, limit) + ChatColor.GRAY + "..." : textContent) : "";
        TextComponent tc = new TextComponent(ChatColor.GOLD + "" + ChatColor.UNDERLINE + name + ChatColor.RESET + " : " + subContent);
        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + getStringLocation(" ", centerLocation)));
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(getConvertedTextFromConfig(rpwi.getTextConfig(), "spots.msg_on_hover_spot_when_get", rpwi.getName()))));
        return tc;
    }

    public void delete() {
        rpwi.getSpotsConfig().set(String.join(".", "spots", name), null);
        try {
            rpwi.getSpotsConfig().save();
        } catch (Exception ignored) {

        }
        spots.remove(name);
    }

    public int getRadius() {return radius;}
    public String getTextContent() {return textContent;}
    public World getWorld() {return world;}
    public String getName() {return name;}
    public Location getCenterLocation() {return centerLocation;}
    public List<Player> getMembers() {return members;}
}
