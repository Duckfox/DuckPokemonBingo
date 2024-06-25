package com.duckfox.duckpokemonbingo.api.bingo;

import com.duckfox.duckapi.utils.StringUtil;
import com.duckfox.duckpokemonbingo.DuckPokemonBingo;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class BingoReward {
    public String id;
    public String name;
    public List<String> description;
    public List<Integer> conditions;
    public List<String> rewards;
    public Bingo bingo;
    public RewardStatus status;
    public int progress;
    public int slot;

    public BingoReward(Bingo bingo, ConfigurationSection section) {
        this.bingo = bingo;
        this.id = section.getString("id");
        this.name = section.getString("name");
        this.description = section.getStringList("description");
        this.conditions = section.getIntegerList("conditions");
        this.rewards = section.getStringList("rewards");
        this.slot = section.getInt("slot");
    }

    public void updateStatus(Player player) {
        List<BingoPokemon> list = bingo.getPokemons(player);
        int count = 0;
        for (Integer condition : conditions) {
            if (list.get(condition - 1).status == BingoPokemon.BingoStatus.PROGRESSING) {
                this.status = RewardStatus.PROGRESSING;
            } else {
                count++;
            }
        }
        this.progress = count;
        if (count == conditions.size()) {
            this.status = RewardStatus.CAN_CLAIM;
        }
        if (bingo.isPlayerClaimedReward(this, player)) {
            this.status = RewardStatus.COMPLETED;
        }
    }

    public String getStatusInfo() {
        return StringUtil.format(this.status.getLocalizedName(), "%progress%", this.progress + "/" + this.conditions.size());
    }

    public void claim(Player player) {
        if (bingo.isPlayerClaimedReward(this, player)) {
            DuckPokemonBingo.getMessageManager().sendMessage(player, "rewards.alreadyClaimed");
            return;
        }
        if (canClaim(player)) {
            for (String reward : rewards) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), StringUtil.format(reward, player));
            }
            bingo.setClaimedReward(this, player);
            DuckPokemonBingo.getMessageManager().sendMessage(player, "rewards.claim");
        } else {
            DuckPokemonBingo.getMessageManager().sendMessage(player, "rewards.cannotClaim");
        }
    }

    public boolean canClaim(Player player) {
        if (bingo.isPlayerClaimedReward(this, player)) {
            return false;
        }
        List<BingoPokemon> list = bingo.getPokemons(player);
        for (Integer condition : conditions) {
            if (list.get(condition - 1).status == BingoPokemon.BingoStatus.PROGRESSING) {
                return false;
            }
        }
        return true;
    }

    public enum RewardStatus {
        PROGRESSING,
        COMPLETED,
        CAN_CLAIM;

        RewardStatus() {
        }

        public String getLocalizedName() {
            return DuckPokemonBingo.getConfigManager().getString("info.reward.status." + this.name().toLowerCase());
        }

    }
}
