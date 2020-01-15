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

package ai.tock.bot.xray

import ai.tock.bot.admin.dialog.DialogReportDAO
import ai.tock.bot.definition.BotDefinitionBase
import ai.tock.bot.engine.BotBus
import ai.tock.bot.xray.XrayKeywords.XRAY_KEYWORD
import ai.tock.bot.xray.XrayKeywords.XRAY_UPDATE_KEYWORD
import ai.tock.shared.error
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.instance
import mu.KotlinLogging

object XrayKeywordHandler {
    private val logger = KotlinLogging.logger {}
    private val dialogReportDAO: DialogReportDAO by KodeinInjector().instance()

    private fun BotBus.createXRay(keyword: String) {
        val params = keyword.replace(XRAY_KEYWORD, "").split(",")
        val labelPlanMap = mapOf(
                "Stories_Complexes" to "JARVISFT-864",
                "Stories_Simples" to "JARVISFT-863",
                "TGVMax" to "JARVISFT-911",
                "Profil" to "JARVISFT-905"
        )
        val xray =
                try {
                    val dialog = dialogReportDAO.getDialog(dialog.id)!!
                    val linkedJira = params.getOrNull(1)?.trim()
                    val connectorType = dialog.actions.mapNotNull { it.connectorType }.lastOrNull()
                    val connectorName = ""
                    //val connectorName = when (connectorType) {
                    //    messengerConnectorType -> "[Messenger]"
                    //    gaConnectorType ->
                    //        if (isVoice)
                    //            "[Google Home]" else "[Google Assistant]"
                    //    vscConnectorType -> "[Oui]"
                    //    else -> null
                    //}
                    val testTitle = { labels: List<String> ->
                        val l = labels.filter { labelPlanMap.containsKey(it) }
                        val labelLink = if (l.isEmpty()) "" else "[${l.first()}]"
                        "${linkedJira?.replace("JARVISFT-", "")} – [AUTO]$connectorName$labelLink " +
                                (params.getOrNull(0)?.run {
                                    if (isBlank()) {
                                        null
                                    } else {
                                        trim()
                                    }
                                } ?: "Test")
                    }
                    XrayService().generateXrayTest(
                            dialog,
                            testTitle,
                            linkedJira,
                            //listOfNotNull(
                            //        when (connectorType) {
                            //            messengerConnectorType -> "JARVISFT-857"
                            //            gaConnectorType ->
                            //                if (isVoice)
                            //                    "JARVISFT-859" else "JARVISFT-861"
                            //            vscConnectorType -> "JARVISFT-862"
                            //            else -> null
                            //        }
                            //),
                            emptyList(),
                            labelPlanMap
                    )
                } catch (e: Exception) {
                    logger.error(e)
                    null
                }
        BotDefinitionBase.endTestContextKeywordHandler(this, false)
        nextUserActionState = null
        //endRawText(if (xray == null) "Error during xray creation" else "New Jira XRay: ${if (targetConnectorType == gaConnectorType) "" else "https://jira.vsct.fr/projects/JARVISFT/issues/"}${xray.key}")
        endRawText("Création")
    }

    private fun BotBus.updateXray(keyword: String) {
        val params = keyword.replace(XRAY_UPDATE_KEYWORD, "")
        val testKey = params.trim()
        val xray =
                try {
                    val dialog = dialogReportDAO.getDialog(dialog.id)!!
                    XrayService().updateXrayTest(dialog, testKey)
                } catch (e: Exception) {
                    logger.error(e)
                    null
                }
        BotDefinitionBase.endTestContextKeywordHandler(this, false)
        nextUserActionState = null
        //endRawText(if (xray == null) "Error during xray update" else "Jira XRay: ${if (targetConnectorType == gaConnectorType) "" else "https://jira.vsct.fr/projects/JARVISFT/issues/"}$testKey")
        endRawText("Mise à jour")
    }
}