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

class ParallelForI(private val NUMBER_OF_THREAD: Int, private val NAME: String) {
    private val executorService: ExecutorService
    private val lock: Lock = ReentrantLock()
    private val condition: Condition = lock.newCondition()
    
    private val finished: Array<AtomicBoolean>
    
    fun isFinished(): Boolean {
        // if one of them is not finished
        // haven't finished
        // all finished
        for (i in 0 until NUMBER_OF_THREAD) {
            if (!finished[i].get()) {
                return false
            }
        }
        return true
    }
    
    init {
        executorService = Executors.newFixedThreadPool(NUMBER_OF_THREAD) { r -> Thread(r, NAME) }
        finished = Array(NUMBER_OF_THREAD) {AtomicBoolean(true)}
        // true is better for waitForLastRun before any run.
    }
    
    fun waitForLastRun() {
        lock.lock()
        // synchronized outside the loop so other thread can't notify when it's not waiting
        var i = 0
        try {
            while (i < NUMBER_OF_THREAD) {
                if (!finished[i].get()) {
                    i = -1
                    //                        Log.v(NAME, "waiting");
                    condition.await() //
                    //                        Log.v(NAME, "waked");
                
                
                }
                i++
            }
        } catch (e: InterruptedException) {
            Log.e(NAME, "How?! Why?!", e)
        } finally {
            lock.unlock()
        }
    }
    
    /*fun run(functionForI: (Int) -> Unit, MAX_I: Int) = runInlined(functionForI, MAX_I)
    private inline */fun run/*Inlined*/(/*crossinline*/ functionForI: (Int) -> Unit, MAX_I: Int) {
        for (finished in finished)
            finished.set(false) // just started
        for (i in 0 until NUMBER_OF_THREAD) {
            executorService.submit {
                val iInitial = i * MAX_I / NUMBER_OF_THREAD
                val iSmallerThan: Int
                if (i == NUMBER_OF_THREAD - 1)
                // last thread
                    iSmallerThan = MAX_I
                else
                    iSmallerThan = (i + 1) * MAX_I / NUMBER_OF_THREAD
                for (i1 in iInitial until iSmallerThan) {
                    try {
                        functionForI(i1)
                    } catch (e: Exception) {
                        val logger = Logger.getAnonymousLogger()
                        logger.log(Level.SEVERE, "an exception was thrown in $NAME", e)
                        throw e
                    }
                    
                }
                finished[i].set(true)
                try {
                    lock.lock()
                    condition.signal()
                    //                    Log.v(NAME, "notified");
                } finally {
                    lock.unlock()
                }
            }
        }
    }
}
