package starred.skies.odin.mixin.od.impl;

import com.odtheking.odin.features.Category;
import kotlin.enums.EnumEntries;
import kotlin.enums.EnumEntriesKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import starred.skies.odin.CategoryAccessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(Category.class)
public class CategoryMixin implements CategoryAccessor {
    @Unique
    private static final List<Category> CUSTOM_CATEGORIES = new ArrayList<>();

    @Override
    public Category registerCategory(String name, String displayName) {
        Category custom = invokeInit(name, Category.values().length + CUSTOM_CATEGORIES.size(), displayName);
        CUSTOM_CATEGORIES.add(custom);
        return custom;
    }

    @Inject(method = "values", at = @At("RETURN"), cancellable = true, remap = false)
    private static void injectCustomCategories(CallbackInfoReturnable<Category[]> cir) {
        if (CUSTOM_CATEGORIES.isEmpty()) return;

        Category[] original = cir.getReturnValue();
        Category[] expanded = Arrays.copyOf(original, original.length + CUSTOM_CATEGORIES.size());
        System.arraycopy(CUSTOM_CATEGORIES.toArray(new Category[0]), 0, expanded, original.length, CUSTOM_CATEGORIES.size());

        cir.setReturnValue(expanded);
    }

    @Inject(method = "getEntries", at = @At("RETURN"), cancellable = true, remap = false)
    private static void injectCustomEntries(CallbackInfoReturnable<EnumEntries<Category>> cir) {
        if (CUSTOM_CATEGORIES.isEmpty()) return;
        cir.setReturnValue(EnumEntriesKt.enumEntries(Category.values()));
    }

    @Invoker("<init>")
    private static Category invokeInit(String enumName, int ordinal, String displayName) {
        throw new AssertionError();
    }
}