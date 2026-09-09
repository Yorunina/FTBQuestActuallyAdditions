package net.yorunina.maa.mixin;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    @Unique
    private static final ResourceLocation maa$ROLL_COUNT_ID = new ResourceLocation("block_factorys_bosses", "roll_count");

    @Unique
    private static Attribute maa$rollCount;

    @Shadow
    @Nullable
    private CompoundTag tag;

    @Inject(method = "isEnchanted", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;getList(Ljava/lang/String;I)Lnet/minecraft/nbt/ListTag;"), cancellable = true)
    private void isEnchanted(CallbackInfoReturnable<Boolean> cir) {
        if (this.tag.getBoolean("hideEnchant")) {
            cir.setReturnValue(false);
        }
    }

    @ModifyExpressionValue(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shouldShowInTooltip(ILnet/minecraft/world/item/ItemStack$TooltipPart;)Z", ordinal = 2))
    private boolean shouldShowEnchantInTooltip(boolean originaltn) {
        if (this.tag != null && this.tag.getBoolean("hideEnchant")) {
            return false;
        }
        return originaltn;
    }

    @Inject(method = "getAttributeModifiers", at = @At("RETURN"), cancellable = true)
    private void maa$stripRollCount(EquipmentSlot slot, CallbackInfoReturnable<Multimap<Attribute, AttributeModifier>> cir) {
        Attribute rollCount = maa$rollCount();
        if (rollCount == null) {
            return;
        }
        Multimap<Attribute, AttributeModifier> original = cir.getReturnValue();
        if (original == null || !original.containsKey(rollCount)) {
            return;
        }
        LinkedHashMultimap<Attribute, AttributeModifier> filtered = LinkedHashMultimap.create(original);
        filtered.removeAll(rollCount);
        cir.setReturnValue(filtered);
    }

    @Unique
    private static Attribute maa$rollCount() {
        if (maa$rollCount == null) {
            maa$rollCount = ForgeRegistries.ATTRIBUTES.getValue(maa$ROLL_COUNT_ID);
        }
        return maa$rollCount;
    }
}