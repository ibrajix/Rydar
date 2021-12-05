package com.ibrajix.rydar.network

import javax.inject.Inject

class ApiDataSource @Inject constructor(private val apiService: ApiService) {

    //search location
    suspend fun searchLocation(input: String) = apiService.searchPlace(input = input)

}