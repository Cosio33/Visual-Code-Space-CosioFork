/*
 * This file is part of Visual Code Space.
 *
 * Visual Code Space is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Visual Code Space is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Visual Code Space.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.teixeira.vcspace.core.ai

import android.content.Context

object AiManager {
    @Volatile
    private var settings: AiSettings? = null

    @Volatile
    private var provider: AiProvider? = null

    fun getSettings(context: Context): AiSettings {
        return settings ?: synchronized(this) {
            settings ?: AiSettings(context.applicationContext).also { settings = it }
        }
    }

    fun getProvider(context: Context): AiProvider {
        val currentSettings = getSettings(context)
        return provider ?: synchronized(this) {
            provider ?: GeminiProvider(currentSettings).also { provider = it }
        }
    }

    fun getProvider(settings: AiSettings): AiProvider {
        return provider ?: synchronized(this) {
            provider ?: GeminiProvider(settings).also { provider = it }
        }
    }

    fun resetProvider() {
        provider = null
    }

    fun reset() {
        provider = null
        settings = null
    }
}
