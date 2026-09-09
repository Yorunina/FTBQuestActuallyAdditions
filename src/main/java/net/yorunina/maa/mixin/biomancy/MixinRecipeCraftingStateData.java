package net.yorunina.maa.mixin.biomancy;

import com.github.elenterius.biomancy.crafting.recipe.BioBrewingRecipe;
import com.github.elenterius.biomancy.crafting.recipe.PotionSerumRecipes;
import com.github.elenterius.biomancy.crafting.state.RecipeCraftingStateData;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(RecipeCraftingStateData.class)
public abstract class MixinRecipeCraftingStateData {
    @Shadow(remap = false)
    protected ResourceLocation recipeId;

    @Shadow(remap = false)
    protected abstract boolean isRecipeOfInstance(Recipe<?> recipe);

    @ModifyReturnValue(method = "getCraftingGoalRecipe", at = @At("RETURN"), remap = false)
    private Optional<?> maa$resolvePotionSerum(Optional<?> original, Level level) {
        if (original.isPresent() || this.recipeId == null) {
            return original;
        }
        for (BioBrewingRecipe recipe : PotionSerumRecipes.RECIPES) {
            if (this.recipeId.equals(recipe.getId()) && isRecipeOfInstance(recipe)) {
                return Optional.of(recipe);
            }
        }
        return original;
    }
}