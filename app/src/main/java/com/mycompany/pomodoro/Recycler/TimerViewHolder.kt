package com.mycompany.pomodoro.Recycler

import android.graphics.drawable.AnimationDrawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.pomodoro.Timer.Timer
import com.mycompany.pomodoro.*
import com.mycompany.pomodoro.Timer.TimerListener
import com.mycompany.pomodoro.databinding.TimerViewBinding


class TimerViewHolder(
    private val binding: TimerViewBinding,
    private val listener: TimerListener,
): RecyclerView.ViewHolder(binding.root)
{


    fun bind(timer: Timer)
    {

            binding.stopwatchTimer.text = timer.msLeft.getTime()
            binding.progressCircle.setPeriod(timer.startTime)
            binding.progressCircle.setCurrent(timer.msLeft)
        if (timer.isStarted) {
                startTimer(timer)
            } else {
                stopTimer(timer)
            }
        binding.playButton.setOnClickListener {
            if (timer.isStarted) {
                listener.stop(timer.id)
            } else {
                listener.start(timer.id)
            }
        }
        binding.resetButton.setOnClickListener {
            stopTimer(timer)
            listener.reset(timer.id)
        }
        binding.deleteButton.setOnClickListener {
            binding.playButton.isClickable = false
            binding.resetButton.isClickable = false
            binding.deleteButton.isClickable = false
            listener.delete(timer.id)
        }
    }

    private fun startTimer(t: Timer) {
        binding.playButton.text="Stop"
        binding.indicator.visibility = View.VISIBLE
       (binding.indicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer(t: Timer) {
        binding.progressCircle.setCurrent(0)
        binding.playButton.text="Start"
        binding.indicator.visibility = View.INVISIBLE
       (binding.indicator.background as? AnimationDrawable)?.stop()
        if(t.isFinished)
        {
            binding.stopwatchTimer.text = "Finished!"
            binding.playButton.isEnabled = false
        }
        else
        {
            binding.playButton.isEnabled = true
        }
    }



    private companion object{
        private const val START_TIME = "00:00:00"
    }
}