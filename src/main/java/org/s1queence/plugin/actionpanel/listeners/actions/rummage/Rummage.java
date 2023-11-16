package org.s1queence.plugin.actionpanel.listeners.actions.rummage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.actionpanel.listeners.actions.CoopRPAction;
import org.s1queence.plugin.utils.MyUtils;

import static org.s1queence.plugin.utils.MyUtils.empty;

public class Rummage extends CoopRPAction {
    public Rummage(@NotNull Player player, @NotNull Player target, @NotNull RPWorldInteractions plugin) {
        super(player, target, plugin);
        actionCountDown(7, ("Вы проводите " + ChatColor.GOLD + "обыск" + ChatColor.RESET + "."), ("Вас " + ChatColor.RED + "обыскивают" +  ChatColor.RESET + "."), (ChatColor.RED + "Начинается осмотр инвентаря!"), (ChatColor.RED + "Начат процесс осмотра инвентаря!"));
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

        Inventory oldTargetInventory = target.getInventory();
        Inventory newTargetInventory = Bukkit.createInventory(null, 54, "Предметы " + target.getName());
        for (int i = 0; i < 54; i++) {
            if (i <= 40) {
                newTargetInventory.setItem(i, oldTargetInventory.getItem(i));
                continue;
            }
            newTargetInventory.setItem(i, empty());
        }

        player.openInventory(newTargetInventory);

        InventoryView targetInvView = player.getOpenInventory();

        new BukkitRunnable() {
            int time = 2400;
            @Override
            public void run() {
                if (time == 0) {
                    cancelAction("Время на осмотр предметов истекло");
                    player.closeInventory();
                    cancel();
                    return;
                }

                if (isActionCanceled() || !player.getOpenInventory().equals(targetInvView)) {
                    cancelAction("Осмотр предметов прерван или завершён");
                    if (!target.isDead() && target.isOnline()) {
                        for (int i = 0; i <= 40; i++) {
                            oldTargetInventory.setItem(i, newTargetInventory.getItem(i));
                        }
                    }
                    cancel();
                    return;
                }

                if (time % 20 == 0) {
                    target.sendTitle("Идёт осмотр предметов", (ChatColor.RED + " Присядьте" + ChatColor.WHITE + ", чтобы помешать! "), 0, 100, 0);
                    MyUtils.sendActionBarMsg(player, getCountDownActionBar(120, time / 20));
                    MyUtils.sendActionBarMsg(target, getCountDownActionBar(120, time / 20));
                }

                target.closeInventory();
                time--;
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}
