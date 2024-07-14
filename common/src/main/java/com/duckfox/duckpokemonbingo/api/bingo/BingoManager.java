package com.duckfox.duckpokemonbingo.api.bingo;

import com.duckfox.duckpokemonbingo.DuckPokemonBingo;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class BingoManager {
    TreeSet<Bingo> bingoSet;

    public BingoManager() {
        this.bingoSet = new TreeSet<>();
    }

    public void initAllBingo() {
        ConfigurationSection bingoSection = DuckPokemonBingo.getConfigManager().getSection("bingo");
        Set<String> keys = bingoSection.getKeys(false);
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                ConfigurationSection section = bingoSection.getConfigurationSection(key);
                Bingo bingo = Bingo.createBingo(section);
                registerBingo(bingo);
            }
        }
    }

    public void unregisterAllBingo() {
        for (Bingo bingo : getBingoSet()) {
            DuckPokemonBingo.saver.unregisterSavable(bingo);
        }
        getBingoSet().clear();
    }

    public Set<Bingo> getBingoSet() {
        return bingoSet;
    }

    public void registerBingo(Bingo bingo) {
        this.bingoSet.add(bingo);
        DuckPokemonBingo.saver.registerSavable(bingo);
    }

    public void unregisterBingo(Bingo bingo) {
        this.bingoSet.remove(bingo);
        DuckPokemonBingo.saver.unregisterSavable(bingo);
    }

    public Bingo getBingo(String id) {
        for (Bingo bingo : bingoSet) {
            if (Objects.equals(id, bingo.id)) {
                return bingo;
            }
        }
        return null;
    }
}
