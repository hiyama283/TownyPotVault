package me.hiyama283.townypotvault.config;

import org.bukkit.Material;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.*;

import java.util.ArrayList;
import java.util.List;

public class VaultDataConfig {
    private final List<PotData> potDataList = new ArrayList<>();
    private final String path;
    private final JavaPlugin plugin;
    public VaultDataConfig(JavaPlugin plugin, String uuidPath) {
        this.path = uuidPath;
        this.plugin = plugin;

        if ( plugin.getConfig().getConfigurationSection(path) != null) {
            for (String key : plugin.getConfig().getConfigurationSection(path).getKeys(false)) {
                String s = path + "." + key;
                potDataList.add(new PotData(plugin.getConfig().getItemStack(s + ".stack"), plugin.getConfig().getInt(s + ".count")));
            }
        }
    }

    public void save() {
        for (PotData potData : potDataList) {
            String s = path + ".";
            if (potData.getStack().getItemMeta() instanceof PotionMeta potMeta) {
                PotionData basePotionData = potMeta.getBasePotionData();
                if (basePotionData.getType() == PotionType.AWKWARD || basePotionData.getType() == PotionType.WATER) {
                    PotionEffect potionEffect = potMeta.getCustomEffects().get(0);
                    s += potionEffect.getType().getName() + "_" + potionEffect.getAmplifier() + "_" + potionEffect.getDuration();
                } else {
                    s += basePotionData.getType() + "_" + basePotionData.isExtended() + "_" + basePotionData.isUpgraded();
                }

                s += "-" + potData.getStack().getType().name();

                plugin.getConfig().set(s + ".count", potData.getCount());
                plugin.getConfig().set(s + ".stack", potData.getStack());
            }
        }
    }

    public List<PotData> getPotDataList() {
        return potDataList;
    }
}
