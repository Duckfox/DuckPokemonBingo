package com.duckfox.duckpokemonbingo.api.bingo;

import com.duckfox.duckapi.nms.Version;
import com.duckfox.duckapi.utils.RandomUtil;
import com.duckfox.duckpokemonbingo.DuckPokemonBingo;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PoolBingo extends Bingo {
    public List<Object> pool;

    public PoolBingo(ConfigurationSection configuration) {
        super(configuration);
        List<String> list = configuration.getStringList("pool");
        this.pool = new ArrayList<>();
        for (String s : list) {
            Object species = DuckPokemonBingo.getVersionController().getSpeciesFromNameAnyCase(s);
            if (species != null) {
                this.pool.add(species);
            } else {
                if (Version.getVersion().isVersion(Version.V1_12_2)) {
                    throw new IllegalArgumentException("Invalid pool species: " + s);
                } else {
                    DuckPokemonBingo.instance.getLogger().severe("Invalid pool species: " + s + "! If you are using Minecraft Server 1.16.5,You cannot use Translated Name as Pool Species.");
                    System.out.println(pool);
                }
            }
        }
    }

    @Override
    public List<BingoPokemon> generatePokemon() {
        List<BingoPokemon> list = new ArrayList<>();
        int id = 1;
        while (list.size() < 25) {
            list.add(new BingoPokemon(id++, RandomUtil.getRandomElement(this.pool), BingoPokemon.BingoStatus.PROGRESSING, this));
        }
        return list;
    }
}
