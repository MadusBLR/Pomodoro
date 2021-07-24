package com.mycompany.pomodoro.Timer

interface TimerListener {
    fun start(id: Int)

    fun stop(id: Int)

    fun reset(id: Int)

    fun delete(id: Int)

}