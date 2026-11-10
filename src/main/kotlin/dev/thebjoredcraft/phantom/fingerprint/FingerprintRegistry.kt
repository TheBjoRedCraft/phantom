package dev.thebjoredcraft.phantom.fingerprint

import java.util.*
import java.util.concurrent.ConcurrentHashMap

object FingerprintRegistry {
    private val retired = ConcurrentHashMap.newKeySet<UUID>()

    fun retire(vararg ids: UUID?) = ids.filterNotNull().forEach { retired += it }
    fun isRetired(id: UUID) = id in retired
}