package com.udhaardaar.mvp

/** Canonical in-process event payload shared by all ArthSaathi V6.2 modules. */
data class V62Event(
    val type: String,
    val entityId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
)