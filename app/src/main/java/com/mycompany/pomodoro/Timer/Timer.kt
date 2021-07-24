package com.mycompany.pomodoro.Timer

data class Timer(val id: Int, var startTime: Long,var msLeft: Long,var isStarted: Boolean, var isFinished : Boolean)
{
    fun reset():Timer { return this.copy(msLeft = startTime) }
}
