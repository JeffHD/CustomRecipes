package com.gmail.jannyboy11.customrecipes.api.furnace;

import java.util.Iterator;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

/**
 * Represents the furnace recipe manager, the almighty manager of all furnaces.
 * 
 * @author Jan
 */
public interface FurnaceManager extends Iterable<FurnaceRecipe> {
	
	/**
	 * Add a furnace recipe. Custom recipes take higher priority than vanilla recipes,
	 * meaning that furnaces will try the use the custom recipes first.
	 * 
	 * @param furnaceRecipe the furnace recipe to be added.
	 * @return the registered furnace recipe - a live object
	 */
	public FurnaceRecipe addCustomRecipe(FurnaceRecipe furnaceRecipe);
	
	/**
	 * Add a furnace recipe. Vanilla recipes take lower priority than custom recipes,
	 * meaning that furnaces will try the use the custom recipes first.
	 * 
	 * @param furnaceRecipe the furnace recipe to be added.
	 * @return the registered furnace recipe - a live object
	 */
	public FurnaceRecipe addVanillaRecipe(FurnaceRecipe furnaceRecipe);

	/**
	 * Iterate over all registered custom recipes
	 * 
	 * @return the iterator
	 */
	public Iterator<? extends FurnaceRecipe> customIterator();

	/**
	 * Iterate over all registered vanilla recipes
	 * 
	 * @return the iterator
	 */
	public Iterator<? extends FurnaceRecipe> vanillaIterator();

	/**
	 * Get a recipe for this ingredient
	 * 
	 * @return the registered recipe, or null if none of the recipes matched the ingredient
	 */
	public FurnaceRecipe getRecipe(ItemStack ingredient);
	
	/**
	 * Get a custom recipe for this ingredient
	 * 
	 * @return the registered recipe, or null if none of the recipes matched the ingredient
	 */
	public FurnaceRecipe getCustomRecipe(ItemStack ingredient);

	/**
	 * Get a vanilla recipe for this ingredient
	 * 
	 * @return the registered recipe, or null if none of the recipes matched the ingredient
	 */
	public FurnaceRecipe getVanillaRecipe(ItemStack ingredient);

	/**
	 * Reset all furnace recipes, only leaving the default vanilla ones.
	 */
	public void reset();

	/**
	 * Delete all vanilla furnace recipes.
	 */
	public void clearVanilla();
	
	/**
	 * Delete all custom furnace recipes.
	 */
	public void clearCustom();
	
	/**
	 * Remove a furnace recipe by its ingredient
	 * 
	 * @param ingredient the ingredient
	 * @return the recipe that was removed, or null if no recipe matched the ingredient
	 */
	public FurnaceRecipe removeRecipe(ItemStack ingredient);
	
	/**
	 * Remove a vanilla furnace recipe by its ingredient
	 * 
	 * @param ingredient the ingredient
	 * @return the recipe that was removed, or null if no recipe matched the ingredient
	 */
	public FurnaceRecipe removeVanillaRecipe(ItemStack ingredient);
	
	/**
	 * Remove a custom furnace recipe by its ingredient
	 * 
	 * @param ingredient the ingredient
	 * @return the recipe that was removed, or null if no recipe matched the ingredient
	 */
	public FurnaceRecipe removeCustomRecipe(ItemStack ingredient);

	/**
	 * Removes a furnace recipe by its key.
	 * 
	 * @param key the key of the furnace recipe
	 * @return the removed recipe, or null if no recipe was removed
	 */
    public FurnaceRecipe removeRecipe(NamespacedKey key);

    /**
     * Removes a vanilla furnace recipe by its key.
     * 
     * @param key the key of the furnace recipe
     * @return the removed recipe, or null if no recipe was removed
     */
    public FurnaceRecipe removeVanillaRecipe(NamespacedKey key);

    /**
     * Removes a custom furnace recipe by its key.
     * 
     * @param key the key of the furnace recipe
     * @return the removed recipe, or null if not recipe was removed
     */
    public FurnaceRecipe removeCustomRecipe(NamespacedKey key);

    /**
     * Gets a furnace recipe by its key.
     * 
     * @param key the key of the furnace recipe
     * @return the registered recipe, or null if no recipe with that key was registered
     */
    public FurnaceRecipe getRecipe(NamespacedKey key);

    /**
     * Gets a furnace custom recipe by its key.
     * 
     * @param key the key of the furnace recipe
     * @return the registered recipe, or null if no recipe with that key was registered
     */
    public FurnaceRecipe getCustomRecipe(NamespacedKey key);

    /**
     * Gets a furnace vanilla recipe by its key.
     * 
     * @param key the key of the furnace recipe
     * @return the registered recipe, or null if no recipe with that key was registered
     */
    public FurnaceRecipe getVanillaRecipe(NamespacedKey key);
}
