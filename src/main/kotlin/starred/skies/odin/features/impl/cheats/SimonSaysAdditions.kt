package starred.skies.odin.features.impl.cheats

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.BlockInteractEvent
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.floor7.SimonSays
import com.odtheking.odin.utils.playSoundSettings
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import net.minecraft.world.level.block.Blocks
import starred.skies.odin.utils.Skit
import starred.skies.odin.mixin.accessors.SimonSaysAccessor

object SimonSaysAdditions : Module(
    name = "SS Additions",
    description = "Additions to the Simon Says module",
    category = Skit.CHEATS
) {
    private val blockWrongStart by BooleanSetting("Block Wrong on Start", false, desc = "Blocks wrong clicks on the start button during first phase.")
    private val maxStartClicks by NumberSetting("Max Start Clicks", 4, 1, 10, 1, desc = "Maximum number of start button clicks allowed during first phase.").withDependency { blockWrongStart }

    var startClickCounter = 0

    init {
        on<WorldLoadEvent> {
            startClickCounter = 0
        }

        on<ChatPacketEvent> {
            if (value == "[BOSS] Goldor: Who dares trespass into my domain?") startClickCounter = 0
        }

        on<BlockInteractEvent> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@on

            @Suppress("CAST_NEVER_SUCCEEDS") // yes it does! nullable just in case some fuckery happens :p
            val accessor = (SimonSays as? SimonSaysAccessor) ?: return@on

            if (pos == accessor.startButton && accessor.firstPhase && blockWrongStart) {
                if (startClickCounter++ >= maxStartClicks && mc.player?.isShiftKeyDown == false) {
                    if (accessor.customClickSounds) playSoundSettings(accessor.blockedClick())
                    cancel()
                    return@on
                } else if (accessor.customClickSounds) playSoundSettings(accessor.correctClick())
            }
        }

        on<TickEvent.Server> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@on

            @Suppress("CAST_NEVER_SUCCEEDS") // yes it does! nullable just in case some fuckery happens :p
            val accessor = (SimonSays as? SimonSaysAccessor) ?: return@on
            if (!accessor.firstPhase) return@on

            if (accessor.lastLanternTick > 10 && accessor.grid.count { mc.level?.getBlockState(it)?.block == Blocks.STONE_BUTTON } > 8) {
                startClickCounter = 0
            }
        }
    }
}