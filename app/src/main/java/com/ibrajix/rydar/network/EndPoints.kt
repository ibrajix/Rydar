package com.ibrajix.rydar.network

import com.ibrajix.rydar.BuildConfig

class EndPoints {

    companion object{

        //BASE
        const val BASE_URL = "https://maps.googleapis.com/maps/api/"

        //GET LOCATION FROM SEARCH QUERY
        const val SEARCH_LOCATION = "place/findplacefromtext/json"

        //API KEY
        const val SEARCH_API_KEY = BuildConfig.SEARCH_API_KEY //--< Make sure you input your own api key.

    }

}