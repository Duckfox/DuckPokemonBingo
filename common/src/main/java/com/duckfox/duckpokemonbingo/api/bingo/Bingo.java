package com.duckfox.duckpokemonbingo.api.bingo;

import com.duckfox.duckapi.utils.FileUtil;
import com.duckfox.duckpokemonbingo.DuckPokemonBingo;
import com.duckfox.duckpokemonbingo.api.Savable;
import com.duckfox.duckpokemonbingo.gui.MainBingo;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class Bingo implements Savable, Comparable<Bingo> {
    public final File file;
    public final BingoType type;
    public final String id;
    public final String name;
    public final boolean checkOriTrainer;
    public final UploadType uploadType;
    public final String tag;
    public FileConfiguration config;
    public final List<BingoReward> rewards;

    @Override
    public int compareTo(Bingo o) {
        return this.id.compareTo(o.id);
    }

    public static Bingo createBingo(ConfigurationSection config) {
        try {
            BingoType bingoType = BingoType.parse(config.getString("type"));
            Class<? extends Bingo> bingoClass = bingoType.bingoClass;
            Constructor<? extends Bingo> constructor = bingoClass.getConstructor(ConfigurationSection.class);
            constructor.setAccessible(true);
            return constructor.newInstance(config);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public BingoReward getReward(String id) {
        for (BingoReward reward : rewards) {
            if (reward.id.equals(id)) {
                return reward;
            }
        }
        return null;
    }

    @Override
    public void save() throws IOException {
        config.save(file);
    }

    public boolean isPlayerGenerated(Player player) {
        return config.contains("bingo." + player.getUniqueId());
    }

    public void updateAll(Player player, List<BingoPokemon> pokemons) {
        for (BingoPokemon pokemon : pokemons) {
            update(player, pokemon, true);
        }
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(Player player, BingoPokemon pokemon, boolean isAll) {
        if (!config.contains("bingo." + player.getUniqueId() + "." + pokemon.id)) {
            config.createSection("bingo." + player.getUniqueId() + "." + pokemon.id);
        }
        ConfigurationSection section = config.getConfigurationSection("bingo." + player.getUniqueId() + "." + pokemon.id);
        pokemon.save(section);
        if (!isAll) {
            if (!DuckPokemonBingo.getConfigManager().getConfig().contains("saveImmediately") || DuckPokemonBingo.getConfigManager().getBoolean("saveImmediately")) {
                try {
                    save();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public boolean isPlayerClaimedReward(BingoReward reward, Player player) {
        return config.contains("rewards." + player.getUniqueId() + "." + reward.id);
    }

    public void setClaimedReward(BingoReward reward, Player player) {
        config.set("rewards." + player.getUniqueId() + "." + reward.id, true);
    }

    public List<BingoPokemon> getPokemons(Player player) {
        List<BingoPokemon> pokemons = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection("bingo." + player.getUniqueId());
        if (section != null) {
            Set<String> keys = section.getKeys(false);
            for (String key : keys) {
                pokemons.add(new BingoPokemon(config.getConfigurationSection("bingo." + player.getUniqueId() + "." + key)));
            }
        }
        return pokemons;
    }

    public void resetAll() {
        config.set("bingo", null);
        config.set("rewards", null);
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Player player : MainBingo.playerInventory.keySet()) {
            if (this.id.equals(MainBingo.playerInventory.get(player))) {
                player.closeInventory();
                MainBingo.openBingo(player, this);
            }
        }
    }

    public void resetPlayer(Player player) {
        config.set("bingo." + player.getUniqueId(), null);
        config.set("rewards." + player.getUniqueId(), null);
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (MainBingo.playerInventory.containsKey(player)) {
            String bingoId = MainBingo.playerInventory.get(player);
            if (this.id.equals(bingoId)) {
                player.closeInventory();
                MainBingo.openBingo(player, this);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bingo bingo = (Bingo) o;
        return type == bingo.type && Objects.equals(id, bingo.id) && Objects.equals(name, bingo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id, name);
    }

    protected Bingo(ConfigurationSection configuration) {
        this.type = BingoType.parse(configuration.getString("type"));
        this.id = configuration.getString("id");
        this.name = configuration.getString("name");
        this.checkOriTrainer = configuration.getBoolean("checkOriTrainer");
        this.uploadType = UploadType.parse(configuration.getString("uploadType"));
        this.tag = configuration.getString("tag");
        this.rewards = new ArrayList<>();
        ConfigurationSection section = configuration.getConfigurationSection("rewards");
        for (String key : section.getKeys(false)) {
            rewards.add(new BingoReward(this, section.getConfigurationSection(key)));
        }
        this.file = FileUtil.getFile(DuckPokemonBingo.instance, "data/bingo/" + id + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public abstract List<BingoPokemon> generatePokemon();

    public enum BingoType {
        COMMON(CommonBingo.class),
        POOL(PoolBingo.class);
        public final Class<? extends Bingo> bingoClass;

        BingoType(Class<? extends Bingo> bingoClass) {
            this.bingoClass = bingoClass;
        }

        public static BingoType parse(String type) {
            for (BingoType value : values()) {
                if (value.name().equalsIgnoreCase(type)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown BingoType: " + type);
        }
    }

    public enum UploadType {
        REMOVE((player, slot, pokemon, bingoPokemon, bingo) -> {
            Object party = DuckPokemonBingo.getVersionController().getParty(player);
            DuckPokemonBingo.getVersionController().partySet(party, slot, null);
            return true;
        }),
        ADD_TAG((player, slot, pokemon, bingoPokemon, bingo) -> {
            if (bingo.tag != null) {
                String[] split = bingo.tag.split(";");
                for (String s : split) {
                    DuckPokemonBingo.getVersionController().addFlag(pokemon, s);
                }
            }

            return true;
        }),
        NOTHING((player, slot, pokemon, bingoPokemon, bingo) -> true);
        public final Upload upload;

        UploadType(Upload upload) {
            this.upload = upload;
        }

        public static UploadType parse(String type) {
            for (UploadType value : values()) {
                if (value.name().equalsIgnoreCase(type)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown UploadType: " + type);
        }
    }

    public interface Upload {
        boolean upload(Player player, int slot, Pokemon pokemon, BingoPokemon bingoPokemon, Bingo bingo);
    }
}
