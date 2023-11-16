package org.s1queence.plugin.actionpanel.listeners.actions;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.actionpanel.utils.ProgressBar;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.utils.MyUtils;

public class CoopRPAction {
    private final Player player;
    private final Player target;
    private final RPWorldInteractions plugin;
    private final ItemStack launchItem;
    private final Float initialPlayerSpeed;
    private final Float initialTargetSpeed;
    public CoopRPAction(@NotNull Player player, @NotNull Player target, @NotNull RPWorldInteractions plugin) {
        this.player = player;
        this.target = target;
        this.plugin = plugin;
        this.launchItem = player.getInventory().getItemInMainHand();
        this.initialPlayerSpeed = player.getWalkSpeed();
        this.initialTargetSpeed = target.getWalkSpeed();
    }

    protected void cancelAction(String msg) {
        plugin.getPlayersInAction().remove(player);
        MyUtils.sendActionBarMsg(player, msg);
        MyUtils.sendActionBarMsg(target, msg);
        player.sendTitle(" ", " ", 0, 0, 0);
        target.sendTitle(" ", " ", 0, 0, 0);
        player.setWalkSpeed(initialPlayerSpeed);
        target.setWalkSpeed(initialTargetSpeed);
        player.closeInventory();
        target.closeInventory();
    }

    protected void playActionSound() {
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.35f, 0.222f);
        target.playSound(target, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.35f, 0.222f);
    }

    protected void setWalkSpeedForAll() {
        player.setWalkSpeed(RPWorldInteractions.PLAYER_RP_ACTION_SPEED);
        target.setWalkSpeed(RPWorldInteractions.PLAYER_RP_ACTION_SPEED);
    }

    protected String getCountDownActionBar(int initial, int current) {
        return ChatColor.GRAY + "{" + ProgressBar.getProgressBar(initial - current, initial, 21, '∎', ChatColor.GOLD, ChatColor.WHITE) + ChatColor.GRAY + "}";
    }

    protected boolean isActionCanceled() {
        boolean isSneaking = player.isSneaking() || target.isSneaking();
        boolean isLaunchItemInitial = player.getInventory().getItemInMainHand().equals(launchItem);
        boolean isTargetNearby = player.getNearbyEntities(1.65f, 0.5f, 1.65f).contains(target);
        boolean isInAction = plugin.isPlayerInAction(player) || plugin.isPlayerInAction(target);
        boolean isOnline = player.isOnline() || target.isOnline();
        boolean isDead = player.isDead() || target.isDead();

        return isSneaking || !isLaunchItemInitial || !isTargetNearby || !isInAction || !isOnline || isDead;
    }

    protected void actionCountDown(@NotNull Integer seconds, @NotNull String processPlayerMsg, @NotNull String processTargetMsg, @NotNull String completePlayerMsg, @NotNull String completeTargetMsg) {
        setWalkSpeedForAll();
        if (plugin.isPlayerInAction(player) || plugin.isPlayerInAction(target)) return;
        if (!plugin.isActionCoolDownExpired(player) || !plugin.isActionCoolDownExpired(target)) return;
        plugin.getPlayersInAction().put(player, target);
        plugin.getItemActionCoolDown().put(player, target);

        new BukkitRunnable() {
            int time = seconds * 20;
            @Override
            public void run() {
                if (isActionCanceled()) {
                    cancelAction("Действие прервано игроком!");
                    plugin.getItemActionCoolDown().remove(player);
                    cancel();
                    return;
                }

                if (time % 20 == 0) {
                    player.sendTitle(" ", processPlayerMsg, 0, 100, 0);
                    target.sendTitle(" ", processTargetMsg, 0, 100, 0);
                    MyUtils.sendActionBarMsg(player, getCountDownActionBar(seconds, time / 20));
                    MyUtils.sendActionBarMsg(target, getCountDownActionBar(seconds, time / 20));
                    playActionSound();
                }

                if (time == 0) {
                    MyUtils.sendActionBarMsg(player, "");
                    MyUtils.sendActionBarMsg(target, "");
                    plugin.getItemActionCoolDown().remove(player);
                    player.sendTitle(" ", completePlayerMsg, 0, 50, 15);
                    target.sendTitle(" ", completeTargetMsg, 0, 50, 15);
                    cancel();
                    return;
                }

                player.closeInventory();
                target.closeInventory();
                time--;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    protected Player getPlayer() {return player;}
    protected Player getTarget() {return target;}
    protected RPWorldInteractions getPlugin() {return plugin;}
}
