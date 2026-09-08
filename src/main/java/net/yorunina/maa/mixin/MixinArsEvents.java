package net.yorunina.maa.mixin;

import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import com.hollingsworth.arsnouveau.common.event.ArsEvents;
import com.hollingsworth.arsnouveau.setup.config.ServerConfig;
import com.hollingsworth.arsnouveau.setup.registry.EnchantmentRegistry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetrawear.item.ModularArmor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(ArsEvents.class)
public class MixinArsEvents {
    private static final Map<EquipmentSlot, UUID> MANA_BOOST_UUIDS = new HashMap<>();
    private static final Map<EquipmentSlot, UUID> MANA_REGEN_UUIDS = new HashMap<>();
    
    static {
        MANA_BOOST_UUIDS.put(EquipmentSlot.HEAD, UUID.fromString("6f98cbcf-b7a7-441b-9a29-9cb5a1db45ac"));
        MANA_BOOST_UUIDS.put(EquipmentSlot.CHEST, UUID.fromString("85ebfd0e-7f69-420d-974a-187541fa930f"));
        MANA_BOOST_UUIDS.put(EquipmentSlot.LEGS, UUID.fromString("b4d50c99-d485-4a06-a9e9-188c8508e327"));
        MANA_BOOST_UUIDS.put(EquipmentSlot.FEET, UUID.fromString("600faadf-3199-4e4f-9a3b-35b8fcffa10d"));
        
        MANA_REGEN_UUIDS.put(EquipmentSlot.HEAD, UUID.fromString("25d96386-4b08-4586-adb2-9af64c82fd52"));
        MANA_REGEN_UUIDS.put(EquipmentSlot.CHEST, UUID.fromString("76d2386f-49a3-4051-9238-3b506993f9c1"));
        MANA_REGEN_UUIDS.put(EquipmentSlot.LEGS, UUID.fromString("57bcd19b-db35-4e6a-bbb4-dee52d0ee715"));
        MANA_REGEN_UUIDS.put(EquipmentSlot.FEET, UUID.fromString("48ee5123-be00-44c2-a754-6162b20b9a5b"));
    }

    @Inject(method = "modifyItemAttributes", at = @At("HEAD"), cancellable = true, remap = false)
    private static void modifyItemAttributes(ItemAttributeModifierEvent event, CallbackInfo ci) {
        ItemStack itemStack = event.getItemStack();
        
        if (!itemStack.isEnchanted()) {
            return;
        }
        
        if (itemStack.getItem() instanceof ModularArmor armor) {
            if (event.getSlotType() != armor.getEquipmentSlot()) {
                ci.cancel();
                return;
            }
            
            int manaBoostLevel = itemStack.getEnchantmentLevel(EnchantmentRegistry.MANA_BOOST_ENCHANTMENT.get());
            if (manaBoostLevel > 0) {
                UUID uuid = MANA_BOOST_UUIDS.get(event.getSlotType());
                event.addModifier(PerkAttributes.MAX_MANA.get(), 
                    new AttributeModifier(
                        uuid, 
                        "max_mana_enchant_modular", 
                        ServerConfig.MANA_BOOST_BONUS.get() * manaBoostLevel,
                        AttributeModifier.Operation.ADDITION
                    )
                );
            }
            
            int manaRegenLevel = itemStack.getEnchantmentLevel(EnchantmentRegistry.MANA_REGEN_ENCHANTMENT.get());
            if (manaRegenLevel > 0) {
                UUID uuid = MANA_REGEN_UUIDS.get(event.getSlotType());
                event.addModifier(PerkAttributes.MANA_REGEN_BONUS.get(), 
                    new AttributeModifier(
                        uuid, 
                        "mana_regen_enchant_modular", 
                        ServerConfig.MANA_REGEN_ENCHANT_BONUS.get() * manaRegenLevel,
                        AttributeModifier.Operation.ADDITION
                    )
                );
            }
            
            ci.cancel();
        }
    }
}