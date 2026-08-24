package net.yorunina.maa.mixin;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.IconAnimation;
import net.yorunina.maa.model.IIconAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(Icon.class)
public abstract class MixinIcon {
    @Redirect(method = "getIcon(Lcom/google/gson/JsonElement;)Ldev/ftb/mods/ftblibrary/icon/Icon;", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftblibrary/icon/IconAnimation;fromList(Ljava/util/List;Z)Ldev/ftb/mods/ftblibrary/icon/Icon;"), remap = false)
    private static Icon getIcon(List<Icon> icons, boolean includeEmpty, @Local(name = "o") JsonObject o) {
        Icon icon =  IconAnimation.fromList(icons, includeEmpty);
        if (icon instanceof IconAnimation animationIcon) {
            ((IIconAnimation)animationIcon).setFrameLength(o.get("frame_length").getAsLong());
        }
        return icon;
    }
}
