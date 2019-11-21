package ai.tock.demo.common

import retrofit2.Call
import retrofit2.http.GET

data class Question(

        val id: String?,
        val title: String?,
        val module: String?,
        val time: String?,
        val img: String?
) {


    interface CoursesService {
        @GET("/courses")
        fun listCourses(): Call<List<Question>>
    }
}