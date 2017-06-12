package com.gmail.jannyboy11.customrecipes.api.crafting.vanilla.recipe;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Represents the recipe responsible for extending Maps.
 * @author Jan
 *
 */
public interface MapExtendRecipe extends ShapedRecipe {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public default ItemStack getRepresentation() {
		ItemStack stack = new ItemStack(Material.MAP);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatColor.GRAY + "Map Extend");
		meta.setLore(Arrays.asList(ChatColor.DARK_GRAY + "Key: " + getKey()));
		stack.setItemMeta(meta);
		return stack;
	}

}