package org.s1queence.plugin.spots;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.libs.block.implementation.Section;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.s1queence.api.S1TextUtils.*;

public class SpotsHandler {
    public static Map<String, Spot> spots = new HashMap<>();

    public static void run(RPWorldInteractions plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (spots.isEmpty()) return;

                for (Map.Entry<String, Spot> entry : spots.entrySet()) {
                    Spot value = entry.getValue();
                    List<Player> members = value.getMembers();
                    Collection<Entity> nearbyEntities = value.getWorld().getNearbyEntities(value.getCenterLocation(), value.getRadius(), value.getRadius(), value.getRadius());
                    String textContent = value.getTextContent();

                    for (Entity entity : nearbyEntities) {
                        if (!(entity instanceof Player)) continue;
                        Player player = (Player) entity;
                        if (!members.contains(player)) {
                            if (textContent == null || textContent.isEmpty()) continue;
                            members.add(player);
                            player.sendMessage(textContent);
                            String soundName = plugin.getOptionsConfig().getString("sounds.spot_join");
                            if (soundName != null && !soundName.equalsIgnoreCase("none"))
                                player.playSound(player.getLocation(), soundName, 0.9f, 1.0f);
                        }
                    }

                    members.removeIf(member -> !nearbyEntities.contains(member));
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    public static void fill(RPWorldInteractions plugin) {
        Section spotsSection = plugin.getSpotsConfig().getSection("spots");
        if (spotsSection == null) return;
        Map<String, Object> stringedSpots = spotsSection.getStringRouteMappedValues(true);
        if (stringedSpots.isEmpty()) return;
        for (String key : stringedSpots.keySet()) {
            if (key.contains(".")) continue;
            Object value = stringedSpots.get(key);
            if (!(value instanceof Section)) continue;
            Map<String, Object> mappedSpot = ((Section)value).getStringRouteMappedValues(true);
            Object stringedWorld = mappedSpot.get("world");
            if (!(stringedWorld instanceof String)) continue;
            World world = plugin.getServer().getWorld((String)stringedWorld);
            if (world == null) continue;
            Object stringedLoc = mappedSpot.get("center_location");
            if (!(stringedLoc instanceof String)) continue;
            Location center = getLocationFromString('_', (String)stringedLoc, world);
            if (center == null) continue;
            Object radius = mappedSpot.get("radius");
            if (!(radius instanceof Integer)) continue;
            Object textContent = mappedSpot.get("text_content");
            Spot spot = textContent instanceof String ? new Spot(key, (int)radius, center, (String)textContent, plugin) : new Spot(key, (int)radius, center, plugin);
            spots.put(key, spot);
        }
    }

}
