package com.mycompany.pomodoro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.*
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.mycompany.pomodoro.Recycler.TimerAdapter
import com.mycompany.pomodoro.Timer.Timer
import com.mycompany.pomodoro.Timer.TimerListener
import com.mycompany.pomodoro.databinding.ActivityMainBinding
import com.mycompany.pomodoro.databinding.DialogBinding
class MainActivity : AppCompatActivity(), TimerListener,LifecycleObserver  {

    private lateinit var binding: ActivityMainBinding
    private val timerAdapter = TimerAdapter()
    private val timers = mutableListOf<Timer>()
    private var startedTimerID = -1
    private var startedTimerIndex = -1
    private var id = 0
    private var timer: CountDownTimer? = null
    private var mins: Long?=0
    private var secs: Long?=0
    private var total: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = timerAdapter
        }

        binding.floatingActionButton.setOnClickListener {

            val mDialogView = DialogBinding.inflate(layoutInflater)
            mins = null
            secs = null
            mDialogView.minutes.doOnTextChanged { text, _, _, _ ->
                if(text!=null&&text!!.length>1&&(text[0]=='0'||text.toString().toLong()>1440)) {
                    mDialogView.minutes.setText(text.subSequence(1, text.length))
                    mDialogView.minutes.setSelection(text.lastIndex)
                }
                mins = checkNumber(text.toString())
            }

            mDialogView.seconds.doOnTextChanged { text, _, _, _ ->
                if(text!!.length>1&&(text[0]=='0'||text.toString().toLong()>60)) {
                    mDialogView.seconds.setText(text.subSequence(1, text.length))
                    mDialogView.seconds.setSelection(text.lastIndex)
                }
                secs = checkNumber(text.toString())
            }
            val builder =  AlertDialog.Builder(this)
            builder.setTitle("Enter your time")
            builder.setView(mDialogView.root)
            builder.setIcon(R.drawable.ic_baseline_av_timer_24)
            builder.setNegativeButton("Cancel",null)
            builder.setPositiveButton("Add") {
                    _, _ ->
                if(mins==null&&secs==null)
                {
                    Toast.makeText(this, "Incorrect input", Toast.LENGTH_LONG).show()
                }
                else
                {
                    total = mins?:0
                    total*=60
                    total+=secs?:0
                    total*=1000
                    timers.add(Timer(id++, total, total, false, false))
                    timerAdapter.submitList(timers.toList())
                }

            }
            builder.show()
        }
    }
    private fun checkNumber(text: String): Long?{
        return if(text!="")
            text.toLong()
        else
            null
    }
    override fun start( id: Int) {

        if(startedTimerID!=-1)
            stop(startedTimerID)

        startedTimerID = id
        startedTimerIndex=-1
        while(timers[++startedTimerIndex].id!=id);
        timers[startedTimerIndex].isStarted = true

        timer = object : CountDownTimer( timers[startedTimerIndex].msLeft, 200L) {
            override fun onTick(millisUntilFinished: Long) {
                timers[startedTimerIndex] = timers[startedTimerIndex].copy(msLeft = millisUntilFinished)
                timerAdapter.submitList(timers.toList())}
            override fun onFinish() {
                timers[startedTimerIndex].isFinished = true
                stop(id)
            }
        }

        timer!!.start()
    }

    override fun stop(id:Int) {
        if(id==startedTimerID) {

            timer!!.cancel()
            timers[startedTimerIndex] = timers[startedTimerIndex].copy(isStarted = false)
            timerAdapter.submitList(timers.toList())

            startedTimerID = -1
            startedTimerIndex = -1

        }
    }

    override fun reset(id: Int) {
        var i: Int = -1
        while(timers[++i].id!=id);

        if(id==startedTimerID) {
            timer!!.cancel()
            timers[i].isStarted = false
            timers[i].isFinished = false
            startedTimerID = -1
            startedTimerIndex = -1
        }
        timers[i].isFinished = false
        timers[i] = timers[i].reset()
        timerAdapter.submitList(timers.toList())
    }

    override fun delete(id: Int) {
        if(id==startedTimerID) {
            timer!!.cancel()
            startedTimerID = -1
            timers.removeAt(startedTimerIndex)
            startedTimerIndex = -1
        }
        else
        {
            if(startedTimerIndex>timers.indexOf(timers.find{it.id==id}))
                startedTimerIndex--
        }
        timers.remove(timers.find { it.id == id })
        timerAdapter.submitList(timers.toList())
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        if(startedTimerID!=-1) {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(STARTED_TIMER_MS, timers[startedTimerIndex].msLeft)
            startIntent.putExtra("START_SYSTEM_MS", SystemClock.elapsedRealtime())

            startService(startIntent)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
            val stopIntent = Intent(this, ForegroundService::class.java)
            stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
            startService(stopIntent)

    }

    override fun onDestroy() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
        super.onDestroy()
    }


}