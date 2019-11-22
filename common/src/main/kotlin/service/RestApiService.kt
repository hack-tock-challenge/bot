package ai.tock.demo.common.service

import ai.tock.demo.common.Question
import retrofit2.Call
import retrofit2.http.GET

interface RestApiService {
    @GET("/question/adult-1")
    fun listQuestion(): Call<List<Question>>
}

