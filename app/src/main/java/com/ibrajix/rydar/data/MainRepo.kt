package com.ibrajix.rydar.data

import com.ibrajix.rydar.network.ApiDataSource
import com.ibrajix.rydar.response.SearchPlaceResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class MainRepo @Inject constructor(private val apiDataSource: ApiDataSource) : BaseDataSource() {

    //search for location
    suspend fun searchLocation(input: String) : Flow<Resource<SearchPlaceResponse>> {
        return flow {
            val result = safeApiCall { apiDataSource.searchLocation(input) }
            emit(result)

        }.flowOn(Dispatchers.IO)

    }

}