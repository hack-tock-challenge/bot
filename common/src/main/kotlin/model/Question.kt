package ai.tock.demo.common

import ai.tock.demo.common.model.Choice
import retrofit2.Call
import retrofit2.http.GET

data class Question(


        /*
        "answer": 750,
        "points": 10,
        "congratMessage": "Bien joué ! Environ 750 trains partent chaque jour de Montparnasse, répartis sur 28 voies de circulation.",
        "difficulty": "adult",
        "hints": [
            "Moins de 850 trains. Allez, tu vas y arriver !",
            "Tu ne fais aucun effort... Je vais tout de même t'aider : c'est entre 500 et 800 trains par jours."
        ],
        "locationStation": "Paris Montparnasse",
        "messages": [
            "C'est parti !",
            "Tu quittes Montparnasse mais sais-tu, à une centaine près, combien de trains partent chaque jour de cette gare ?"
        ],
        "order": 10,
        "type": "number",
        "pushTime": 0
         */
        val answer: String?,
        val points: String?,
        val congratMessage: String?,
        val difficulty: String?,
        val hints: List<String?>,
        val locationStation: String?,
        val messages: List<String>,
        val order: String?,
        val type: String?,
        val pushTime: String?,
        val image: String?,
        val choices: List<Choice>?

) {



}