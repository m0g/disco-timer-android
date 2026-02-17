package com.anonymous.discotimer.data

data class TimerState(
    val work: Int = 40,
    val cycles: Int = 3,
    val sets: Int = 2,
    val prepare: Int = 0,
    val currentTime: Int = 0,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val isMuted: Boolean = false,
    val isPreparing: Boolean = false,
    val prepareTimeRemaining: Int = 0
) {
    val totalTime: Int
        get() = work * cycles * sets

    val totalIntervals: Int
        get() = cycles * sets

    val remainingTime: Int
        get() = totalTime - currentTime

    val currentWorkTime: Int
        get() {
            if (totalIntervals == 0) return 0

            val intervals = List(totalIntervals) { work }
            var localValue = work

            intervals.foldIndexed(0) { _, acc, value ->
                if (acc + value >= remainingTime && acc < remainingTime) {
                    localValue = remainingTime - acc
                }
                acc + value
            }

            return localValue
        }

    val currentSet: Int
        get() {
            var set = 1
            for (i in 1..totalIntervals) {
                if (i * work >= remainingTime && totalIntervals > 0) {
                    set = (totalIntervals - i) / cycles + 1
                    break
                }
            }
            return set
        }

    val currentCycle: Int
        get() {
            var cycle = 1
            for (i in 1..totalIntervals) {
                if (i * work >= remainingTime && totalIntervals > 0) {
                    cycle = totalIntervals - cycles * currentSet - i + cycles + 1
                    break
                }
            }
            return cycle
        }
}
