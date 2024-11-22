package com.example.tripsetgo
data class PhotoCapsule(
    val id: String = "",
    val name: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    var isLocked: Boolean = false,
    var unlockTime: Long = 0,
    val ownerId: String = "",
    val photoCount: Int = 0
) {
    fun isUnlockTimeReached(): Boolean {
        return System.currentTimeMillis() >= unlockTime
    }

    fun isAccessible(): Boolean {
        return !isLocked || isUnlockTimeReached()
    }

    fun getRemainingTime(): String {
        if (!isLocked || isUnlockTimeReached()) return "Unlocked"

        val remaining = unlockTime - System.currentTimeMillis()
        val days = remaining / (24 * 60 * 60 * 1000)
        val hours = (remaining % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)

        return when {
            days > 0 -> "$days days, $hours hours remaining"
            hours > 0 -> "$hours hours remaining"
            else -> "Less than an hour remaining"
        }
    }
}