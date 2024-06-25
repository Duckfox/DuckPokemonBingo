package com.duckfox.duckpokemonbingo;

import com.duckfox.duckapi.DuckPlugin;
import com.duckfox.duckapi.managers.ConfigManager;
import com.duckfox.duckapi.managers.MessageManager;
import com.duckfox.duckapi.utils.FileUtil;
import com.duckfox.duckpokemonbingo.api.bingo.Bingo;
import com.duckfox.duckpokemonbingo.api.bingo.BingoManager;
import com.duckfox.duckpokemonbingo.command.BingoCommand;
import com.duckfox.duckpokemonbingo.gui.MainBingo;
import net.minecraft.client.main.Main;
import org.bukkit.entity.Player;

import java.util.*;

public final class DuckPokemonBingo extends DuckPlugin {
    public static DuckPokemonBingo instance;
    public static final BingoManager bingoManager = new BingoManager();

    public static MessageManager getMessageManager() {
        return instance.messageManager;
    }

    public static ConfigManager getConfigManager() {
        return instance.configManager;
    }

    public static final AutoSaver saver = AutoSaver.INSTANCE;

    @Override
    public void onEnable() {
        instance = this;
        this.pluginName = "DuckPokemonBingo";
        this.version = "1.0";
        getLogger().info("§b======" + pluginName + " " + version + "======");
        getLogger().info("Author: Duck_fox QQ:2660759310 Email:2660759310@qq.com");
        getLogger().info("Loading AutoSaver...");
        saver.init();
        getLogger().info("AutoSaver loaded!");
        FileUtil.mkDir(this, "data/player");
        FileUtil.mkDir(this, "data/bingo");
        getLogger().info("Loading Bingo...");
        bingoManager.initAllBingo();
        getLogger().info("All Bingo loaded!");

        this.getServer().getPluginCommand("DuckPokemonBingo").setExecutor(new BingoCommand());
        this.getServer().getPluginManager().registerEvents(MainBingo.INSTANCE, this);
    }

    @Override
    public void reload() {
        super.reload();
        saver.saveAll();
        Map<Player, String> copyMap = new HashMap<>(MainBingo.playerInventory);
        for (Player player : copyMap.keySet()) {
            player.closeInventory();
        }
        bingoManager.unregisterAllBingo();
        bingoManager.initAllBingo();
        saver.init();
        for (Player player : copyMap.keySet()) {
            String bingoId = copyMap.get(player);
            Bingo bingo = bingoManager.getBingo(bingoId);
            if (bingo != null) {
                MainBingo.openBingo(player, bingo);
            }
        }
    }

    @Override
    public void onDisable() {
        saver.saveAll();
        for (Player player : MainBingo.playerInventory.keySet()) {
            player.closeInventory();
        }
        bingoManager.unregisterAllBingo();
    }
}
