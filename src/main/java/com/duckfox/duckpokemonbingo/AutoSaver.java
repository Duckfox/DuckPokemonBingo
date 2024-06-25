package com.duckfox.duckpokemonbingo;

import com.duckfox.duckapi.DuckPlugin;
import com.duckfox.duckpokemonbingo.api.Savable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.HashSet;

public final class AutoSaver {
    public static final AutoSaver INSTANCE = new AutoSaver();
    private HashSet<Savable> savables;
    private AutoSaver(){
    }
    public void registerSavable(final Savable savable) {
        savables.add(savable);
    }
    public void unregisterSavable(final Savable savable) {
        savables.remove(savable);
    }
    public void init(){
        savables = new HashSet<>();
        new BukkitRunnable() {
            public void run() {
                saveAll();
            }
        }.runTaskTimerAsynchronously(DuckPokemonBingo.instance,0,DuckPokemonBingo.getConfigManager().getInteger("autosave.period") * 60 * 1000L);
    }
    public void saveAll(){
        for (Savable savable : savables) {
            try {
                savable.save();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
