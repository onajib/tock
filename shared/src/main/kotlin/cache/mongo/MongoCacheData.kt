/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.shared.cache.mongo

import fr.vsct.tock.shared.jackson.AnyValueWrapper
import org.litote.kmongo.Data
import org.litote.kmongo.Id
import org.litote.kmongo.JacksonData
import org.litote.kmongo.newId
import java.time.Instant

/**
 *
 */
@Data(internal = true)
@JacksonData(internal = true)
internal class MongoCacheData(
    val id: Id<out Any?> = newId(),
    val type: String,
    val s: String? = null,
    val b: ByteArray? = null,
    val a: AnyValueWrapper? = null,
    val date: Instant = Instant.now()
) {

    companion object {
        fun <T : Any> fromValue(id: Id<T>, type: String, v: T): MongoCacheData {
            return when (v) {
                is String -> MongoCacheData(id, type, s = v)
                is ByteArray -> MongoCacheData(id, type, b = v)
                else -> MongoCacheData(id, type, a = AnyValueWrapper(v))
            }
        }
    }

    fun toValue(): Any {
        return s ?: b ?: a!!.value!!
    }

}