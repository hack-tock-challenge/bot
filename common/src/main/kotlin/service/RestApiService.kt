package ai.tock.demo.common.service

import ai.tock.demo.common.Course
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RestApiService {
    @GET("/courses")
    fun listCourses(): Call<List<Course>>
}

