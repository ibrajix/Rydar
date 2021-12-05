package com.ibrajix.rydar.network


import com.ibrajix.rydar.network.EndPoints.Companion.SEARCH_API_KEY
import com.ibrajix.rydar.response.SearchPlaceResponse
import retrofit2.Response
import retrofit2.http.*


@JvmSuppressWildcards
interface ApiService {

    //seller login
    @GET(EndPoints.SEARCH_LOCATION)
    suspend fun searchPlace(
        @Query("fields") fields: String = "formatted_address,name,geometry",
        @Query("input") input: String,
        @Query("inputtype") inputtype: String = "textquery",
        @Query("key") key: String = SEARCH_API_KEY)
    : Response<SearchPlaceResponse>

}