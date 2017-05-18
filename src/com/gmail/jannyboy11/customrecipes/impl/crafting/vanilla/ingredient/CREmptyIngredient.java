package com.gmail.jannyboy11.customrecipes.impl.crafting.vanilla.ingredient;

import com.gmail.jannyboy11.customrecipes.api.crafting.vanilla.ingredient.EmptyIngredient;

import net.minecraft.server.v1_12_R1.RecipeItemStack;

public class CREmptyIngredient extends CRChoiceIngredient implements EmptyIngredient {
	
	public static final CREmptyIngredient INSTANCE = new CREmptyIngredient();

	private CREmptyIngredient() {
		super(RecipeItemStack.a);
	}

}
