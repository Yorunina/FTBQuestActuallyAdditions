package net.yorunina.maa.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.yorunina.maa.ModpackActuallyAdditions;
import net.yorunina.maa.compat.lootr.LootrContainerRows;

@Mod.EventBusSubscriber(modid = ModpackActuallyAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MAAAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, ModpackActuallyAdditions.MODID);

    public static final RegistryObject<Attribute> LOOTR_CONTAINER_ROWS = ATTRIBUTES.register(
            "lootr_container_rows",
            () -> new RangedAttribute(
                    "attribute.name.maa.lootr_container_rows",
                    LootrContainerRows.DEFAULT_ROWS,
                    LootrContainerRows.MIN_ROWS,
                    LootrContainerRows.MAX_ROWS
            ).setSyncable(true)
    );

    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, LOOTR_CONTAINER_ROWS.get());
    }
}