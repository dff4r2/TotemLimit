package com.totemlimit.mixin;

import com.totemlimit.TotemLimitMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void tl$touch(Player player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer sp) || sp.isCreative() || sp.isSpectator()) return;

        ItemStack stack = ((ItemEntity) (Object) this).getItem();
        if (!stack.is(Items.TOTEM_OF_UNDYING)) return;

        if (TotemLimitMod.count(sp, null) + stack.getCount() > TotemLimitMod.max()) {
            ci.cancel();
            if (sp.tickCount % 20 == 0) TotemLimitMod.warn(sp);
        }
    }
}
