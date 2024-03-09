package me.hiyama283.townypotvault.config;

import org.bukkit.inventory.ItemStack;

public class PotData {
    private final ItemStack stack;
    private Integer count;
    public PotData(ItemStack stack, Integer count) {
        this.stack = stack;
        this.count = count;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public ItemStack getStack() {
        return stack;
    }
}
