package ru.kolayghost.kspec;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Kspec extends JavaPlugin implements CommandExecutor {
    File configFile;
    FileConfiguration config;
    private final String KSPECOFF_PERMISSION = "kspecoff.use";
    private final String KSPEC_PERMISSION = "kspec.use";
    private final Map<UUID, Location> previousLocations = new HashMap<>();
    private final Map<UUID, GameMode> previousGameModes = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("KSpecPlugin enabled!");
        getLogger().info("Плагин был создан Kolay_Ghost");
        getCommand("kspec").setExecutor(this);
        getCommand("kspecoff").setExecutor(this);
    }

    @Override
    public void onDisable() {
        getLogger().info("KSpecPlugin disabled!");
        getLogger().info("Плагин был создан Kolay_Ghost");
        restoreAllSpectators();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда может быть использована только игроками.");
            return true;
        }
        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("kspec")) {

            if (!player.hasPermission(KSPEC_PERMISSION)) {
                player.sendMessage(ChatColor.RED + "У вас нет прав на использование этой команды.");
                return true;
            }
            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "Использование: /kspec <ник>");
                return true;
            }

            String targetName = args[0];
            Player target = Bukkit.getPlayer(targetName);

            if (target == null) {
                player.sendMessage(ChatColor.RED + "Игрок " + targetName + " не найден.");
                return true;
            }
            enterSpectatorMode(player, target);
            return true;
        } else if (command.getName().equalsIgnoreCase("kspecoff")) {

            if (!player.hasPermission(KSPECOFF_PERMISSION)) {
                player.sendMessage(ChatColor.RED + "У вас нет прав на использование этой команды.");
                return true
                        ;
            }
            restorePlayer(player);
            player.setGameMode(GameMode.SURVIVAL);
            return true;
        }
        return false;
    }


    private void enterSpectatorMode(Player player, Player target) {
        UUID playerId = player.getUniqueId();

        // Save current location and game mode
        previousLocations.put(playerId, player.getLocation());
        previousGameModes.put(playerId, player.getGameMode());
        // Teleport to target
        player.teleport(target);

        // Set player to spectator mode
        player.setGameMode(GameMode.SPECTATOR);
        player.sendMessage(ChatColor.GREEN + "Вы телепортированы к " + target.getName() + " в режиме наблюдения.");
    }

    private void restorePlayer(Player player) {
        UUID playerId = player.getUniqueId();
        if (previousLocations.containsKey(playerId)) {
            player.teleport(previousLocations.remove(playerId));
        }
        if (previousGameModes.containsKey(playerId)) {
            player.setGameMode(previousGameModes.remove(playerId));
        }
        player.sendMessage(ChatColor.GREEN + "Режим наблюдения отключен.");
    }

    private void restoreAllSpectators() {
        for (UUID playerId : previousLocations.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                restorePlayer(player);
            }
        }
        previousLocations.clear();
        previousGameModes.clear();
    }
}