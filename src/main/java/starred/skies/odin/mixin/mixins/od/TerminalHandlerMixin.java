package starred.skies.odin.mixin.mixins.od;

import com.odtheking.odin.events.GuiEvent;
import com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler.TerminalHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import starred.skies.odin.events.TerminalUpdateEvent;

@Mixin(TerminalHandler.class)
public class TerminalHandlerMixin {
    @Inject(method = "updateSlot", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CopyOnWriteArrayList;addAll(Ljava/util/Collection;)Z", shift = At.Shift.AFTER))
    private void onUpdateSlot(GuiEvent.SlotUpdate event, CallbackInfo ci) {
        new TerminalUpdateEvent((TerminalHandler) (Object) this).postAndCatch();
    }
}
