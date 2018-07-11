package com.chomusukestudio.projectrocketc.ThreadClasses


import android.util.Log

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.logging.Level
import java.util.logging.Logger

// for inlining purposes everything have to be public, i'll just use naming convention
class ParallelForI(val __numberOfThread: Int, val __name: String) {
    val __executorService: ExecutorService
    val __lock: Lock = ReentrantLock()
    val __condition: Condition = __lock.newCondition()
    
    val __finished: Array<AtomicBoolean>
    
    fun isFinished(): Boolean {
        // if one of them is not finished
        // haven't finished
        // all finished
        for (i in 0 until __numberOfThread) {
            if (!__finished[i].get()) {
                return false
            }
        }
        return true
    }
    
    init {
        __executorService = Executors.newFixedThreadPool(__numberOfThread) { r -> Thread(r, __name) }
        __finished = Array(__numberOfThread) {AtomicBoolean(true)}
        // true is better for waitForLastRun before any run.
    }
    
    fun waitForLastRun() {
        __lock.lock()
        // synchronized outside the loop so other thread can't notify when it's not waiting
        var i = 0
        try {
            while (i < __numberOfThread) {
                if (!__finished[i].get()) {
                    i = -1
                    //                        Log.v(__name, "waiting");
                    __condition.await() //
                    //                        Log.v(__name, "waked");
                }
                i++
            }
        } catch (e: InterruptedException) {
            Log.e(__name, "How?! Why?!", e)
        } finally {
            __lock.unlock()
        }
    }

    inline fun run(crossinline functionForI: (Int) -> Unit, MAX_I: Int) {
        for (finished in __finished)
            finished.set(false) // just started
        for (i in 0 until __numberOfThread) {
            __executorService.submit {
                val iInitial = i * MAX_I / __numberOfThread
                val iSmallerThan: Int
                if (i == __numberOfThread - 1)
                    // last thread
                    iSmallerThan = MAX_I
                else
                    iSmallerThan = (i + 1) * MAX_I / __numberOfThread
                for (i1 in iInitial until iSmallerThan) {
                    try {
                        functionForI(i1)
                    } catch (e: Exception) {
                        val logger = Logger.getAnonymousLogger()
                        logger.log(Level.SEVERE, "an exception was thrown in $__name", e)
                        throw e
                    }
                    
                }
                __finished[i].set(true)
                try {
                    __lock.lock()
                    __condition.signal()
                    //                    Log.v(__name, "notified");
                } finally {
                    __lock.unlock()
                }
            }
        }
    }
}
