package com.ibrajix.rydar.data

import com.google.gson.Gson
import com.ibrajix.rydar.response.AuthResponse
import retrofit2.Response


abstract class BaseDataSource {

    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Resource<T> {

        try {
            val response = apiCall()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    return Resource.success(body)
                }
            }

            else{
                val message: AuthResponse = Gson().fromJson(response.errorBody()!!.charStream(), AuthResponse::class.java)
                return Resource.error(message.message)
            }

            return Resource.failed("Something went wrong, try again")

        } catch (e: Exception) {
            return Resource.failed(e.toString())
        }
    }

}