package com.mycompany.pomodoro.Recycler

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.mycompany.pomodoro.Timer.Timer
import com.mycompany.pomodoro.Timer.TimerListener
import com.mycompany.pomodoro.databinding.TimerViewBinding

class TimerAdapter: ListAdapter<Timer, TimerViewHolder>(itemComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = TimerViewBinding.inflate(layoutInflater, parent, false)
        return TimerViewHolder(binding, parent.context as TimerListener, )
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private companion object {

        private val itemComparator = object : DiffUtil.ItemCallback<Timer>() {

            override fun areItemsTheSame(oldItem: Timer, newItem: Timer): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Timer, newItem: Timer): Boolean {
                return oldItem.msLeft == newItem.msLeft &&
                        oldItem.isStarted == newItem.isStarted
            }
            override fun getChangePayload(oldItem: Timer, newItem: Timer) = Any()

        }
    }
}