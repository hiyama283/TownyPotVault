package me.hiyama283.townypotvault.config;

import com.palmergames.bukkit.towny.TownyAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class PotVaultRootConfig {
    private final HashMap<UUID, VaultDataConfig> townDataConfigHashMap = new HashMap<>();
    private final HashMap<UUID, VaultDataConfig> nationDataConfigHashMap = new HashMap<>();
    private final JavaPlugin plugin;

    public PotVaultRootConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();

        ConfigurationSection townsConfig = config.getConfigurationSection("towns");
        if (townsConfig != null) {
            for (String towns : townsConfig.getKeys(false)) {
                UUID uuid = UUID.fromString(towns);
                if (TownyAPI.getInstance().getTown(uuid) == null) continue;
                townDataConfigHashMap.put(uuid, new VaultDataConfig(plugin, "towns." + towns));
            }
        }

        ConfigurationSection nationsConfig = config.getConfigurationSection("nations");
        if (nationsConfig != null) {
            for (String nations : nationsConfig.getKeys(false)) {
                UUID key = UUID.fromString(nations);
                if (TownyAPI.getInstance().getNation(key) == null) continue;
                nationDataConfigHashMap.put(key, new VaultDataConfig(plugin, "nations." + nations));
            }
        }
    }
    public void save() {
        // Clearing everything.
        for(String key : plugin.getConfig().getKeys(false)){
            plugin.getConfig().set(key,null);
        }

        townDataConfigHashMap.forEach(((uuid, vaultDataConfig) -> vaultDataConfig.save()));
        nationDataConfigHashMap.forEach(((uuid, vaultDataConfig) -> vaultDataConfig.save()));
    }

    public HashMap<UUID, VaultDataConfig> getNationDataConfigHashMap() {
        return nationDataConfigHashMap;
    }

    public HashMap<UUID, VaultDataConfig> getTownDataConfigHashMap() {
        return townDataConfigHashMap;
    }
}
