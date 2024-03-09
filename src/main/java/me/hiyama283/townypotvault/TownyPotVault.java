package me.hiyama283.townypotvault;

import me.hiyama283.townypotvault.config.PotVaultRootConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.MenuFunctionListener;

import java.util.HashMap;

public final class TownyPotVault extends JavaPlugin {
    public static JavaPlugin INSTANCE = null;
    public static PotVaultRootConfig vaultRootConfig = null;
    // true - town/false - nation
    public static final HashMap<Menu, Boolean> menu = new HashMap<>();

    @Override
    public void onEnable() {
        INSTANCE = this;
        getLogger().info("Initializing...");

        saveDefaultConfig();
        vaultRootConfig = new PotVaultRootConfig(this);
        menu.clear();

        Bukkit.getPluginManager().registerEvents(new MenuFunctionListener(), this);
        Bukkit.getPluginManager().registerEvents(new ClickListener(), this);

        getCommand("potvault").setExecutor(new PotVaultCommandExecutor());

        String[] split = """
                ╔═╗┬  ┌─┐┬ ┬┬ ┬┌┬┐┬┌─┐
                ╠═╣│  │  ├─┤└┬┘││││├─┤
                ╩ ╩┴─┘└─┘┴ ┴ ┴ ┴ ┴┴┴ ┴
                      -Feat. Pot vault
                """.split("\n");
        for (String s : split) {
            getLogger().info(s);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        vaultRootConfig.save();
        saveConfig();
    }
}
