package com.duckfox.duckpokemonbingo.api.bingo;

import com.duckfox.duckapi.utils.RandomUtil;
import com.duckfox.duckpokemonbingo.DuckPokemonBingo;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class CommonBingo extends Bingo {
    public int minNdex;
    public int maxNdex;
    public boolean canLegendary;
    public boolean canUltraBeast;
    public List<String> blackList;

    public CommonBingo(ConfigurationSection configuration) {
        super(configuration);
        this.minNdex = configuration.getInt("minNdex");
        this.maxNdex = configuration.getInt("maxNdex");
        this.canLegendary = configuration.getBoolean("canLegendary");
        this.canUltraBeast = configuration.getBoolean("canUltraB");
        this.blackList = configuration.getStringList("blackList");
    }
    public List<BingoPokemon> generatePokemon() {
        List<BingoPokemon> list = new ArrayList<>();
        int id = 1;
        while (list.size() < 25){
            Object species;
            while (true){
                int randInt = RandomUtil.getRandomIntInclusive(minNdex,maxNdex);
                Object fromDex = DuckPokemonBingo.getVersionController().getSpeciesFromDex(randInt);
                if (DuckPokemonBingo.getVersionController().isSpeciesLegendary(fromDex) && !canLegendary){
                    continue;
                }
                if (DuckPokemonBingo.getVersionController().isSpeciesUltraBeast(fromDex) && !canUltraBeast){
                    continue;
                }
                if (blackList.contains(DuckPokemonBingo.getVersionController().getSpeciesPokemonName(fromDex)) || blackList.contains(DuckPokemonBingo.getVersionController().getSpeciesLocalizedName(fromDex))){
                    continue;
                }
                species = fromDex;
                break;
            }
            list.add(new BingoPokemon(id++,species, BingoPokemon.BingoStatus.PROGRESSING,this));
        }
        return list;
    }
}
