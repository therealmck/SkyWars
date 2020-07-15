package me.therealmck.skywars.listeners;

import me.therealmck.skywars.Main;
import me.therealmck.skywars.data.Game;
import me.therealmck.skywars.data.players.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportCanceller implements Listener {
    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player p = event.getPlayer();
        boolean shouldCancel = false;

        for (Game game : Main.runningGames) {
            for (GamePlayer player : game.getPlayers()) {
                if (player.getBukkitPlayer().equals(p)) shouldCancel = true;
            }
        }

        if (shouldCancel && event.getCause().equals(PlayerTeleportEvent.TeleportCause.COMMAND)) {
            event.setCancelled(true);
            p.sendMessage("You can't teleport out while a game is running!");
        }
    }
}
