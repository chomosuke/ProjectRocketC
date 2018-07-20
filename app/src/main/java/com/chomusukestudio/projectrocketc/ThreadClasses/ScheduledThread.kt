package com.chomusukestudio.projectrocketc.ThreadClasses

import com.chomusukestudio.projectrocketc.runWithExceptionChecked
import java.util.concurrent.Executors

class ScheduledThread(private val periodInMillisecond: Long, private val task: () -> Unit) {
    private val name = "Scheduled Thread with period of $periodInMillisecond millisecond."
    @Volatile private var running = false
    private val singleThread = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, name) }
    fun run() {
        if (running) {
            throw IllegalThreadStateException("$name is running")
        } else {
            running = true
        }
        singleThread.submit {
            runWithExceptionChecked {
                while (running) {
                    task()
                    Thread.sleep(periodInMillisecond)
                }
            }
        }
    }

    fun pause() {
        running = false
    }
}