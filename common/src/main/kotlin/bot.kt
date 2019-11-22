/*
 * Copyright (C) 2017/2019 VSCT
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

package ai.tock.demo.common

import ai.tock.bot.api.client.ClientBus
import ai.tock.bot.api.client.newBot
import ai.tock.bot.api.client.newStory
import ai.tock.bot.api.client.unknownStory
import ai.tock.bot.api.model.context.Entity
import ai.tock.bot.connector.web.webButton
import ai.tock.bot.connector.web.webMessage
import ai.tock.bot.definition.Intent
import ai.tock.shared.property
import com.fasterxml.jackson.databind.DeserializationFeature
import retrofit2.Retrofit
import ai.tock.demo.common.service.RestApiService
import ai.tock.nlp.entity.NumberValue
import ai.tock.shared.addJacksonConverter
import ai.tock.shared.jackson.mapper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ai.tock.nlp.entity.Value
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.typeOf

val apiKey = property("tock_bot_api_key", "1e8af066-4872-4f1c-addc-151d8a13ea3c")

//val url = "http://mobile-courses-server.herokuapp.com/"
val url = "https://f1f659e2.ngrok.io/"

val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addJacksonConverter(mapper) // the TOCK Jackson mapper, see Jackson.kt
        .build()

val service = retrofit.create(RestApiService::class.java!!)

val map = ConcurrentHashMap<String, List<Question>?>()

val bot = newBot(
        apiKey,
        newStory("greetings") {
            println(userId.id)
            entities.clear()
            map.clear()
            end(
                    newCard(
                            "Bonjour, je suis Quizzy :)",
                            "Les Office de Tourisme te proposent des questions sur ton parcours !",
                            newAttachment("https://zupimages.net/up/19/47/lern.png"),
                            newAction("Mode adulte"),
                            newAction("Mode Famille")
                    )
            )

        },
        newStory("challenge") {
            val age = entityText("age")
            var ageval = "String"
            when (age) {
                "enfant" -> ageval = "child"
                "adulte" -> ageval = "adult"
                else -> ageval = "any!!!Warning"
            }
            println(ageval)
            println(map)

            println("Calling API")
            service.listQuestion().enqueue(object : Callback<List<Question>> {
                override fun onResponse(call: Call<List<Question>>, response: Response<List<Question>>) {
                    val results = response.body()
                    println(results)
                    println(userId.id)
                    map.putIfAbsent(userId.id, results)
                    println(map)
                    println("End Called API")
                    val multiPlayer = entityText("multiPlayer")
                    if ( multiPlayer.isNullOrEmpty() )
                        end(
                                newCard(
                                        "Seul ou contre un autre voyageur?",
                                        "mode $ageval",
                                        newAttachment("https://zupimages.net/up/19/47/tnmi.png"),
                                        newAction("solo"),
                                        newAction("duo")
                                )
                        )
                    else {
                        end("Mode $multiPlayer")
                    }

                }
                override fun onFailure(call: Call<List<Question>>, t: Throwable) {
                    //send("Erreur Technique: Désolé, je n'ai pas pu vous proposer de question, merci de reessayer plus tard.")
                    error("KO")
                }
            })
        },
        newStory("challengesolo") {
            //cleanup entities
            val multiPlayer = entityText("multiPlayer")
            end(
                    newCard(
                            "Prêt a commencer le test?",
                            "",
                            newAttachment("https://zupimages.net/up/19/47/tnmi.png"),
                            newAction("C'est Parti")
                    )
            )
        },
        newStory("challengemulti") {
            val multiPlayer = entityText("multiPlayer")
            end(
                    newCard(
                            "Prêt a commencer le test?",
                            "",
                            newAttachment("https://zupimages.net/up/19/47/tnmi.png"),
                            newAction("C'est Parti")
                    )
            )
        },
        newStory("startchallenge") {
            //initialisation e l'entité qui persiste l'index de la liste pour connaitre la question a poser
            val counter = entityText("counter")

            val indexCourse = majListIndex()

            println(indexCourse.toInt())
            println(map)
            val myList = map.getValue(userId.id)
            if ( indexCourse.toInt() < myList?.size!!){

                val c = myList?.get(indexCourse.toInt())
                val ch1= c?.choices?.get(0)?.text
                val ch2= c?.choices?.get(1)?.text
                val ch3= c?.choices?.get(2)?.text

                if ( c?.type == "choices" ){
                    //On retourne une carte a actions
                    send (
                            newCard(
                            "$c?.messages[0]",
                            "$c?.messages[1]",
                            null,
                            newAction("$ch1"),
                            newAction("$ch2"),
                            newAction("$ch3")
                            )
                    )
                }
                else if (c.image.isNullOrEmpty()){
                    for (item in c?.messages) {
                        send(item)
                    }
                }
                else {
                    newCard(
                            c?.messages[0],
                            c?.messages[1],
                            newAttachment(c?.image)
                    )
                }

                end(
                )
            }
            else{
                removeEntity(role="counter")
                end("Le questionnaire est terminé. Merci de votre participation")
            }
        },
        newStory("reponse") {

            //recuperer la question courante
            val counter = entityValue<NumberValue>("counter")
            println(counter)
            println(counter?.value)
            val myList = map.getValue(userId.id)
            val c = counter?.value?.toInt()?.let { myList?.get(it) }

            //recuperer l'entité type de la reponse de l'utilisateur
            val entityType = c?.type?.let { entityText(it) }
            println(entityType)
            //test de la reponse

            if ( c?.answer ==  entityType ){
                if (c != null) {
                    end(
                            newCard(
                                    c.congratMessage,null, null,
                                    newAction("Question suivante")
                            )
                    )
                }
            }
            else{
                end(
                        newCard(
                            "reponse attendue: ${c?.answer}",
                            "votre réponse: $entityType",
                                null,
                                newAction("Question suivante")
                        )
                )
            }
        },
        newStory("card") {
            //cleanup entities
            val test = entityText("location")
            entities.clear()
            end(
                newCard(
                    test ?: "Hey",
                    "Where are you going?",
                    newAttachment("https://upload.wikimedia.org/wikipedia/commons/2/22/Heckert_GNU_white.svg"),
                    newAction("Action1"),
                    newAction("Tock", "https://doc.tock.ai")
                )
            )
        },
        newStory("carousel") {
            end(
                newCarousel(
                    listOf(
                        newCard(
                            "Card 1",
                            null,
                            newAttachment("https://upload.wikimedia.org/wikipedia/commons/2/22/Heckert_GNU_white.svg"),
                            newAction("Action1"),
                            newAction("Tock", "https://doc.tock.ai")
                        ),
                        newCard(
                            "Card 2",
                            null,
                            newAttachment("https://doc.tock.ai/fr/images/header.jpg"),
                            newAction("Action1"),
                            newAction("Tock", "https://doc.tock.ai")
                        )
                    )
                )
            )
        },
        unknownStory {
            end {
                //custom model sample
                webMessage(
                    "Sorry - not understood",
                    webButton("Card", Intent("card")),
                    webButton("Carousel", Intent("carousel"))
                )
            }
        }
)

private fun ClientBus.majListIndex(): Number {
    val indexCourse = (entities.find { it.role == "counter" }?.apply { value = NumberValue((value as NumberValue).value.toInt() + 1) }?.value as? NumberValue)?.value
            ?: 0
    if (indexCourse == 0) {
        entities.add(Entity(type = "test", role = "counter", value = NumberValue(0), new = true, content = "counter"))
    }
    return indexCourse
}
