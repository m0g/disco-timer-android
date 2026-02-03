package com.anonymous.discotimer.utils

object TimeFormatter {
    fun formatSeconds(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    fun humanizeDuration(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return buildString {
            if (hours > 0) {
                append("$hours hour")
                if (hours > 1) append("s")
                if (minutes > 0 || secs > 0) append(", ")
            }
            if (minutes > 0) {
                append("$minutes minute")
                if (minutes > 1) append("s")
                if (secs > 0) append(", ")
            }
            if (secs > 0 || (hours == 0 && minutes == 0)) {
                append("$secs second")
                if (secs != 1) append("s")
            }
        }
    }
}
