package dev.henkle.trees.ad

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Filter(val trigger: Trigger)

@Serializable
data class Trigger(@SerialName("url-filter") val urlFilter: String)