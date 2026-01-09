/*
BSD 3-Clause License

Copyright (c) 2025-2026, Starred

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

@file:Suppress("UNUSED")

package starred.skies.odin.helpers

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import com.odtheking.odin.OdinMod.mc
import kotlinx.coroutines.*
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.data.registries.VanillaRegistries
import net.minecraft.resources.RegistryOps
import java.io.File
import kotlin.jvm.optionals.getOrNull
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Manages persistent storage of data in JSON format.
 *
 * @property path The relative path to the storage file (without extension)
 */
class Scribble(private val path: String) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
    private val file: File = File(FabricLoader.getInstance().configDir.toFile(), "odinClient/$path.json").apply { parentFile.mkdirs() }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var root: JsonObject? = null
    private var isDirty = false
    private var saveJob: Job? = null

    private fun <T : Any> JsonElement?.toData(codec: Codec<T>): T? = codec.parse(JsonOps.INSTANCE, this).result().getOrNull()
    private fun <T : Any> T.toJson(codec: Codec<T>): JsonElement? = codec.encodeStart(JsonOps.INSTANCE, this).result().getOrNull()

    init {
        ClientLifecycleEvents.CLIENT_STOPPING.register { _ ->
            scope.launch {
                saveJob?.cancelAndJoin()
                save()
            }
        }
    }

    private fun load(): JsonObject {
        root?.let { return it }

        root = try {
            if (file.exists() && file.length() > 0) {
                val content = file.readText()
                val element = JsonParser.parseString(content)
                if (element.isJsonObject) element.asJsonObject.getAsJsonObject("@odinClient:data") else JsonObject()
            } else {
                JsonObject()
            }
        } catch (e: Exception) {
            JsonObject()
        }

        return root!!
    }

    private fun save() {
        if (!isDirty) return

        try {
            val data = root ?: return

            val wrapped = JsonObject().apply {
                add("@odinClient:data", data)
            }

            val tempFile = File(file.parent, "${file.name}.tmp")
            tempFile.writeText(gson.toJson(wrapped))

            if (file.exists()) file.delete()
            if (!tempFile.renameTo(file)) {
                tempFile.copyTo(file, overwrite = true)
                tempFile.delete()
            }

            isDirty = false
        } catch (e: Exception) {
        }
    }

    private fun markDirty() {
        isDirty = true
        scheduleSave()
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = scope.launch {
            delay(5000)
            save()
        }
    }

    /**
     * Reloads data from disk, discarding any unsaved changes.
     */
    fun reload() {
        root = null
        isDirty = false
    }

    /**
     * Internal property delegate for codec-based values.
     *
     * @param T The type of value to store
     * @property key The key to store the value under
     * @property default The default value if the key doesn't exist
     * @property codec The codec for serialization/deserialization
     */
    inner class Value<T : Any>(
        private val key: String,
        private val default: T,
        private val codec: Codec<T>
    ) : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return load().get(key)?.toData(codec) ?: default
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            value.toJson(codec)?.let {
                load().add(key, it)
                markDirty()
            }
        }
    }

    /**
     * Creates a delegated property for storing an Int.
     */
    fun int(key: String, default: Int = 0) = Value(key, default, Codec.INT)

    /**
     * Creates a delegated property for storing a Long.
     */
    fun long(key: String, default: Long = 0L) = Value(key, default, Codec.LONG)

    /**
     * Creates a delegated property for storing a String.
     */
    fun string(key: String, default: String = "") = Value(key, default, Codec.STRING)

    /**
     * Creates a delegated property for storing a Boolean.
     */
    fun boolean(key: String, default: Boolean = false) = Value(key, default, Codec.BOOL)

    /**
     * Creates a delegated property for storing a Double.
     */
    fun double(key: String, default: Double = 0.0) = Value(key, default, Codec.DOUBLE)

    /**
     * Creates a delegated property for storing a Float.
     */
    fun float(key: String, default: Float = 0f) = Value(key, default, Codec.FLOAT)

    /**
     * Creates a delegated property for storing a List.
     */
    fun <T : Any> list(key: String, codec: Codec<T>, default: List<T> = emptyList()) = Value(key, default, codec.listOf())

    /**
     * Creates a delegated property for storing a Set.
     */
    fun <T : Any> set(key: String, codec: Codec<T>, default: Set<T> = emptySet()) = Value(key, default, codec.listOf().xmap({ it.toSet() }, { it.toList() }))

    /**
     * Creates a delegated property for storing a Map.
     */
    fun <K : Any, V : Any> map(key: String, keyCodec: Codec<K>, valueCodec: Codec<V>, default: Map<K, V> = emptyMap()) = Value(key, default, Codec.unboundedMap(keyCodec, valueCodec))
}