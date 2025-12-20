package starred.skies.odin.mixin.od.impl;

import com.odtheking.odin.features.Module;
import com.odtheking.odin.features.ModuleManager;
import com.odtheking.odin.features.impl.dungeon.BreakerDisplay;
import com.odtheking.odin.features.impl.dungeon.KeyHighlight;
import com.odtheking.odin.features.impl.dungeon.LividSolver;
import com.odtheking.odin.features.impl.dungeon.SpiritBear;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(value = ModuleManager.class, remap = false)
public class ModuleManagerMixin {
    
    @Final
    @Shadow
    private static ArrayList<Module> modules;
    
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void removeModules(CallbackInfo ci) {
        modules.removeIf(module ->
            module instanceof LividSolver ||
            module instanceof BreakerDisplay ||
            module instanceof KeyHighlight ||
            module instanceof SpiritBear
        );
    }
}