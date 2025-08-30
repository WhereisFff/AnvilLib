package dev.anvilcraft.lib.init;

import dev.anvilcraft.lib.AnvilLib;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class LibRecipeBookCategories {
    private static final DeferredRegister<RecipeBookCategory> DF = DeferredRegister.create(
        BuiltInRegistries.RECIPE_BOOK_CATEGORY,
        AnvilLib.MOD_ID
    );

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> IN_WORLD_RECIPE = register("in_world_recipe");

    public static DeferredHolder<RecipeBookCategory, RecipeBookCategory> register(String name) {
        return DF.register(name, RecipeBookCategory::new);
    }
}
