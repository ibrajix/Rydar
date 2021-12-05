package com.ibrajix.rydar.response

data class Candidate(
    val formatted_address: String,
    val geometry: Geometry,
    val name: String,
    val rating: Double
)