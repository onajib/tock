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

package fr.vsct.tock.bot.connector.ga

import fr.vsct.tock.bot.connector.ga.model.response.GASimpleResponse
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.translator.TextAndVoiceTranslatedString
import fr.vsct.tock.translator.isSSML

/**
 * Provides a [GASimpleResponse] with specified [CharSequence].
 */
fun BotBus.simpleResponse(text: CharSequence): GASimpleResponse {
    val t = translate(text)
    return if (t is TextAndVoiceTranslatedString) {
        simpleTextAndVoiceResponse(t)
    } else if (t.isSSML()) {
        flexibleSimpleResponse(ssml = t)
    } else {
        flexibleSimpleResponse(textToSpeech = t)
    }
}

/**
 * Provides a [GASimpleResponse] with specified textToSpeech, ssml and displayText.
 */
fun BotBus.flexibleSimpleResponse(
        textToSpeech: CharSequence? = null,
        ssml: CharSequence? = null,
        displayText: CharSequence? = null): GASimpleResponse {
    val t = translateAndSetBlankAsNull(textToSpeech)
    val s = translateAndSetBlankAsNull(ssml)
    val d = translateAndSetBlankAsNull(displayText)

    return simpleResponse(t, s, d)
}


private fun simpleResponse(textToSpeech: String?, ssml: String?, displayText: String?): GASimpleResponse {
    val ssmlWithoutEmoji = ssml?.removeEmojis()
    val textToSpeechWithoutEmoji = if (ssmlWithoutEmoji.isNullOrBlank()) textToSpeech?.removeEmojis().run { if (isNullOrBlank()) " - " else this } else null
    return GASimpleResponse(textToSpeechWithoutEmoji, ssmlWithoutEmoji, displayText)
}

private fun simpleTextAndVoiceResponse(text: TextAndVoiceTranslatedString): GASimpleResponse {
    val t = if (text.isSSML()) null else text.voice.toString()
    val s = if (text.isSSML()) text.voice.toString() else null
    val d = text.text.toString()

    return simpleResponse(t, s, d)
}

internal fun simpleResponseWithoutTranslate(text: CharSequence): GASimpleResponse {
    return if (text is TextAndVoiceTranslatedString) {
        simpleTextAndVoiceResponse(text)
    } else if (text.isSSML()) {
        flexibleSimpleResponseWithoutTranslate(ssml = text)
    } else {
        flexibleSimpleResponseWithoutTranslate(textToSpeech = text)
    }
}

internal fun flexibleSimpleResponseWithoutTranslate(
        textToSpeech: CharSequence? = null,
        ssml: CharSequence? = null,
        displayText: CharSequence? = null): GASimpleResponse {
    val t = textToSpeech.setBlankAsNull()
    val s = ssml.setBlankAsNull()
    val d = displayText.setBlankAsNull()

    return simpleResponse(t, s, d)
}
