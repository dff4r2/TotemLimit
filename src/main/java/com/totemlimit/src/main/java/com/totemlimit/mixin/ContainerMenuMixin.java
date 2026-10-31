package com.totemlimit.mixin;

import com.totemlimit.TotemLimitMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class ContainerMenuMixin {
    @Unique private ItemStack[] tl$slots;
    @Unique private ItemStack[] tl$inv;
    @Unique private ItemStack tl$carried;
    @Unique private int tl$before;

    @Inject(method = "clicked", at = @At("HEAD"))
    private void tl$head(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        tl$slots = null;
        tl$inv = null;
        tl$carried = null;
        if (!(player instanceof ServerPlayer sp) || sp.isCreative() || sp.isSpectator()) return;

        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        tl$before = TotemLimitMod.count(sp, menu.getCarried());
        if (tl$before < TotemLimitMod.max()) return;

        tl$slots = new ItemStack[menu.slots.size()];
        for (int i = 0; i < tl$slots.length; i++) {
            tl$slots[i] = menu.slots.get(i).getItem().copy();
        }
        Inventory inv = sp.getInventory();
        tl$inv = new ItemStack[inv.getContainerSize()];
        for (int i = 0; i < tl$inv.length; i++) {
            tl$inv[i] = inv.getItem(i).copy();
        }
        tl$carried = menu.getCarried().copy();
    }

    @Inject(method = "clicked", at = @At("RETURN"))
    private void tl$tail(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (tl$slots == null || !(player instanceof ServerPlayer sp)) return;

        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        int after = TotemLimitMod.count(sp, menu.getCarried());

        if (after > tl$before && after > TotemLimitMod.max()) {
            for (int i = 0; i < tl$slots.length && i < menu.slots.size(); i++) {
                menu.slots.get(i).set(tl$slots[i].copy());
            }
            Inventory inv = sp.getInventory();
            for (int i = 0; i < tl$inv.length && i < inv.getContainerSize(); i++) {
                inv.setItem(i, tl$inv[i].copy());
            }
            menu.setCarried(tl$carried.copy());
            menu.broadcastFullState();
            TotemLimitMod.warn(sp);
        }

        tl$slots = null;
        tl$inv = null;
        tl$carried = null;
    }
}
