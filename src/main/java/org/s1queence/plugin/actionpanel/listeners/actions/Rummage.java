package org.s1queence.plugin.actionpanel.listeners.actions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.utils.MyUtils;
import org.s1queence.plugin.utils.TextUtils;

import static org.s1queence.plugin.utils.MyUtils.empty;

public class Rummage extends CoopRPAction {
    public Rummage(@NotNull Player player, @NotNull Player target, @NotNull RPWorldInteractions plugin) {
        super(player, target, plugin);
        actionCountDown(7, "rummage_action");
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isPlayerInAction(player) || !plugin.isPlayerInAction(target)) {
                    cancel();
                    return;
                }

                if (plugin.isActionCoolDownExpired(player) && plugin.isActionCoolDownExpired(target)) {
                    start();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }
    private void start() {
        Player player = getPlayer();
        Player target = getTarget();
        RPWorldInteractions plugin = getPlugin();

        if (plugin.getPlayersInAction().get(player) != target) return;

        Inventory targetInventory = target.getInventory();
        Inventory oldPlayerInventory = Bukkit.createInventory(null, 54, "Предметы " + target.getName());
        Inventory newTargetInventory = Bukkit.createInventory(null, 54, "Предметы " + target.getName());
        for (int i = 0; i < 54; i++) {
            if (i <= 40) {
                newTargetInventory.setItem(i, targetInventory.getItem(i));
                oldPlayerInventory.setItem(i, player.getInventory().getItem(i));
                continue;
            }
            newTargetInventory.setItem(i, empty(plugin));
        }

        player.openInventory(newTargetInventory);

        InventoryView targetInvView = player.getOpenInventory();
        final float ACTION_TIME = 2400.0f;

        new BukkitRunnable() {
            int time = (int)ACTION_TIME;
            @Override
            public void run() {
                if (isActionCanceled() || !player.getOpenInventory().equals(targetInvView) || time == 0) {
                    cancelAction("rummage_action.process.cancel");
                    if (!target.isDead() && target.isOnline()) {
                        for (int i = 0; i <= 40; i++) {
                            ItemStack currentItem = newTargetInventory.getItem(i);
                            if (currentItem == null) continue;
                            String mat = currentItem.getType().toString().toLowerCase();
                            World world = target.getWorld();
                            Location loc = target.getLocation();
                            switch (i) {
                                case 36: {
                                    if (!mat.contains("boots")) {
                                        world.dropItemNaturally(loc, currentItem);
                                        currentItem = null;
                                    }
                                    break;
                                }
                                case 37: {
                                    if (!mat.contains("leggings")) {
                                        world.dropItemNaturally(loc, currentItem);
                                        currentItem = null;
                                    }
                                    break;
                                }
                                case 38: {
                                    if (!mat.contains("chestplate")) {
                                        world.dropItemNaturally(loc, currentItem);
                                        currentItem = null;
                                    }
                                    break;
                                }
                                case 39: {
                                    if (!mat.contains("helmet") && !mat.equals("carved_pumpkin")) {
                                        world.dropItemNaturally(loc, currentItem);
                                        currentItem = null;
                                    }
                                    break;
                                }
                            }
                            targetInventory.setItem(i, currentItem);
                        }
                    }

                    if (!target.isOnline()) {
                        for (int i = 0; i <= 40; i++) {
                            player.getInventory().setItem(i, oldPlayerInventory.getItem(i));
                        }
                    }
                    cancel();
                    return;
                }

                if (time % 20 == 0) {
                    target.sendTitle(TextUtils.getMsg("rummage_action.process.every_second.target.title", plugin), TextUtils.getMsg("rummage_action.process.every_second.target.subtitle", plugin), 0, 100, 0);
                }

                target.closeInventory();
                time--;
                int percent = (int)(((ACTION_TIME - time) / ACTION_TIME) * 100);
                MyUtils.sendActionBarMsg(player, TextUtils.getProgressBarMsg("rummage_action.process.every_tick.action_bar_both", getCountDownProgressBar((int)ACTION_TIME, time), Integer.toString(percent), plugin));
                MyUtils.sendActionBarMsg(target, TextUtils.getProgressBarMsg("rummage_action.process.every_tick.action_bar_both", getCountDownProgressBar((int)ACTION_TIME, time), Integer.toString(percent), plugin));
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}
