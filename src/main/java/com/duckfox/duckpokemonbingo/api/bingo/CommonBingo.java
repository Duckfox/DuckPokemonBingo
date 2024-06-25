package com.duckfox.duckpokemonbingo.api.bingo;

import com.duckfox.duckapi.utils.RandomUtil;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
            EnumSpecies enumspecies;
            while (true){
                int randInt = RandomUtil.getRandomIntInclusive(minNdex,maxNdex);
                EnumSpecies fromDex = EnumSpecies.getFromDex(randInt);
                if (fromDex.isLegendary() && !canLegendary){
                    continue;
                }
                if (fromDex.isUltraBeast() && !canUltraBeast){
                    continue;
                }
                if (blackList.contains(fromDex.getPokemonName()) || blackList.contains(fromDex.getLocalizedName())){
                    continue;
                }
                enumspecies = fromDex;
                break;
            }
            list.add(new BingoPokemon(id++,enumspecies, BingoPokemon.BingoStatus.PROGRESSING,this));
        }
        return list;
    }
}
