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

import ai.tock.bot.admin.dialog.ActionReport
import ai.tock.bot.admin.dialog.DialogReport
import ai.tock.bot.admin.dialog.DialogReportDAO
import ai.tock.bot.definition.BotDefinitionBase
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.message.Sentence
import ai.tock.bot.xray.XrayKeywords.XRAY_KEYWORD
import ai.tock.bot.xray.XrayKeywords.XRAY_UPDATE_KEYWORD
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.property
import ai.tock.shared.provide
import mu.KotlinLogging

class XrayKeywordHandler {
    val logger = KotlinLogging.logger {}
    val dialogReportDAO: DialogReportDAO = injector.provide()
    val jiraKeyProject = property("tock_bot_test_jira_project", "Set a key for the jira project.")

    internal fun createXray(keyword: String, bus: BotBus) {
        val params = keyword.replace(XRAY_KEYWORD, "").split(",")
        val labelPlanMap = mapOf(
                "Stories_Complexes" to "JARVISFT-864",
                "Stories_Simples" to "JARVISFT-863",
                "TGVMax" to "JARVISFT-911",
                "Profil" to "JARVISFT-905"
        )
        val xray =
                try {
                    val dialog = dialogReportDAO.getDialog(bus.dialog.id)!!.cleanSurrogates()
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
                        "${linkedJira?.replace("$jiraKeyProject-", "")} – [AUTO]$connectorName$labelLink " +
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
                            listOfNotNull(""),
                            labelPlanMap
                    )
                } catch (e: Exception) {
                    logger.error(e)
                    null
                }
        BotDefinitionBase.endTestContextKeywordHandler(bus, false)
        bus.nextUserActionState = null
        if (xray != null) {
            bus.endRawText("Xray issue created : ${xray.key}")
        } else {
            bus.endRawText("Error during issue creation")
        }
    }

    internal fun updateXray(keyword: String, bus: BotBus) {
        val params = keyword.replace(XRAY_UPDATE_KEYWORD, "")
        val testKey = params.trim()
        val xray =
                try {
                    val dialog = dialogReportDAO.getDialog(bus.dialog.id)!!.cleanSurrogates()
                    XrayService().updateXrayTest(dialog, testKey)
                } catch (e: Exception) {
                    logger.error(e)
                    null
                }
        BotDefinitionBase.endTestContextKeywordHandler(bus, false)
        bus.nextUserActionState = null
        if (xray != null) {
            bus.endRawText("Xray issue updated : $testKey")
        } else {
            bus.endRawText("Error during update of issue $testKey")
        }
    }

    private fun DialogReport.cleanSurrogates(): DialogReport {
        var cleanedActions: MutableList<ActionReport> = mutableListOf()

        this.actions.forEachIndexed {index, currentAction ->
            var currentActionMessage = currentAction.message.toPrettyString()
            if(currentAction.message.isSimpleMessage()) {
                currentActionMessage.forEach { c ->
                    if (c.isSurrogate()) {
                        currentActionMessage = currentActionMessage.replace("$c", "")
                    }
                }
                cleanedActions.add(currentAction.copy(message = Sentence(currentActionMessage)))
            }
        }
        return DialogReport(cleanedActions, this.userInterface, this.id)
    }
}