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

val url = "http://mobile-courses-server.herokuapp.com/"
val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addJacksonConverter(mapper) // the TOCK Jackson mapper, see Jackson.kt
        .build()

val service = retrofit.create(RestApiService::class.java!!)

val request = service.listCourses()

val map = ConcurrentHashMap<String, List<Course>?>()

val bot = newBot(
        apiKey,
        newStory("greetings") {
            end("Hello $message")
            //!!!! not used
        },
        newStory("challenge") {
            entities.clear()
            if  ( map.isEmpty() ) {
                println("Calling API")
                request.enqueue(object : Callback<List<Course>> {
                    override fun onResponse(call: Call<List<Course>>, response: Response<List<Course>>) {
                        val results = response.body()
                        println(results)
                        println(userId.id)
                        map.putIfAbsent(userId.id, results)
                        println(map)
                        println("End Called API")

                    }
                    override fun onFailure(call: Call<List<Course>>, t: Throwable) {
                        error("KO")
                    }
                })
            }
            println(map)
            val age = entityText("age")
            var ageval = "String"
            when (age) {
                "enfant" -> ageval = "child"
                "adulte" -> ageval = "adult"
                else -> ageval = "agval"
            }
            end(
                    newCard(
                            "Prêt a commencer le test?",
                            "mode $ageval",
                            newAttachment("http://www.carnot-blossac.fr/wp-content/uploads/2019/05/download.png"),
                            newAction("C'est Parti"),
                            newAction("C'est Parti", "https://doc.tock.ai/")
                    )
            )
        },
        newStory("startchallenge") {
            //initialisation e l'entité qui persiste l'index de la liste pour connaitre la question a poser
            val counter = entityText("counter")

            val indexCourse = (entities.find { it.role == "counter" }?.apply { value = NumberValue((value as NumberValue).value.toInt() + 1) }?.value as? NumberValue)?.value ?: 0
            if(indexCourse == 0) {
                entities.add( Entity(type="test", role="counter", value = NumberValue(0), new = true, content = "counter") )
            }
            println(indexCourse.toInt())
            println(map)
            val myList = map.getValue(userId.id)
            if ( indexCourse.toInt() < myList?.size!!){

                val c = myList?.get(indexCourse.toInt())

                end(
                        newCard(
                                "${c?.title}",
                                "${c?.time}",
                                newAttachment("${c?.img}"),
                                newAction("Action1"),
                                newAction("Tock", "https://doc.tock.ai")
                        )
                )
            }
            else{
                removeEntity(role="counter")
                end("Le questionnaire est terminé. Merci de votre participation")
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
