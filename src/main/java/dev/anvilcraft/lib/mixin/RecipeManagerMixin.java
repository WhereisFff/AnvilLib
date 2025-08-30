package dev.anvilcraft.lib.mixin;

import dev.anvilcraft.lib.injection.IRecipeManagerExtension;
import dev.anvilcraft.lib.recipe.InWorldRecipe;
import dev.anvilcraft.lib.recipe.util.InWorldRecipeManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(RecipeManager.class)
abstract class RecipeManagerMixin implements IRecipeManagerExtension {
    @Shadow
    @Final
    private HolderLookup.Provider registries;
    @Shadow
    private RecipeMap recipes;
    @Unique
    private InWorldRecipeManager anvillib$inWorldRecipeManager = null;

    @Override
    public void anvillib$setInWorldRecipeManager(InWorldRecipeManager manager) {
        this.anvillib$inWorldRecipeManager = manager;
    }

    @Override
    public InWorldRecipeManager anvillib$getInWorldRecipeManager() {
        return this.anvillib$inWorldRecipeManager;
    }

    @Override
    public HolderLookup.Provider anvillib$getRegistries() {
        return this.registries;
    }

    @Override
    public void anvillib$addRecipes(@NotNull List<RecipeHolder<InWorldRecipe>> recipes) {
        Set<RecipeHolder<?>> recipeHolderSet = new HashSet<>(this.recipes.values());
        Set<ResourceLocation> keys = new HashSet<>();
        recipes.forEach(recipe -> {
            if (keys.contains(recipe.id().location())) return;
            keys.add(recipe.id().location());
            recipeHolderSet.add(recipe);
        });
        this.recipes = RecipeMap.create(recipeHolderSet);
    }
}
