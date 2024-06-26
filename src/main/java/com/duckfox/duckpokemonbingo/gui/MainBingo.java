package com.duckfox.duckpokemonbingo.gui;

import com.duckfox.duckapi.utils.NBTUtil;
import com.duckfox.duckapi.utils.PokemonUtil;
import com.duckfox.duckapi.utils.StringUtil;
import com.duckfox.duckpokemonbingo.DuckPokemonBingo;
import com.duckfox.duckpokemonbingo.api.bingo.Bingo;
import com.duckfox.duckpokemonbingo.api.bingo.BingoPokemon;
import com.duckfox.duckpokemonbingo.api.bingo.BingoReward;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainBingo implements InventoryHolder, Listener {
    public static final MainBingo INSTANCE = new MainBingo();
    public static final Map<Player, String> playerInventory = Collections.synchronizedMap(new HashMap<Player, String>());

    @Override
    public Inventory getInventory() {
        return null;
    }

    public static void openBingo(Player player, Bingo bingo) {
        Inventory inventory = Bukkit.createInventory(INSTANCE, 54, StringUtil.format(bingo.name, player));
        refreshBingoPokemon(inventory, player, bingo);
        refreshRewards(inventory, player, bingo);

        {
            Material material = Material.matchMaterial(DuckPokemonBingo.getConfigManager().getString("icons.glassPane.material", player));
            ItemStack itemStack = new ItemStack(material != null ? material : Material.STAINED_GLASS_PANE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            String name = DuckPokemonBingo.getConfigManager().getString("icons.glassPane.name", player);
            itemMeta.setDisplayName(name);
            List<String> lore = DuckPokemonBingo.getConfigManager().getStringList("icons.glassPane.lore", player);
            itemMeta.setLore(lore != null ? lore : Collections.emptyList());
            itemStack.setItemMeta(itemMeta);
            setItemStack(inventory, itemStack, 6, 15, 24, 33, 42, 51, 8, 17, 26, 35, 44, 53);
        }

        refreshPartyPokemon(inventory, player);
        refreshInGuiInfo(inventory, "", InfoType.INFO);
        player.openInventory(inventory);
        playerInventory.put(player, bingo.id);
    }

    private static void refreshInGuiInfo(Inventory inventory, String text, InfoType type) {
        if (DuckPokemonBingo.getConfigManager().getBoolean("inGuiInfo")) {
            ItemStack textInfo = PokemonUtil.getTextInfo(1000, 1000, type.color.getRed(), type.color.getGreen(), type.color.getBlue(), type.color.getAlpha(), text, 0D, 0D, 20D, -200, 20);
            inventory.setItem(53, textInfo);
        }
    }

    @EventHandler
    public void onClicked(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory != null && inventory.getHolder() instanceof MainBingo) {
            ItemStack clicked = event.getCurrentItem();
            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true);
            ItemStack cursor = event.getCursor();
            PlayerPartyStorage party = PokemonUtil.getParty(player);
            //先进行鼠标判断 如果鼠标上的是队伍宝可梦
            if (cursor != null && NBTUtil.hasKey(cursor, "partySlot")) {
                player.setItemOnCursor(null);
                // 读取slot
                int slot = NBTUtil.readIntegerNBT(cursor, "partySlot");
                // 如果点击的是冰菓宝可梦
                if (clicked.getType() != Material.AIR && NBTUtil.hasKey(clicked, "bingoId")) {
                    String bingoId = NBTUtil.readStringNBT(clicked, "bingoId");
                    int bingoPokemonId = NBTUtil.readIntegerNBT(clicked, "bingoPokemonId");
                    Bingo bingo = DuckPokemonBingo.bingoManager.getBingo(bingoId);
                    if (bingo != null) {
                        Pokemon pokemon = party.get(slot);
                        if (pokemon != null) {
                            BingoPokemon bingoPokemon = bingo.getPokemons(player).get(bingoPokemonId - 1);
                            if (bingoPokemon.status == BingoPokemon.BingoStatus.COMPLETED) {
                                sendMsg(inventory, InfoType.ERROR, player, "upload.alreadyUploaded", "%pokemon%", bingoPokemon.species.getLocalizedName());
                            } else {
                                if (bingoPokemon.species == pokemon.getSpecies()) {
                                    if (!bingo.checkOriTrainer || player.getUniqueId().equals(pokemon.getOriginalTrainerUUID())) {
                                        if (pokemon.hasSpecFlag(DuckPokemonBingo.getConfigManager().getString("tag.cannotUploadTag"))) {
                                            DuckPokemonBingo.getMessageManager().sendMessage(player, "upload.cannotUploadBecauseUploaded", "%pokemon%", pokemon.getDisplayName());
                                        } else {
                                            boolean result = bingo.uploadType.upload.upload(player, slot, pokemon, bingoPokemon, bingo);
                                            if (result) {
                                                bingoPokemon.complete();
                                                bingo.update(player, bingoPokemon, false);
                                                sendMsg(inventory, InfoType.INFO, player, "upload.success", "%pokemon%", pokemon.getDisplayName());
                                            }
                                        }
                                        refreshBingoPokemon(inventory, player, bingo);
                                        refreshRewards(inventory, player, bingo);
                                    } else {
                                        sendMsg(inventory, InfoType.ERROR, player, "upload.oriTrainerMismatch", "%pokemon%", pokemon.getDisplayName());
                                    }
                                } else {
                                    sendMsg(inventory, InfoType.ERROR, player, "upload.speciesMismatch", "%bingoPokemon%", bingoPokemon.species.getLocalizedName(), "%pokemon%", pokemon.getLocalizedName());
                                }
                            }
                        }
                    }
                }
                // 重新生成宝可梦

                refreshPartyPokemon(inventory, player);
                return;
            }
            //进行判断点击的是否为队伍宝可梦
            if (NBTUtil.hasKey(clicked, "partySlot")) {
                // 如果是则设置可拾取，放到鼠标上
                int slot = NBTUtil.readIntegerNBT(clicked, "partySlot");
                if (party.get(slot) != null) {
                    event.setCancelled(false);
                    refreshInGuiInfo(inventory, DuckPokemonBingo.getMessageManager().getString("inGuiText.selectParty", player, "%pokemon%", party.get(slot).getDisplayName()), InfoType.INFO);
                }
            }
            if (NBTUtil.hasKey(clicked, "bingoReward")) {
                String rewardId = NBTUtil.readStringNBT(clicked, "bingoReward");
                String bingoId = NBTUtil.readStringNBT(clicked, "bingoId");
                Bingo bingo = DuckPokemonBingo.bingoManager.getBingo(bingoId);
                if (bingo != null) {
                    BingoReward reward = bingo.getReward(rewardId);
                    reward.claim(player);
                    refreshRewards(inventory, player, bingo);
                }
            }
        }
        if ((event.getSlot() < 0 || event.getWhoClicked().getInventory().equals(event.getClickedInventory())) && event.getCursor() != null && NBTUtil.hasKey(event.getCursor(), "partySlot")) {
            event.setCancelled(true);
        }
    }

    public static void sendMsg(Inventory inventory, InfoType infoType, Player player, String key, String... args) {
        String msg = DuckPokemonBingo.getMessageManager().getString(key, player, args);
        player.sendMessage(msg);
        refreshInGuiInfo(inventory, msg, infoType);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory != null && inventory.getHolder() instanceof MainBingo) {
            event.getPlayer().setItemOnCursor(null);
            playerInventory.remove((Player) event.getPlayer());
        }
    }

    private static void refreshRewards(Inventory inventory, Player player, Bingo bingo) {
        for (BingoReward reward : bingo.rewards) {
            reward.updateStatus(player);
            ItemStack itemStack = new ItemStack(Material.CHEST);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(StringUtil.format(reward.name, player, "%status%", reward.getStatusInfo()));
            itemMeta.setLore(StringUtil.format(reward.description, player));
            itemStack.setItemMeta(itemMeta);
            itemStack = NBTUtil.writeStringNBT(itemStack, "bingoReward", reward.id);
            itemStack = NBTUtil.writeStringNBT(itemStack, "bingoId", bingo.id);
            inventory.setItem(reward.slot, itemStack);
        }
    }

    private static void refreshBingoPokemon(Inventory inventory, Player player, Bingo bingo) {
        List<BingoPokemon> list;
        if (bingo.isPlayerGenerated(player)) {
            list = bingo.getPokemons(player);
        } else {
            list = bingo.generatePokemon();
            bingo.updateAll(player, list);
        }
        for (BingoPokemon bingoPokemon : list) {
            Pokemon pokemon = Pixelmon.pokemonFactory.create(bingoPokemon.species);
            pokemon.setForm(0);
            pokemon.setShiny(false);
            ItemStack itemStack = PokemonUtil.getPhoto(pokemon);
            ItemMeta itemMeta = itemStack.getItemMeta();
            String name = DuckPokemonBingo.getConfigManager().getString("icons.bingoPokemon.name", player,
                    "%pokemonName%", pokemon.getLocalizedName(),
                    "%status%", bingoPokemon.status.getLocalizedName(), "%id%", String.valueOf(bingoPokemon.id));
            itemMeta.setDisplayName(name);
            List<String> lore = DuckPokemonBingo.getConfigManager().getStringList("icons.bingoPokemon.lore", player,
                    "%pokemonName%", pokemon.getLocalizedName(),
                    "%status%", bingoPokemon.status.getLocalizedName(), "%id%", String.valueOf(bingoPokemon.id));
            itemMeta.setLore(lore);
            if (bingoPokemon.status == BingoPokemon.BingoStatus.COMPLETED) {
                itemMeta.addEnchant(Enchantment.DAMAGE_ALL, 5, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            itemStack.setItemMeta(itemMeta);
            itemStack = NBTUtil.writeIntegerNBT(itemStack, "bingoPokemonId", bingoPokemon.id);
            itemStack = NBTUtil.writeStringNBT(itemStack, "bingoId", bingo.id);
            inventory.setItem(getSlot(bingoPokemon.id), itemStack);
        }
    }

    private static void refreshPartyPokemon(Inventory inventory, Player player) {
        PlayerPartyStorage party = PokemonUtil.getParty(player);
        Pokemon[] all = party.getAll();
        for (int i = 0; i < all.length; i++) {
            Pokemon pokemon = all[i];
            ItemStack itemStack;
            if (pokemon != null) {
                itemStack = PokemonUtil.getPhoto(pokemon);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(DuckPokemonBingo.getConfigManager().getString("icons.partyPokemon.name", player, "%pokemonName%", pokemon.getDisplayName()));
                itemStack.setItemMeta(itemMeta);
                itemStack = NBTUtil.writeIntegerNBT(itemStack, "partySlot", i);
            } else {
                Material material = Material.matchMaterial(DuckPokemonBingo.getConfigManager().getString("icons.noPokemon.material", player));
                itemStack = new ItemStack(material != null ? material : Material.BARRIER);
                ItemMeta itemMeta = itemStack.getItemMeta();
                String name = DuckPokemonBingo.getConfigManager().getString("icons.noPokemon.name", player);
                itemMeta.setDisplayName(name);
                List<String> lore = DuckPokemonBingo.getConfigManager().getStringList("icons.noPokemon.lore", player);
                itemMeta.setLore(lore != null ? lore : Collections.emptyList());
                itemStack.setItemMeta(itemMeta);
            }
            inventory.setItem(i * 9 + 7, itemStack);
        }
    }

    private static void setItemStack(Inventory inventory, ItemStack itemStack, int... slot) {
        for (int j : slot) {
            inventory.setItem(j, itemStack);
        }
    }

    private static int getSlot(int id) {
        return id + 4 * (((id - 1) / 5 - 1) + 1);
    }

    public enum InfoType {
        ERROR(Color.red),
        INFO(Color.green);
        public final Color color;

        InfoType(Color color) {
            this.color = color;
        }
    }

}
