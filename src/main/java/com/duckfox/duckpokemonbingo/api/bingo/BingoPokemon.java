package com.duckfox.duckpokemonbingo.api.bingo;

import com.duckfox.duckpokemonbingo.DuckPokemonBingo;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.io.Serializable;

public class BingoPokemon implements Serializable{
    public int id;
    public EnumSpecies species;
    public BingoStatus status;
    public Bingo parent;
    public BingoPokemon(int id,EnumSpecies species, BingoStatus status, Bingo parent){
        this.id = id;
        this.species = species;
        this.status = status;
        this.parent = parent;
    }
    public BingoPokemon(ConfigurationSection section){
        this.id = section.getInt("id");
        this.species = EnumSpecies.getFromDex(section.getInt("species"));
        this.status = BingoStatus.values()[(section.getInt("status"))];
        this.parent = DuckPokemonBingo.bingoManager.getBingo(section.getString("parent"));
    }
    public void complete(){
        this.status = BingoStatus.COMPLETED;
    }
    public void progressing(){
        this.status = BingoStatus.PROGRESSING;
    }
    public void save(ConfigurationSection config){
        config.set("id", this.id);
        config.set("species", this.species.getNationalPokedexInteger());
        config.set("status", this.status.ordinal());
        config.set("parent", this.parent.id);
    }


    public enum BingoStatus {
        PROGRESSING,
        COMPLETED;
        BingoStatus(){}
        public String getLocalizedName(){
            return DuckPokemonBingo.getConfigManager().getString("info.bingo.status." + this.name().toLowerCase());
        }
    }
}
