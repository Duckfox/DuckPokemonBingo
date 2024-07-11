package com.duckfox.duckpokemonbingo.command;

import com.duckfox.duckpokemonbingo.DuckPokemonBingo;
import com.duckfox.duckpokemonbingo.api.bingo.Bingo;
import com.duckfox.duckpokemonbingo.api.bingo.BingoPokemon;
import com.duckfox.duckpokemonbingo.gui.MainBingo;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class BingoCommand implements CommandExecutor, TabCompleter {
    public static final List<String> COMMANDS = Arrays.asList("open", "reset", "help", "reload", "save", "status");

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender instanceof Player && args.length >= 2 && "open".equalsIgnoreCase(args[0])) {
            String arg = args[1];
            Player player = (Player) commandSender;
            Bingo bingo = DuckPokemonBingo.bingoManager.getBingo(arg);
            if (bingo != null) {
                MainBingo.openBingo(player, bingo);
                return true;
            } else {
                DuckPokemonBingo.getMessageManager().sendMessage(player, "unknownBingoId", "%id%", arg);
                return false;
            }
        } else if (commandSender.hasPermission("duckpokemonbingo.reset") && args.length >= 2 && "reset".equalsIgnoreCase(args[0])) {
            Bingo bingo = DuckPokemonBingo.bingoManager.getBingo(args[1]);
            if (bingo != null) {
                if (args.length >= 3) {
                    Player player = Bukkit.getPlayer(args[2]);
                    if (player != null) {
                        bingo.resetPlayer(player);
                        DuckPokemonBingo.getMessageManager().sendMessage(commandSender, "reset.singlePlayerSuccess", "%player%", args[2], "%bingo%", bingo.id);
                        return true;
                    } else {
                        DuckPokemonBingo.getMessageManager().sendMessage(commandSender, "unknownPlayer", "%player%", args[2]);
                        return false;
                    }
                } else {
                    bingo.resetAll();
                    DuckPokemonBingo.getMessageManager().sendMessage(commandSender, "reset.allSuccess", "%bingo%", args[1]);
                    return true;
                }
            } else {
                DuckPokemonBingo.getMessageManager().sendMessage(commandSender, "unknownBingoId", "%id%", args[1]);
                return false;
            }
        } else if (args.length >= 1 && "help".equalsIgnoreCase(args[0])) {
            DuckPokemonBingo.getMessageManager().sendMessages(commandSender, "help");
            return true;
        } else if (args.length >= 1 && "reload".equalsIgnoreCase(args[0])) {
            DuckPokemonBingo.instance.reload();
            DuckPokemonBingo.getMessageManager().sendMessage(commandSender, "reload");
            return true;
        } else if (args.length >= 1 && "save".equalsIgnoreCase(args[0])) {
            DuckPokemonBingo.saver.saveAll();
            DuckPokemonBingo.getMessageManager().sendMessage(commandSender, "save");
            return true;
        } else if (commandSender.hasPermission("duckpokemonbingo.status") && args.length >= 2 && "status".equalsIgnoreCase(args[0])) {
            Bingo bingo = DuckPokemonBingo.bingoManager.getBingo(args[1]);
            if (bingo != null) {
                if (args.length >= 3) {
                    Player player = Bukkit.getPlayer(args[2]);
                    if (player != null) {
                        List<BingoPokemon> list = bingo.getPokemons(player);
                        int i = 0;
                        StringBuilder sb = new StringBuilder();
                        for (BingoPokemon bingoPokemon : list) {
                            i++;
                            sb.append(bingoPokemon.status == BingoPokemon.BingoStatus.PROGRESSING ? "&c0 " : "&a1 ");
                            if (i % 5 == 0) {
                                sb.append("\n");
                            }
                        }
                        DuckPokemonBingo.getMessageManager().sendMessage(commandSender, sb.toString().isEmpty() ? "status.empty" : sb.toString(), sb.toString().isEmpty());
                        return true;
                    } else {
                        DuckPokemonBingo.getMessageManager().sendMessage(commandSender, "unknownPlayer", "%player%", args[2]);
                        return false;
                    }
                }
            } else {
                DuckPokemonBingo.getMessageManager().sendMessage(commandSender, "unknownBingoId", "%id%", args[1]);
                return false;
            }
        }
        DuckPokemonBingo.getMessageManager().sendMessage(commandSender, "unknownCommand", "%command%", args.length >= 1 ? args[0] : "");
        return false;

    }
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

        if (args.length == 1) {
            ArrayList<String> list = new ArrayList<>(COMMANDS);
            list.removeIf(s1 -> !s1.startsWith(args[0].toLowerCase()));
            return list;
        }
        if (args.length == 2) {
            if ("reset".equalsIgnoreCase(args[0]) || "open".equalsIgnoreCase(args[0]) || "status".equalsIgnoreCase(args[0])) {
                Set<Bingo> bingoSet = DuckPokemonBingo.bingoManager.getBingoSet();
                List<String> list = new ArrayList<>(bingoSet.size());
                for (Bingo bingo : bingoSet) {
                    list.add(bingo.id);
                }
                list.removeIf(s1 -> !s1.startsWith(args[1].toLowerCase()));
                return list;
            }
        }
        if (args.length == 3) {
            if ("reset".equalsIgnoreCase(args[0]) || "status".equalsIgnoreCase(args[0])) {
                List<String> players = new ArrayList<>(Bukkit.getOnlinePlayers()).stream().map(Player::getName).collect(Collectors.toList());
                players.removeIf((player) -> !player.toLowerCase().startsWith(args[2].toLowerCase()));
                return players;
            }
        }
        return Collections.emptyList();
    }
}
