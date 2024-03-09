package me.hiyama283.townypotvault;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import me.hiyama283.townypotvault.config.PotData;
import me.hiyama283.townypotvault.config.VaultDataConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ipvp.canvas.slot.ClickOptions;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.type.ChestMenu;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PotVaultCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (command.getName().equalsIgnoreCase("potvault") && commandSender instanceof Player player) {
            Town town;
            try {
                town = TownyAPI.getInstance().getResident(player.getUniqueId()).getTown();
            } catch (NotRegisteredException e) {
                player.sendMessage("Oh fuck NotRegisteredException");
                return false;
            }
            if (town != null && town.equals(TownyAPI.getInstance().getTown(player.getLocation()))) {
                UUID uuid = town.getUUID();
                createMainMenu(uuid).open(player);
            } else {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You cannot run command due you are not in town!"));
            }
            return true;
        }
        return false;
    }

    private ItemStack getSlotStack(String name, String loreText, Material material) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(name.isEmpty() ? MiniMessage.miniMessage().deserialize("<yellow></yellow>") : MiniMessage.miniMessage().deserialize(name));
        List<Component> lore = new ArrayList<>();
        lore.add(loreText.isEmpty() ? MiniMessage.miniMessage().deserialize("<yellow></yellow>") : MiniMessage.miniMessage().deserialize(loreText));
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private ChestMenu createMainMenu(UUID uuid) {
        ChestMenu menu = ChestMenu.builder(5)
                .title("Pot vault Menu")
                .redraw(true)
                .build();

        for (Slot slot : menu.getSlots()) {
            slot.setItem(getSlotStack("", "", Material.GRAY_STAINED_GLASS_PANE));
            slot.setClickOptions(ClickOptions.DENY_ALL);
        }

        Slot townVaultSlot = menu.getSlot(21);
        townVaultSlot.setItem(getSlotStack("Town vault", "Open town vault.", Material.CHEST));
        townVaultSlot.setClickHandler((player, clickInformation) -> {
            createVaultMenu(
                    uuid, TownyPotVault.vaultRootConfig.getTownDataConfigHashMap().get(uuid), true
            ).open(player);
        });

        Slot nationVaultSlot = menu.getSlot(23);
        nationVaultSlot.setItem(getSlotStack("Nation vault", "Open town vault.", Material.CHEST));
        nationVaultSlot.setClickHandler((player, clickInformation) -> {
            if (TownyAPI.getInstance().getNation(player) == null) {
                player.sendMessage(deserialize("<red>Your town does not have nation!"));
                return;
            }
            createVaultMenu(
                    uuid, TownyPotVault.vaultRootConfig.getNationDataConfigHashMap().get(uuid), false
            ).open(player);
        });

        return menu;
    }

    public static ChestMenu createVaultMenu(UUID uuid, VaultDataConfig config, boolean isTown) {
        ChestMenu menu = ChestMenu.builder(5)
                .title("Pot vault Menu")
                .redraw(true)
                .build();

        if (config == null) {
            VaultDataConfig value = new VaultDataConfig(TownyPotVault.INSTANCE, (isTown ? "towns." : "nations.") + uuid.toString());
            (isTown ? TownyPotVault.vaultRootConfig.getTownDataConfigHashMap() : TownyPotVault.vaultRootConfig.getNationDataConfigHashMap())
                    .put(uuid, value);
            return createVaultMenu(uuid, value, isTown);
        }

        for (Slot slot : menu.getSlots()) {
            slot.setClickOptions(ClickOptions.DENY_ALL);
        }

        synchronized (config.getPotDataList()) {
            int i = 0;
            for (PotData potData : config.getPotDataList()) {
                Slot slot = menu.getSlot(i);

                slot.setClickHandler((player, clickInformation) -> {
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

                i++;
            }
        }

        menu.setCloseHandler((player, menu1) -> {
            synchronized (config.getPotDataList()) {
                for (int i = 0; i < config.getPotDataList().size(); i++) {
                    PotData potData = config.getPotDataList().get(i);
                    if (potData.getCount() < 1) {
                        config.getPotDataList().remove(potData);
                        i--;
                    }
                }
            }

            synchronized (TownyPotVault.menu) {
                TownyPotVault.menu.remove(menu1);
            }
        });

        synchronized (TownyPotVault.menu) {
            TownyPotVault.menu.put(menu, isTown);
        }
        return menu;
    }

    private static Component deserialize(String string) {
        return MiniMessage.miniMessage().deserialize(string);
    }
}
