package com.example.beato

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.*
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.*
import com.example.beatmakingapp.R
import java.util.*

class PatternActivity : Activity() {
    var state_playing = false
    var state_recording = false
    var timeAtStart: Long = 0
    var timeSinceStart: Long = 0
    var timeAtLastBeat: Long = 0
    var timeSinceLastBeat: Long = 0
    var currentPlaybackThread = true
    var bpm = 120
    var bars = 4
    var check = 0
    var beatInBar = 0
    var soundIndex = 0
    var frontQueue = PriorityQueue(1, Global.comp)
    private var patternId = -1
    var snapValue = 0.5
    var padIds = arrayOf(
        intArrayOf(
            R.id.pad_00,
            R.id.pad_01,
            R.id.pad_02,
            R.id.pad_03,
            R.id.pad_04,
            R.id.pad_05
        ),
        intArrayOf(R.id.pad_10, R.id.pad_11, R.id.pad_12, R.id.pad_13, R.id.pad_14, R.id.pad_15),
        intArrayOf(R.id.pad_20, R.id.pad_21, R.id.pad_22, R.id.pad_23, R.id.pad_24, R.id.pad_25),
        intArrayOf(R.id.pad_30, R.id.pad_31, R.id.pad_32, R.id.pad_33, R.id.pad_34, R.id.pad_35),
        intArrayOf(R.id.pad_40, R.id.pad_41, R.id.pad_42, R.id.pad_43, R.id.pad_44, R.id.pad_45),
        intArrayOf(R.id.pad_50, R.id.pad_51, R.id.pad_52, R.id.pad_53, R.id.pad_54, R.id.pad_55)
    )

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pattern_layout)
        val context: Context = this
        this.volumeControlStream = AudioManager.STREAM_MUSIC

        // Restore preferences
        buttonNames =
            getSharedPreferences(BUTTON_NAMES, 0)
        buttonSounds =
            getSharedPreferences(BUTTON_SOUNDS, 0)
        Global.patternContext = this
        if (Global.initialized === false) {
            Global.initialize()
            Global.initialized = true
        }
        val defValue = "loadDefault"
        var path: String?

        // Load default buttons
        path = buttonSounds?.getString("p_00", defValue)
        if (path != defValue) Global.soundIds[0][0] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_01", defValue)
        if (path != defValue) Global.soundIds[0][1] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_02", defValue)
        if (path != defValue) Global.soundIds[0][2] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_03", defValue)
        if (path != defValue) Global.soundIds[0][3] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_04", defValue)
        if (path != defValue) Global.soundIds[0][4] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_05", defValue)
        if (path != defValue) Global.soundIds[0][5] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_10", defValue)
        if (path != defValue) Global.soundIds[1][0] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_11", defValue)
        if (path != defValue) Global.soundIds[1][1] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_12", defValue)
        if (path != defValue) Global.soundIds[1][2] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_13", defValue)
        if (path != defValue) Global.soundIds[1][3] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_14", defValue)
        if (path != defValue) Global.soundIds[1][4] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_15", defValue)
        if (path != defValue) Global.soundIds[1][5] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_20", defValue)
        if (path != defValue) Global.soundIds[2][0] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_21", defValue)
        if (path != defValue) Global.soundIds[2][1] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_22", defValue)
        if (path != defValue) Global.soundIds[2][2] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_23", defValue)
        if (path != defValue) Global.soundIds[2][3] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_24", defValue)
        if (path != defValue) Global.soundIds[2][4] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_25", defValue)
        if (path != defValue) Global.soundIds[2][5] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_30", defValue)
        if (path != defValue) Global.soundIds[3][0] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_31", defValue)
        if (path != defValue) Global.soundIds[3][1] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_32", defValue)
        if (path != defValue) Global.soundIds[3][2] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_33", defValue)
        if (path != defValue) Global.soundIds[3][3] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_34", defValue)
        if (path != defValue) Global.soundIds[3][4] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_35", defValue)
        if (path != defValue) Global.soundIds[3][5] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_40", defValue)
        if (path != defValue) Global.soundIds[4][0] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_41", defValue)
        if (path != defValue) Global.soundIds[4][1] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_42", defValue)
        if (path != defValue) Global.soundIds[4][2] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_43", defValue)
        if (path != defValue) Global.soundIds[4][3] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_44", defValue)
        if (path != defValue) Global.soundIds[4][4] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_45", defValue)
        if (path != defValue) Global.soundIds[4][5] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_50", defValue)
        if (path != defValue) Global.soundIds[5][0] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_51", defValue)
        if (path != defValue) Global.soundIds[5][1] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_52", defValue)
        if (path != defValue) Global.soundIds[5][2] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_53", defValue)
        if (path != defValue) Global.soundIds[5][3] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_54", defValue)
        if (path != defValue) Global.soundIds[5][4] = Global.soundPool.load(path, 1)
        path = buttonSounds?.getString("p_55", defValue)
        if (path != defValue) Global.soundIds[5][5] = Global.soundPool.load(path, 1)

        // Set up an Audio Manager to Return Current Phone Volume State
        val audio = context
            .getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (audio.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> {
            }
            AudioManager.RINGER_MODE_SILENT, AudioManager.RINGER_MODE_VIBRATE -> AlertDialog.Builder(
                this
            ).setTitle("Phone in Silent Mode!")
                .setPositiveButton(android.R.string.yes, null).create()
                .show()
        }

        // Load graphics and pattern data to match active pattern
        var message: String? = ""
        val intent = intent
        message = intent.getStringExtra("msgFromParent")
        if (message == null) message = "default"
        val button =
            findViewById<View>(R.id.pattern_number_button) as Button
        if (message == "first" || message == "default" || message == "default : first") {
            patternId = 0
            bars = Global.pattern1Bars
            snapValue = Global.p1SnapValue
            button.text = "Pat-1"
            updateGradient("yellow")
        } else if (message == "second") {
            patternId = 1
            bars = Global.pattern2Bars
            snapValue = Global.p2SnapValue
            button.text = "Pat-2"
            updateGradient("green")
        } else if (message == "third") {
            patternId = 2
            bars = Global.pattern3Bars
            snapValue = Global.p3SnapValue
            button.text = "Pat-3"
            updateGradient("blue")
        } else if (message == "fourth") {
            patternId = 3
            bars = Global.pattern4Bars
            snapValue = Global.p4SnapValue
            button.text = "Pat-4"
            updateGradient("purple")
        }

        // Set sound playback volume
        val am =
            getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volume = am
            .getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()

        // update progress bar during pattern loop
        val progBar = findViewById<View>(R.id.progBar) as ProgressBar
        progBar.progress = 0

        // During each playback loop, run through thread, comparing the current
        playbackThread1 = Thread(Runnable {
            Looper.prepare()
            l1 = Looper.myLooper()
            val handler = Handler(Looper.myLooper()!!)
            handler.post(object : Runnable {
                override fun run() {
                    if (state_playing == true) {
                        val currentTime = SystemClock.elapsedRealtime()
                        timeSinceStart = currentTime - timeAtStart
                        timeSinceLastBeat = currentTime - timeAtLastBeat
                        if ((timeSinceLastBeat >= 60000 / Global.bpm.toDouble() || timeSinceLastBeat == 0L)
                            && Global.metronome === true
                        ) {
                            Global.metroPool.play(
                                Global.metroId, volume
                                        * 0.02.toFloat(), volume * 0.02.toFloat(),
                                1, 0, 1.0.toFloat()
                            )
                            timeAtLastBeat = currentTime
                        }
                        if (frontQueue.size > 0) {
                            while (frontQueue.size > 0
                                && frontQueue.peek()?.offset!! <= (timeSinceStart
                                        * Global.bpm.toDouble() / 60000 + 1)
                            ) {
                                val s = frontQueue.remove()
                                mainHandler.post {
                                    val button =
                                        findViewById<View>(
                                            padIds[s?.buttonId_i!!][s.buttonId_j]
                                        ) as Button
                                    val animation: Animation = AlphaAnimation(
                                        1F, 0F
                                    )
                                    animation.duration = 100
                                    animation.interpolator = LinearInterpolator()
                                    animation.repeatMode = Animation.REVERSE
                                    button.startAnimation(animation)
                                }
                                Global.soundPool.play(
                                    s?.soundPoolId!!,
                                    volume, volume, 1, 0, 1.0.toFloat()
                                )
                            }
                        }
                        if (timeSinceLastBeat >= 60000 / Global.bpm) {
                            mainHandler.post { }
                        }
                        if (timeSinceStart >= 240000 * bars / Global.bpm) {
                            timeSinceStart = 0
                            timeAtStart = currentTime
                            frontQueue.clear()
                            frontQueue.addAll(
                                Global.patternSoundQueues[patternId]
                            )
                        }
                        mainHandler.post {
                            progBar.progress = ((timeSinceStart * 25 * Global.bpm) / (60000 * bars)).toInt()
                            if (state_playing == false) progBar.progress = 0
                        }
                    }
                    handler.postDelayed(this, 1)
                }
            })
            Looper.loop()
        }, "PlaybackThread")
        playbackThread1!!.start()
        val playButton =
            findViewById<View>(R.id.play_button) as ImageButton
        val recordButton =
            findViewById<View>(R.id.record_button) as ImageButton
        playButton.setOnClickListener {
            if (state_playing == false) {
                state_playing = true
                playButton.setImageResource(R.drawable.play_button_pressed)
                timeAtStart = SystemClock.elapsedRealtime()
                timeAtLastBeat = SystemClock.elapsedRealtime()
                frontQueue.clear()
                frontQueue.addAll(Global.patternSoundQueues[patternId])
                recordButton.isEnabled = false
                playButton.setImageResource(R.drawable.stop_button_normal)
                recordButton
                    .setImageResource(R.drawable.record_button_normal)
                playButton
                    .setBackgroundResource(R.drawable.stop_border_play)
            } else {
                state_playing = false
                state_recording = false
                playButton.isEnabled = true
                recordButton.isEnabled = true
                timeSinceStart = 0
                progBar.progress = 0
                playButton.setImageResource(R.drawable.play_button_pressed)
                recordButton
                    .setImageResource(R.drawable.record_button_pressed)
                playButton
                    .setBackgroundResource(R.drawable.button_background)
            }
        }
        recordButton.setOnClickListener {
            if (state_recording == false) {
                state_recording = true
                state_playing = true
                timeAtStart = SystemClock.elapsedRealtime()
                timeAtLastBeat = SystemClock.elapsedRealtime()
                recordButton
                    .setImageResource(R.drawable.record_button_pressed)
                frontQueue.clear()
                frontQueue.addAll(Global.patternSoundQueues[patternId])
                playButton.isEnabled = false
                recordButton
                    .setImageResource(R.drawable.stop_button_normal)
                playButton.setImageResource(R.drawable.play_button_normal)
                recordButton
                    .setBackgroundResource(R.drawable.stop_border_record)
            } else {
                state_playing = false
                state_recording = false
                playButton.isEnabled = true
                recordButton.isEnabled = true
                timeSinceStart = 0
                progBar.progress = 0
                playButton.setImageResource(R.drawable.play_button_pressed)
                recordButton
                    .setImageResource(R.drawable.record_button_pressed)
                recordButton
                    .setBackgroundResource(R.drawable.button_background)
            }
        }
        val pad =
            ArrayList<ArrayList<Button>>()
        for (i in 0..5) pad.add(ArrayList())
        var btn: Button
        for (i in 0..5) {
            for (j in 0..5) {
                btn = findViewById<View>(padIds[i][j]) as Button
                pad[i].add(btn)

                // When button is pressed, play sound. If recording, snap sound
                // to appropriate beat and add to sound queue
                pad[i][j]
                    .setOnClickListener {
                        val offset: Double
                        val exactBeatOffset = (SystemClock
                            .elapsedRealtime() - timeAtStart).toDouble() * Global.bpm.toDouble() / 60000 + 1
                        val a = exactBeatOffset / snapValue
                        val b: Double = a as Double
                        val c = a - b
                        offset =
                            if (c <= 0.5) exactBeatOffset - snapValue * c else exactBeatOffset + snapValue * 1 - c
                        Thread(Runnable {
                            mainHandler.post {
                                if (state_recording == true) Global.patternSoundQueues[patternId]
                                    .add(
                                        Sound(
                                            Global.soundIds[i][j],
                                            i, j,
                                            offset,
                                            patternId
                                        )
                                    )
                                Global.soundPool
                                    .play(
                                        Global.soundIds[i][j],
                                        volume, volume,
                                        1, 0,
                                        1.0.toFloat()
                                    )
                            }
                        }).start()
                    }
            }
        }
    }

    // Called when activity resumes
    override fun onResume() {
        super.onResume()

        // Update button text
        var btn: Button
    }

    // Transition to Track Activity
    fun callTrackActivity(v: View?) {
        val trackIntent = Intent(this, TrackActivity::class.java)
        trackIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(trackIntent)
        finish()
    }

    // Show pattern options dialog with tempo, bars, export, metronome
    fun callPatternOptions(v: View?) {
        val context: Context = this
        val patternInfoDialog = Dialog(this)
        patternInfoDialog.setContentView(R.layout.pattern_info_dialog)
        patternInfoDialog.setTitle("Pattern Options")
        val tempoEdit = patternInfoDialog
            .findViewById<View>(R.id.edit_tempo) as EditText
        tempoEdit.setText(
            java.lang.String.valueOf(Global.bpm),
            TextView.BufferType.EDITABLE
        )
        val barsEdit = patternInfoDialog
            .findViewById<View>(R.id.edit_bars) as EditText
        if (patternId == 0) barsEdit.setText(
            java.lang.String.valueOf(Global.pattern1Bars),
            TextView.BufferType.EDITABLE
        ) else if (patternId == 1) barsEdit.setText(
            java.lang.String.valueOf(Global.pattern2Bars),
            TextView.BufferType.EDITABLE
        ) else if (patternId == 2) barsEdit.setText(
            java.lang.String.valueOf(Global.pattern3Bars),
            TextView.BufferType.EDITABLE
        ) else if (patternId == 3) barsEdit.setText(
            java.lang.String.valueOf(Global.pattern4Bars),
            TextView.BufferType.EDITABLE
        )
        val metronomeButton = patternInfoDialog
            .findViewById<View>(R.id.metronome_button) as Button
        metronomeButton.setOnClickListener {
            if (Global.metronome === true) {
                Global.metronome = false
                metronomeButton.text = "Metronome is Off"
            } else {
                Global.metronome = true
                metronomeButton.text = "Metronome is On"
            }
        }

        // Export
        val exportButton = patternInfoDialog
            .findViewById<View>(R.id.export_button) as Button
        exportButton.setOnClickListener {
            val builder =
                AlertDialog.Builder(context)
            val inflater = layoutInflater
            val dialoglayout = inflater.inflate(
                R.layout.custom_dialog,
                currentFocus as ViewGroup?
            )
            val text = dialoglayout
                .findViewById<View>(R.id.file) as EditText
            builder.setView(dialoglayout)
            builder.setTitle("Save File")
            builder.setMessage("Exporting to SD card")
                .setPositiveButton(R.string.ok,
                    DialogInterface.OnClickListener { dialog, id ->
                        var mExternalStorageAvailable = false
                        var mExternalStorageWriteable = false
                        val io = WavIO()
                        val sdcard = Environment
                            .getExternalStorageDirectory()
                        val state = Environment
                            .getExternalStorageState()
                        if (Environment.MEDIA_MOUNTED
                            == state
                        ) {
                            mExternalStorageWriteable = true
                            mExternalStorageAvailable = mExternalStorageWriteable
                        } else if (Environment.MEDIA_MOUNTED_READ_ONLY
                            == state
                        ) {
                            // We can only read the media
                            mExternalStorageAvailable = true
                            mExternalStorageWriteable = false
                        } else {
                            mExternalStorageWriteable = false
                            mExternalStorageAvailable = mExternalStorageWriteable
                        }
                        // -----------------------------------------------------------------
                        if (mExternalStorageAvailable) {
                            if (mExternalStorageWriteable) {
                                val exportFileName = (text
                                    .text.toString()
                                        + ".wav")
                                Toast.makeText(
                                    context,
                                    "Exporting To : "
                                            + sdcard.path
                                            + "/Music/Beats/exported/"
                                            + exportFileName,
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                                synchronized(Global.patternSoundQueues) {
                                    val result: Boolean = io
                                        .exportSound(
                                            exportFileName,
                                            Global.patternSoundQueues[patternId],
                                            context
                                        )
                                    if (result) {
                                        Toast.makeText(
                                            context,
                                            "Done!!",
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Not Done!!",
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "ERROR : External Storage is available but not writable. Please check Permissions.",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "ERROR : External Storage is not available.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        patternInfoDialog.dismiss()
                    })
                .setNegativeButton(R.string.Cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        // User cancelled the dialog
                    })
            // Create the AlertDialog object and return it
            builder.create().show()
        }

        // Commit pattern option changes
        val done = patternInfoDialog
            .findViewById<View>(R.id.pattern_info_done_button) as Button
        done.setOnClickListener {
            Global.bpm = Integer.valueOf(tempoEdit.text.toString())
            if (patternId == 0) {
                Global.pattern1Bars = Integer.valueOf(
                    barsEdit.text
                        .toString()
                )
            } else if (patternId == 1) {
                Global.pattern2Bars = Integer.valueOf(
                    barsEdit.text
                        .toString()
                )
            } else if (patternId == 2) {
                Global.pattern3Bars = Integer.valueOf(
                    barsEdit.text
                        .toString()
                )
            } else if (patternId == 3) {
                Global.pattern4Bars = Integer.valueOf(
                    barsEdit.text
                        .toString()
                )
            }
            bars = Integer.valueOf(barsEdit.text.toString())
            patternInfoDialog.dismiss()
        }
        val clearPattern = patternInfoDialog
            .findViewById<View>(R.id.clear_pattern_button2) as Button
        clearPattern.setOnClickListener {
            frontQueue.clear()
            Global.patternSoundQueues[patternId].clear()
            patternInfoDialog.dismiss()
            Toast.makeText(context, "Pattern Cleared!", Toast.LENGTH_SHORT)
                .show()
        }
        patternInfoDialog.show()
    }

    // set button gradient
    fun updateGradient(name: String) {
        var btn: Button
        for (i in 0..5) {
            for (j in 0..5) {
                btn = findViewById<View>(padIds[i][j]) as Button
                if (name.compareTo("yellow") == 0) {
                    btn.setBackgroundResource(R.drawable.gradient_yellow)
                    if (i >= 3 && j < 3) {
                        btn.setBackgroundResource(R.drawable.gradient_green)
                    }
                } else if (name.compareTo("green") == 0) {
                    btn.setBackgroundResource(R.drawable.gradient_green)
                    if (i >= 3 && j < 3) {
                        btn.setBackgroundResource(R.drawable.gradient_yellow)
                    }
                } else if (name.compareTo("blue") == 0) {
                    btn.setBackgroundResource(R.drawable.gradient_blue)
                    if (i >= 3 && j < 3) {
                        btn.setBackgroundResource(R.drawable.gradient_purple)
                    }
                } else if (name.compareTo("purple") == 0) {
                    btn.setBackgroundResource(R.drawable.gradient_purple)
                    if (i >= 3 && j < 3) {
                        btn.setBackgroundResource(R.drawable.gradient_blue)
                    }
                }
            }
        }
    }

    // Release resources and quit thread
    public override fun onDestroy() {
        super.onDestroy()
        l1!!.quit()
        frontQueue.clear()
    }

    companion object {
        private val mainHandler = Handler()
        private var playbackThread1: Thread? = null
        private var l1: Looper? = null
        const val BUTTON_NAMES = "ButtonNamess"
        var buttonNames: SharedPreferences? = null
        const val BUTTON_SOUNDS = "buttonSounds?s"
        var buttonSounds : SharedPreferences? = null
    }
}