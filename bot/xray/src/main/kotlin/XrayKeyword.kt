/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.bot.xray.keyword

import ai.tock.bot.engine.nlp.BuiltInKeywordListener
import ai.tock.bot.engine.nlp.KeywordService
import ai.tock.shared.property
import java.util.concurrent.ConcurrentSkipListSet

class XrayKeyword : KeywordService {
    val XRAY_KEYWORD = property("tock_bot_xray_creation_keyword", "_xray_")
    val XRAY_UPDATE_KEYWORD = property("tock_bot_xray_update_keyword", "_xray_update_")

    init {
        BuiltInKeywordListener.keywords.addAll(ConcurrentSkipListSet<String>(
                listOf(XRAY_KEYWORD, XRAY_UPDATE_KEYWORD)))
        BuiltInKeywordListener.keywordRegexp = "^($XRAY_KEYWORD|$XRAY_UPDATE_KEYWORD).*\$".toRegex()
    }
}