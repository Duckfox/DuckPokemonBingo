package com.duckfox.duckpokemonbingo.api.bingo;

import com.duckfox.duckapi.utils.RandomUtil;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PoolBingo extends Bingo{
    public List<EnumSpecies> pool;
    public PoolBingo(ConfigurationSection configuration) {
        super(configuration);
        List<String> list = configuration.getStringList("pool");
        this.pool = new ArrayList<>();
        for (String s : list) {
            EnumSpecies enumSpecies = EnumSpecies.getFromNameAnyCase(s);
            if (enumSpecies!=null){
                this.pool.add(enumSpecies);
            }else {
                throw new IllegalArgumentException("Invalid pool species: " + s);
            }
        }
    }

    @Override
    public List<BingoPokemon> generatePokemon() {
        List<BingoPokemon> list = new ArrayList<>();
        int id = 1;
        while (list.size() < 25){
            list.add(new BingoPokemon(id++,RandomUtil.getRandomElement(this.pool), BingoPokemon.BingoStatus.PROGRESSING,this));
        }
        return list;
    }
}
