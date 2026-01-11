package starred.skies.odin.mixin.accessors;

import com.odtheking.odin.features.impl.floor7.SimonSays;
import kotlin.Triple;
import kotlin.jvm.functions.Function0;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(SimonSays.class)
public interface SimonSaysAccessor {
    @Accessor("startButton")
    BlockPos getStartButton();

    @Accessor("lastLanternTick")
    int getLastLanternTick();

    @Accessor("firstPhase")
    boolean getFirstPhase();

    @Accessor("grid")
    Set<BlockPos> getGrid();

    @Invoker("getCustomClickSounds")
    boolean getCustomClickSounds();

    @Accessor("blockedClick")
    Function0<Triple<String, Float, Float>> getBlockedClick();

    @Accessor("correctClick")
    Function0<Triple<String, Float, Float>> getCorrectClick();
}