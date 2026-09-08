package net.yorunina.maa.mixin.biomancy;

import com.github.elenterius.biomancy.entity.mob.ai.goal.FindItemGoal;
import com.github.elenterius.biomancy.entity.mob.fleshblob.AdulteratedEaterFleshBlob;
import com.github.elenterius.biomancy.entity.mob.fleshblob.AdulteratedHangryEaterFleshBlob;
import com.github.elenterius.biomancy.entity.mob.fleshblob.PrimordialEaterFleshBlob;
import com.github.elenterius.biomancy.entity.mob.fleshblob.PrimordialHangryEaterFleshBlob;
import net.minecraft.world.entity.item.ItemEntity;
import net.yorunina.maa.compat.biomancy.BiomancyOrganHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(value = {
        AdulteratedEaterFleshBlob.class,
        AdulteratedHangryEaterFleshBlob.class,
        PrimordialEaterFleshBlob.class,
        PrimordialHangryEaterFleshBlob.class
})
public abstract class MixinPrimordialEaterFleshBlob {
    @ModifyArg(
            method = "registerGoals",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/elenterius/biomancy/entity/mob/ai/goal/FindItemGoal;<init>(Lnet/minecraft/world/entity/Mob;DLjava/util/function/Predicate;)V",
                    remap = false
            ),
            index = 2
    )
    private Predicate<ItemEntity> maa$seekOrganItems(Predicate<ItemEntity> original) {
        return itemEntity -> original.test(itemEntity)
                || (FindItemGoal.ITEM_ENTITY_FILTER.test(itemEntity) && BiomancyOrganHelper.canHoldOrganItem(itemEntity.getItem()));
    }
}