package com.ibrajix.rydar.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LocationDirectionModel(
    var fromPoint1: Double? = null,
    var fromPoint2: Double? = null,
    var toPoint1: Double? = null,
    var toPoint2: Double? = null,
    var midPoint1: Double? = null,
    var midPoint2: Double? = null
) : Parcelable