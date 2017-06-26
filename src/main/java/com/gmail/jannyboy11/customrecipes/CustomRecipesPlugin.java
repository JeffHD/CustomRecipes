package com.gmail.jannyboy11.customrecipes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.jannyboy11.customrecipes.api.CustomRecipesApi;
import com.gmail.jannyboy11.customrecipes.api.InventoryUtils;
import com.gmail.jannyboy11.customrecipes.api.crafting.CraftingRecipe;
import com.gmail.jannyboy11.customrecipes.api.crafting.SimpleChoiceIngredient;
import com.gmail.jannyboy11.customrecipes.api.crafting.SimpleShapedRecipe;
import com.gmail.jannyboy11.customrecipes.api.crafting.SimpleShapelessRecipe;
import com.gmail.jannyboy11.customrecipes.api.crafting.custom.recipe.NBTRecipe;
import com.gmail.jannyboy11.customrecipes.api.crafting.custom.recipe.PermissionRecipe;
import com.gmail.jannyboy11.customrecipes.api.crafting.vanilla.ingredient.ChoiceIngredient;
import com.gmail.jannyboy11.customrecipes.api.crafting.vanilla.recipe.ShapedRecipe;
import com.gmail.jannyboy11.customrecipes.api.crafting.vanilla.recipe.ShapelessRecipe;
import com.gmail.jannyboy11.customrecipes.api.furnace.FurnaceRecipe;
import com.gmail.jannyboy11.customrecipes.api.furnace.SimpleFurnaceRecipe;
import com.gmail.jannyboy11.customrecipes.commands.AddRecipeCommandExecutor;
import com.gmail.jannyboy11.customrecipes.commands.ListRecipesCommandExecutor;
import com.gmail.jannyboy11.customrecipes.commands.RemoveRecipeCommandExecutor;
import com.gmail.jannyboy11.customrecipes.gui.ListRecipesListener;
import com.gmail.jannyboy11.customrecipes.impl.crafting.CRCraftingManager;
import com.gmail.jannyboy11.customrecipes.impl.crafting.custom.addremove.NBTAdder;
import com.gmail.jannyboy11.customrecipes.impl.crafting.custom.addremove.NBTRemover;
import com.gmail.jannyboy11.customrecipes.impl.crafting.custom.addremove.PermissionAdder;
import com.gmail.jannyboy11.customrecipes.impl.crafting.custom.addremove.PermissionRemover;
import com.gmail.jannyboy11.customrecipes.impl.crafting.custom.ingredient.InjectedIngredient;
import com.gmail.jannyboy11.customrecipes.impl.crafting.vanilla.addremove.ShapedAdder;
import com.gmail.jannyboy11.customrecipes.impl.crafting.vanilla.addremove.ShapedRemover;
import com.gmail.jannyboy11.customrecipes.impl.crafting.vanilla.addremove.ShapelessAdder;
import com.gmail.jannyboy11.customrecipes.impl.crafting.vanilla.addremove.ShapelessRemover;
import com.gmail.jannyboy11.customrecipes.impl.crafting.vanilla.recipe.CRVanillaRecipe;
import com.gmail.jannyboy11.customrecipes.impl.furnace.CRFurnaceManager;
import com.gmail.jannyboy11.customrecipes.impl.furnace.CRFurnaceRecipe;
import com.gmail.jannyboy11.customrecipes.impl.furnace.addremove.FurnaceAdder;
import com.gmail.jannyboy11.customrecipes.impl.furnace.addremove.FurnaceRemover;

import net.minecraft.server.v1_12_R1.IRecipe;

public class CustomRecipesPlugin extends JavaPlugin implements CustomRecipesApi {

	private final NavigableMap<String, BiConsumer<? super Player, ? super List<String>>> adders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private final NavigableMap<String, BiConsumer<? super Player, ? super List<String>>> removers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	private final Map<String, Supplier<? extends List<? extends Recipe>>> recipeSuppliers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private final Map<String, Function<? super Recipe, ? extends ItemStack>> recipeToItemMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private final Map<String, BiConsumer<? super Recipe, ? super CommandSender>> recipeToCommandSenderDiplayMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	private CRCraftingManager craftingManager = new CRCraftingManager();
	private CRFurnaceManager furnaceManager = new CRFurnaceManager();

	@Override
	public void onLoad() {
		//define RecipeItemstackInjected subclass
		InjectedIngredient.inject();

		//adders
		addAdder("shaped", new ShapedAdder(this));
		addAdder("shapeless", new ShapelessAdder(this));
		addAdder("nbt", new NBTAdder(this));
		addAdder("permission", new PermissionAdder(this));
		addAdder("furnace", new FurnaceAdder(this));

		//removers
		addRemover("shaped", new ShapedRemover(this));
		addRemover("shapeless", new ShapelessRemover(this));
		addRemover("nbt", new NBTRemover(this));
		addRemover("permission", new PermissionRemover(this));
		addRemover("furnace", new FurnaceRemover(this));
		//TODO add standard removers

		//representations for the listrecipes menu
		recipeToItemMap.put("shaped", recipe -> ((ShapedRecipe) recipe).getRepresentation());
		recipeToItemMap.put("shapeless", recipe -> ((ShapelessRecipe) recipe).getRepresentation());
		recipeToItemMap.put("furnace", recipe -> ((FurnaceRecipe) recipe).getRepresentation());
		recipeToItemMap.put("nbt", recipe -> ((NBTRecipe) recipe).getRepresentation());
		recipeToItemMap.put("permission", recipe -> ((PermissionRecipe) recipe).getRepresentation());

		//recipe displayers
		recipeToCommandSenderDiplayMap.put("shaped", (recipe, commandSender) -> {
			ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
			commandSender.sendMessage("Key: " + craftingManager.getKey(shapedRecipe));
			commandSender.sendMessage("Result: " + InventoryUtils.getItemName(shapedRecipe.getResult()));
			commandSender.sendMessage("Width: " + shapedRecipe.getWidth());
			commandSender.sendMessage("Height: " + shapedRecipe.getHeight());
			commandSender.sendMessage("Ingredients: " + shapedRecipe.getIngredients().stream()
					.map(ingr -> ingr.getChoices().stream().map(InventoryUtils::getItemName).collect(Collectors.toList()))
					.collect(Collectors.toList()));
			if (shapedRecipe.hasGroup()) commandSender.sendMessage("Group: " + shapedRecipe.getGroup());
			if (shapedRecipe.isHidden()) commandSender.sendMessage("Hidden: true");
			commandSender.sendMessage("");
		});
		recipeToCommandSenderDiplayMap.put("shapeless", (recipe, commandSender) -> {
			ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
			commandSender.sendMessage("Key: " + craftingManager.getKey(shapelessRecipe));
			commandSender.sendMessage("Result: " + InventoryUtils.getItemName(shapelessRecipe.getResult()));
			commandSender.sendMessage("Ingredients: " + shapelessRecipe.getIngredients().stream()
					.map(ingr -> ingr.getChoices().stream().map(InventoryUtils::getItemName).collect(Collectors.toList()))
					.collect(Collectors.toList()));
			if (shapelessRecipe.hasGroup()) commandSender.sendMessage("Group: " + shapelessRecipe.getGroup());
			if (shapelessRecipe.isHidden()) commandSender.sendMessage("Hidden: true");
			commandSender.sendMessage("");
		});
		recipeToCommandSenderDiplayMap.put("furnace", (recipe, commandSender) -> {
			FurnaceRecipe furnaceRecipe = (FurnaceRecipe) recipe;
			commandSender.sendMessage("Ingredient: " + InventoryUtils.getItemName(furnaceRecipe.getIngredient()));
			commandSender.sendMessage("Result: " + InventoryUtils.getItemName(furnaceRecipe.getResult()));
			if (furnaceRecipe.hasXp()) commandSender.sendMessage("XP: " + furnaceRecipe.getXp());
			commandSender.sendMessage("");
		});
		recipeToCommandSenderDiplayMap.put("nbt", (recipe, commandSender) -> {
			NBTRecipe nbtRecipe = (NBTRecipe) recipe;
			commandSender.sendMessage("Key: " + craftingManager.getKey(nbtRecipe));
			commandSender.sendMessage("Result: " + InventoryUtils.getItemName(nbtRecipe.getResult()));
			commandSender.sendMessage("Width: " + nbtRecipe.getWidth());
			commandSender.sendMessage("Height: " + nbtRecipe.getHeight());
			commandSender.sendMessage("Ingredients: " + nbtRecipe.getIngredients().stream()
					.map(ingr -> ingr.getChoices().stream().map(InventoryUtils::getItemName).collect(Collectors.toList()))
					.collect(Collectors.toList()));
			if (nbtRecipe.hasGroup()) commandSender.sendMessage("Group: " + nbtRecipe.getGroup());
			if (nbtRecipe.isHidden()) commandSender.sendMessage("Hidden: true");
			commandSender.sendMessage("NBT specific");
			commandSender.sendMessage("");
		});
		recipeToCommandSenderDiplayMap.put("permission", (recipe, commandSender) -> {
			PermissionRecipe permissionRecipe = (PermissionRecipe) recipe;
			commandSender.sendMessage("Key: " + craftingManager.getKey(permissionRecipe));
			commandSender.sendMessage("Result: " + InventoryUtils.getItemName(permissionRecipe.getResult()));
			commandSender.sendMessage("Width: " + permissionRecipe.getWidth());
			commandSender.sendMessage("Height: " + permissionRecipe.getHeight());
			commandSender.sendMessage("Ingredients: " + permissionRecipe.getIngredients().stream()
					.map(ingr -> ingr.getChoices().stream().map(InventoryUtils::getItemName).collect(Collectors.toList()))
					.collect(Collectors.toList()));
			if (permissionRecipe.hasGroup()) commandSender.sendMessage("Group: " + permissionRecipe.getGroup());
			if (permissionRecipe.isHidden()) commandSender.sendMessage("Hidden: true");
			commandSender.sendMessage("Permission: " + permissionRecipe.getPermission());
			commandSender.sendMessage("");
		});

		//recipe providers
		recipeSuppliers.put("shaped", () -> StreamSupport.stream(Spliterators.spliteratorUnknownSize(craftingManager.iterator(),
				Spliterator.NONNULL), false)
				.filter(recipe -> recipe instanceof ShapedRecipe)
				.collect(Collectors.toList()));
		recipeSuppliers.put("shapeless", () -> StreamSupport.stream(Spliterators.spliteratorUnknownSize(craftingManager.iterator(),
				Spliterator.NONNULL), false)
				.filter(recipe -> recipe instanceof ShapelessRecipe)
				.collect(Collectors.toList()));
		recipeSuppliers.put("furnace", () -> StreamSupport.stream(Spliterators.spliteratorUnknownSize(furnaceManager.iterator(),
				Spliterator.NONNULL), false)
				.collect(Collectors.toList()));
		recipeSuppliers.put("nbt", () -> StreamSupport.stream(Spliterators.spliteratorUnknownSize(craftingManager.iterator(),
				Spliterator.NONNULL), false)
				.filter(recipe -> recipe instanceof NBTRecipe)
				.collect(Collectors.toList()));
		recipeSuppliers.put("permission", () -> StreamSupport.stream(Spliterators.spliteratorUnknownSize(craftingManager.iterator(),
				Spliterator.NONNULL), false)
				.filter(recipe -> recipe instanceof PermissionRecipe)
				.collect(Collectors.toList()));
	}


	public boolean addAdder(String recipeType, BiConsumer<? super Player, ? super List<String>> adder) {
		return adders.putIfAbsent(recipeType, adder) == null;
	}
	
	public boolean addRemover(String recipeType, BiConsumer<? super Player, ? super List<String>> remover) {
		return removers.putIfAbsent(recipeType, remover) == null;
	}


	@Override
	public void onEnable() {
		getCommand("addrecipe").setExecutor(new AddRecipeCommandExecutor(Collections.unmodifiableNavigableMap(adders)));
		getCommand("removerecipe").setExecutor(new RemoveRecipeCommandExecutor(Collections.unmodifiableNavigableMap(removers)));
		getCommand("listrecipes").setExecutor(new ListRecipesCommandExecutor(this::getRecipes,
				Collections.unmodifiableMap(recipeToItemMap),
				Collections.unmodifiableMap(recipeToCommandSenderDiplayMap)));

		getServer().getPluginManager().registerEvents(new ListRecipesListener(), this);
	}


	public static CustomRecipesPlugin getInstance() {
		return JavaPlugin.getPlugin(CustomRecipesPlugin.class);
	}


	@Override
	public CRCraftingManager getCraftingManager() {
		return craftingManager;
	}

	@Override
	public CRFurnaceManager getFurnaceManager() {
		return furnaceManager;
	}


	@Override
	public boolean isVanillaRecipeType(CraftingRecipe recipe) {
		if (!(recipe instanceof CRVanillaRecipe)) return false;

		CRVanillaRecipe<? extends IRecipe> vanillaWrapper = (CRVanillaRecipe<? extends IRecipe>) recipe;
		return vanillaWrapper.getHandle().getClass().getName().startsWith("net.minecraft.server.");
	}


	@Override
	public ShapedRecipe asCustomRecipesMirror(org.bukkit.inventory.ShapedRecipe bukkitRecipe) {
		String[] shape = bukkitRecipe.getShape();
		Map<Character, ItemStack> map = bukkitRecipe.getIngredientMap();
		List<? extends ChoiceIngredient> ingredients = Arrays.stream(shape)
				.flatMapToInt(s -> s.chars())
				.mapToObj(i -> map.getOrDefault((char) i, null))
				.map(SimpleChoiceIngredient::fromChoices)
				.collect(Collectors.toList());

		int width = shape[0].length();
		int height = shape.length;

		SimpleShapedRecipe simple = new SimpleShapedRecipe(bukkitRecipe.getResult(), width, height, ingredients);
		CraftingRecipe byKey = craftingManager.getRecipe(bukkitRecipe.getKey()); //TODO can we do better? get by result and by ingredients?
		return simple.equals(byKey) ? (ShapedRecipe) byKey : simple;
	}

	@Override
	public ShapelessRecipe asCustomRecipesMirror(org.bukkit.inventory.ShapelessRecipe bukkitRecipe) {
		List<? extends ChoiceIngredient> ingredients = bukkitRecipe.getIngredientList().stream()
				.map(SimpleChoiceIngredient::fromChoices)
				.collect(Collectors.toList());

		SimpleShapelessRecipe simple = new SimpleShapelessRecipe(bukkitRecipe.getResult(), ingredients);
		CraftingRecipe byKey = craftingManager.getRecipe(bukkitRecipe.getKey()); //TODO can we do better? get by result and by ingredients?
		return simple.equals(byKey) ? (ShapelessRecipe) byKey : simple;
	}

	@Override
	public FurnaceRecipe asCustomRecipesMirror(org.bukkit.inventory.FurnaceRecipe bukkitRecipe) {
		SimpleFurnaceRecipe simple = new SimpleFurnaceRecipe(bukkitRecipe.getInput(), bukkitRecipe.getResult(), bukkitRecipe.getExperience());

		CRFurnaceRecipe recipe = furnaceManager.getRecipe(bukkitRecipe.getInput());
		return simple.equals(recipe) ? recipe : simple;
	}


	public List<? extends Recipe> getRecipes(String type) {
		return recipeSuppliers.getOrDefault(type, Collections::emptyList).get();
	}




	public void setCraftingManager(CRCraftingManager craftingManager) {
		this.craftingManager = Objects.requireNonNull(craftingManager);
	}

	public void setFurnaceManager(CRFurnaceManager furnaceManager) {
		this.furnaceManager = Objects.requireNonNull(furnaceManager);
	}
	
	@SuppressWarnings("deprecation")
	public NamespacedKey getKey(String string) {
		String[] split = string.split(":");
		return split.length == 1 ? new NamespacedKey(this, string) : new NamespacedKey(split[0], split[1]);
	}

}
