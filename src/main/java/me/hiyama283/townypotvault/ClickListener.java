package me.hiyama283.townypotvault;

import com.palmergames.bukkit.towny.TownyAPI;
import me.hiyama283.townypotvault.config.PotData;
import me.hiyama283.townypotvault.config.VaultDataConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.type.MenuHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ClickListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            for (Menu menu : TownyPotVault.menu.keySet()) {
                if (menu.isOpen(player) && event.getView().getTopInventory().getHolder() instanceof MenuHolder &&
                        event.getClickedInventory() != event.getView().getTopInventory()
                ) {
                    Boolean isTown = TownyPotVault.menu.get(menu);
                    HashMap<UUID, VaultDataConfig> vaultDataConfig = isTown ? TownyPotVault.vaultRootConfig.getTownDataConfigHashMap() : TownyPotVault.vaultRootConfig.getNationDataConfigHashMap();
                    VaultDataConfig vaultDataConfig1 = vaultDataConfig.get(TownyAPI.getInstance().getTown(player).getUUID());
                    if (vaultDataConfig1 == null) return;

                    ItemStack item = event.getCurrentItem();
                    if (item == null || item.getType() == Material.AIR) continue;

                    Material slotType = item.getType();
                    if (slotType == Material.POTION || slotType == Material.SPLASH_POTION || slotType == Material.LINGERING_POTION) {
                        if (item.getItemMeta() instanceof PotionMeta potMeta) {
                            boolean b = false;
                            for (PotData potData : vaultDataConfig1.getPotDataList()) {
                                if (potData.getStack().getType() == item.getType() && potData.getStack().getItemMeta() instanceof PotionMeta potionMeta) {
                                    if (potionMeta.getBasePotionData().getType() == potMeta.getBasePotionData().getType() ||
                                            !potionMeta.getCustomEffects().isEmpty() &&
                                            potionMeta.getCustomEffects().get(0) == potMeta.getCustomEffects().get(0)
                                    ) {
                                        if (potionMeta.getBasePotionData().isUpgraded() == potMeta.getBasePotionData().isUpgraded() &&
                                            potionMeta.getBasePotionData().isExtended() == potMeta.getBasePotionData().isExtended()
                                        ) {
                                            potData.setCount(potData.getCount() + 1);
                                            b = true;
                                        }
                                    }
                                }
                            }

                            if (!b) {
                                PotData potData = new PotData(item, 1);
                                vaultDataConfig1.getPotDataList().add(potData);
                                for (Slot slot : menu.getSlots()) {
                                    if (slot.getItem(player) == null || slot.getItem(player).getType() == Material.AIR) {
                                        slot.setClickHandler((player1, clickInformation) -> {
                                            if (potData.getCount() < 1) {
                                                player.sendMessage(deserialize("<red>Pots are run out!"));
                                                return;
                                            }

                                            if (clickInformation.getAction() == InventoryAction.PICKUP_ALL ||
                                                    clickInformation.getAction() == InventoryAction.PICKUP_SOME ||
                                                    clickInformation.getAction() == InventoryAction.PICKUP_HALF ||
                                                    clickInformation.getAction() == InventoryAction.PICKUP_ONE
                                            ) {
                                                if (player.getInventory().firstEmpty() != -1) {
                                                    player.getInventory().addItem(potData.getStack());
                                                } else {
                                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 50, 12);
                                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Your inventory is Full!"));
                                                    return;
                                                }
                                                potData.setCount(potData.getCount() - 1);
                                            }

                                            if (clickInformation.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                                                for (;;) {
                                                    if (player.getInventory().firstEmpty() == -1) break;
                                                    if (potData.getCount() < 1) {
                                                        player.sendMessage(deserialize("<red>Pots are run out!"));
                                                        break;
                                                    }

                                                    player.getInventory().addItem(potData.getStack());
                                                    potData.setCount(potData.getCount() - 1);
                                                }
                                            }

                                            menu.update();
                                        });

                                        slot.setItemTemplate(p -> {
                                            ItemStack stack = potData.getStack().clone();
                                            ItemMeta itemMeta = stack.getItemMeta();
                                            List<Component> lores = new ArrayList<>();
                                            lores.add(Component.empty());
                                            lores.add(deserialize("<white>Count:</white> <rainbow>" + potData.getCount()));
                                            lores.add(deserialize("<white>Click to withdraw 1 pot"));
                                            lores.add(deserialize("<white>Click to withdraw as much you possible"));
                                            itemMeta.lore(lores);
                                            stack.setItemMeta(itemMeta);

                                            return stack;
                                        });
                                        break;
                                    }
                                }
                            }

                            event.getClickedInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
                        }
                    }
                    event.setCancelled(true);

                    menu.update();
                    break;
                }
            }
        }
    }

    private static Component deserialize(String string) {
        return MiniMessage.miniMessage().deserialize(string);
    }
}
