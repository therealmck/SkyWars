package me.therealmck.skywars.data;

import me.therealmck.skywars.Main;
import me.therealmck.skywars.data.players.GamePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Queue {
    private List<Player> regularQueue;
    private List<Player> fastPassQueue;
    private List<Game> customGameQueue;

    public Queue() {
        this.regularQueue = new ArrayList<>();
        this.fastPassQueue = new ArrayList<>();
        this.customGameQueue = new ArrayList<>();
    }

    public List<Player> getRegularQueue() {
        return regularQueue;
    }

    public List<Player> getFastPassQueue() {
        return fastPassQueue;
    }

    public List<Game> getCustomGameQueue() {
        return customGameQueue;
    }

    public void addRegularPlayer(Player player) {
        regularQueue.add(player);
    }

    public void addFastPlayer(Player player) {
        fastPassQueue.add(player);
    }

    public void addGame(Game game) {
        customGameQueue.add(game);
    }

    public void processQueue(Game oldGame) {
        // Logic to add queued players and games to a running game.
        if (!customGameQueue.isEmpty()) {
            // Add a queued custom game to a running game.
            Game game = customGameQueue.get(0);
            customGameQueue.remove(game);

            game.setMap(oldGame.getMap());
            game.fillChests();
            game.beginGame();
            Main.waitingGames.remove(oldGame);
            Main.runningGames.add(game);

        } else {
            // Process fast pass queue before regular queue
            Game game = new Game();
            game.setMap(oldGame.getMap());
            int maxPlayers = Main.skyWarsConfig.getInt("MaximumPlayers");

            for (Player p : fastPassQueue) {
                if (game.getPlayers().size() < maxPlayers) {
                    game.addPlayer(new GamePlayer(p));
                    fastPassQueue.remove(p);
                }
            }

            for (Player p : regularQueue) {
                if (game.getPlayers().size() < maxPlayers) {
                    game.addPlayer(new GamePlayer(p));
                    regularQueue.remove(p);
                }
            }

            Main.waitingGames.remove(oldGame);
            Main.waitingGames.add(game);

        }
    }
}
