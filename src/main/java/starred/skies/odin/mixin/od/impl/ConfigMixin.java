package starred.skies.odin.mixin.od.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.odtheking.odin.config.Config;
import com.odtheking.odin.features.Category;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import starred.skies.odin.CategoryAdapter;

@Mixin(Config.class)
public class ConfigMixin {
    @Final
    @Shadow
    @Mutable
    private static Gson gson;

    @Inject(method = "<clinit>", at = @At("RETURN"), remap = false)
    private static void registerCategoryAdapter(CallbackInfo ci) {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Category.class, new CategoryAdapter())
                .create();
    }
}