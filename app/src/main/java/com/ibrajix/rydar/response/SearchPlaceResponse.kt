package com.ibrajix.rydar.response

data class SearchPlaceResponse(
    val candidates: List<Candidate>,
    val status: String
)