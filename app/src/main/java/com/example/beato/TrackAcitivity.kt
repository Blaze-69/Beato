package com.example.beato

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioManager
import android.os.*
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.beatmakingapp.R
import java.util.*

class TrackActivity : Activity() {
    private var pattern1Button: Button? = null
    private var pattern2Button: Button? = null
    private var pattern3Button: Button? = null
    private var pattern4Button: Button? = null
    private var addPattern1Button: ImageButton? = null
    private var addPattern2Button: ImageButton? = null
    private var addPattern3Button: ImageButton? = null
    private var addPattern4Button: ImageButton? = null
    private val context: Context = this
    private var progBar: ProgressBar? = null
    private var maxBars = 0
    private var num = 1
    var state_playing = false
    var timeAtStart: Long = 0
    var timeSinceStart: Long = 0
    private var playButton: ImageButton? = null
    private var exportButton: Button? = null
    private var timeOfLastBeat = 0.0

    // Called when activity is created
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.track_layout2)
        progBar = findViewById<View>(R.id.progBar) as ProgressBar
        progBar!!.progress = 0
        val am =
            getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volume = am
            .getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()

        //used to play the sounds in track queue
        playbackThread1 = Thread(Runnable {
            Looper.prepare()
            l1 = Looper.myLooper()
            val handler = Handler(Looper.myLooper()!!)
            handler.post(object : Runnable {
                override fun run() {
                    if (state_playing == true) {
                        val currentTime = SystemClock.elapsedRealtime()
                        timeSinceStart = currentTime - timeAtStart
                        if (Global.trackSoundQueue.size > 0) {
                            while (Global.trackSoundQueue.size> 0
                                && Global.trackSoundQueue.peek()
                                    .offset <= (timeSinceStart
                                        * Global.bpm.toDouble() / 60000 + 1)
                            ) {
                                val s = Global.trackSoundQueue
                                    .remove()
                                println(
                                    timeSinceStart
                                            * Global.bpm.toDouble()
                                            / 60000 + 1
                                )
                                System.out.println(s.offset)
                                Global.soundPool.play(
                                    s.soundPoolId,
                                    volume, volume, 1, 0, 1.0.toFloat()
                                )
                            }
                        } else {
                            createTrackQueue()
                            state_playing = false
                            mainHandler.post {
                                enableAllButtons()
                                playButton
                                    ?.setImageResource(R.drawable.play_button_pressed)
                                playButton
                                    ?.setBackgroundResource(R.drawable.button_background)
                            }
                        }
                        mainHandler.post {
                            progBar!!.progress =
                                (4 * timeSinceStart * 25 * Global.bpm / ((timeOfLastBeat - 1) * 60000)).toInt()
                        }
                    }
                    handler.postDelayed(this, 1)
                }
            })
            Looper.loop()
        }, "PlaybackThread")
        playbackThread1!!.start()
        addButtons()
    }

    //describe the functionality of play button and export button
    fun addButtons() {

        //play button functionality
        playButton = findViewById<View>(R.id.play_button_track) as ImageButton
        playButton!!.setOnClickListener {
            if (state_playing == false) {
                createTrackQueue()
                val it: Iterator<*> = Global.trackSoundQueue.iterator()
                while (it.hasNext()) {
                    val s = it.next() as Sound
                    if (timeOfLastBeat < s.offset) timeOfLastBeat = s.offset
                }
                disableAllButtons()
                playButton!!.setImageResource(R.drawable.stop_button_normal)
                playButton!!
                    .setBackgroundResource(R.drawable.stop_border_play)
                timeAtStart = SystemClock.elapsedRealtime()
                timeSinceStart = 0
                state_playing = true
            } else {
                enableAllButtons()
                Global.trackSoundQueue.clear()
                createTrackQueue()
                timeAtStart = SystemClock.elapsedRealtime()
                timeSinceStart = 0
                state_playing = false
                playButton!!.setImageResource(R.drawable.play_button_pressed)
                playButton!!
                    .setBackgroundResource(R.drawable.button_background)
            }
        }

        //export button functionality
        exportButton =
            findViewById<View>(R.id.export_button_track) as Button
        exportButton!!.setOnClickListener {
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
                            // We can read and write the media
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
                                            Global.trackSoundQueue,
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
                    })
                .setNegativeButton(R.string.Cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        // User cancelled the dialog
                    })
            // Create the AlertDialog object and return it
            builder.create().show()
        }

        //buttons to navigate the pattern
        //eg pattern1Button navigates to Pattern-1
        pattern1Button =
            findViewById<View>(R.id.pattern1Button) as Button
        pattern1Button!!.setOnClickListener {
            val intent = Intent().setClass(
                context,
                PatternActivity::class.java
            )
            val message = "first"
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            intent.putExtra("msgFromParent", message)
            startActivity(intent)
        }
        pattern2Button =
            findViewById<View>(R.id.pattern2Button) as Button
        pattern2Button!!.setOnClickListener {
            val intent = Intent().setClass(
                context,
                PatternActivity::class.java
            )
            val message = "second"
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            intent.putExtra("msgFromParent", message)
            startActivity(intent)
        }
        pattern3Button =
            findViewById<View>(R.id.pattern3Button) as Button
        pattern3Button!!.setOnClickListener {
            val intent = Intent().setClass(
                context,
                PatternActivity::class.java
            )
            val message = "third"
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            intent.putExtra("msgFromParent", message)
            startActivity(intent)
        }
        pattern4Button =
            findViewById<View>(R.id.pattern4Button) as Button
        pattern4Button!!.setOnClickListener {
            val intent = Intent().setClass(
                context,
                PatternActivity::class.java
            )
            val message = "fourth"
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            intent.putExtra("msgFromParent", message)
            startActivity(intent)
        }

        //add instances of Pattern-1
        addPattern1Button = findViewById<View>(R.id.addPattern1Button) as ImageButton
        addPattern1Button!!.setOnClickListener {
            num = 1
            val addPattern1Dialog = Dialog(context)
            addPattern1Dialog.setContentView(R.layout.add_pattern_dialog)
            addPattern1Dialog.setTitle("Add Pattern 1 at which bar?")
            val pickerEdit = addPattern1Dialog
                .findViewById<View>(R.id.add_pattern_edit) as EditText
            pickerEdit.setText(
                num.toString(),
                TextView.BufferType.EDITABLE
            )
            val pickerDown = addPattern1Dialog
                .findViewById<View>(R.id.add_pattern_down) as Button
            val pickerUp = addPattern1Dialog
                .findViewById<View>(R.id.add_pattern_up) as Button
            val okButton = addPattern1Dialog
                .findViewById<View>(R.id.add_pattern_ok) as Button
            val cancelButton = addPattern1Dialog
                .findViewById<View>(R.id.add_pattern_cancel) as Button
            pickerUp.setOnClickListener {
                num += 1
                pickerEdit.setText(
                    num.toString(),
                    TextView.BufferType.EDITABLE
                )
            }
            pickerDown.setOnClickListener {
                if (num > 1) {
                    num = num - 1
                    pickerEdit.setText(
                        num.toString(),
                        TextView.BufferType.EDITABLE
                    )
                }
            }
            okButton.setOnClickListener {
                num = Integer.valueOf(pickerEdit.text.toString())
                if (num > 50) {
                    addPattern1Dialog.dismiss()
                    AlertDialog.Builder(context)
                        .setTitle(
                            "Limit Exceeded!\nValue cannot be greater than 50!"
                        )
                        .setPositiveButton(
                            android.R.string.yes,
                            null
                        ).create().show()
                } else {
                    if (isValidPosition(
                            Global.pattern1Bars,
                            Global.pattern1SegmentPositions, num
                        )
                    ) {
                        Global.pattern1SegmentPositions.add(num)
                        Global.pattern1SegmentPositions.sort()
                        val p1TrackLayout =
                            findViewById<View>(R.id.pattern1TrackRow) as LinearLayout
                        p1TrackLayout.removeAllViews()
                        Global.buttonPositions1.clear()
                        var lastP = 1
                        for (p in Global.pattern1SegmentPositions) {
                            if (p - lastP > 0) {
                                val buffer = View(context)
                                val lp =
                                    LinearLayout.LayoutParams(
                                        300 * (p - lastP),
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                buffer.layoutParams = lp
                                p1TrackLayout.addView(buffer)
                            }
                            val p1B = Button(context)
                            Global.buttonPositions1[p1B] = p
                            p1B.setOnLongClickListener { v -> // TODO Auto-generated method stub
                                val btn =
                                    v as Button
                                val builder =
                                    AlertDialog.Builder(
                                        context
                                    )
                                builder.setTitle(R.string.removePattern)
                                builder.setMessage(
                                    R.string.confirmation
                                )
                                    .setPositiveButton(
                                        R.string.ok,
                                        DialogInterface.OnClickListener { dialog, id ->
                                            val position =
                                                Global.buttonPositions1[btn]
                                            Global.buttonPositions1
                                                .remove(btn)
                                            val result =
                                                Global.pattern1SegmentPositions
                                                    .remove(position!!)
                                            btn.visibility = View.INVISIBLE
                                            createTrackQueue()
                                        })
                                    .setNegativeButton(
                                        R.string.Cancel,
                                        DialogInterface.OnClickListener { dialog, id ->
                                            // User
                                            // cancelled
                                            // the
                                            // dialog
                                        })
                                // Create the AlertDialog object and
                                // return it
                                builder.create().show()
                                true
                            }
                            val lp = LinearLayout.LayoutParams(
                                Global.pattern1Bars * 300,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            lp.gravity = 17
                            p1B.layoutParams = lp
                            p1B.setBackgroundResource(R.drawable.rounded_button_yellow)
                            p1TrackLayout.addView(p1B)
                            lastP = p + Global.pattern1Bars
                            createTrackQueue()
                        }
                    } else {
                        AlertDialog.Builder(context)
                            .setTitle(
                                "Pattern already exists at this bar!"
                            )
                            .setPositiveButton(
                                android.R.string.yes, null
                            )
                            .create().show()
                    }
                    addPattern1Dialog.dismiss()
                }
            }
            cancelButton.setOnClickListener { addPattern1Dialog.dismiss() }
            addPattern1Dialog.show()
        }

        //add instances of Pattern-2
        addPattern2Button = findViewById<View>(R.id.addPattern2Button) as ImageButton
        addPattern2Button!!.setOnClickListener {
            num = 1
            val addPattern2Dialog = Dialog(context)
            addPattern2Dialog.setContentView(R.layout.add_pattern_dialog)
            addPattern2Dialog.setTitle("Add Pattern 2 at which bar?")
            val pickerEdit = addPattern2Dialog
                .findViewById<View>(R.id.add_pattern_edit) as EditText
            pickerEdit.setText(
                num.toString(),
                TextView.BufferType.EDITABLE
            )
            val pickerDown = addPattern2Dialog
                .findViewById<View>(R.id.add_pattern_down) as Button
            val pickerUp = addPattern2Dialog
                .findViewById<View>(R.id.add_pattern_up) as Button
            val okButton = addPattern2Dialog
                .findViewById<View>(R.id.add_pattern_ok) as Button
            val cancelButton = addPattern2Dialog
                .findViewById<View>(R.id.add_pattern_cancel) as Button
            pickerUp.setOnClickListener {
                num += 1
                pickerEdit.setText(
                    num.toString(),
                    TextView.BufferType.EDITABLE
                )
            }
            pickerDown.setOnClickListener {
                if (num > 1) {
                    num = num - 1
                    pickerEdit.setText(
                        num.toString(),
                        TextView.BufferType.EDITABLE
                    )
                }
            }
            okButton.setOnClickListener {
                num = Integer.valueOf(pickerEdit.text.toString())
                if (isValidPosition(
                        Global.pattern2Bars,
                        Global.pattern2SegmentPositions, num
                    )
                ) {
                    Global.pattern2SegmentPositions.add(num)
                    Global.pattern2SegmentPositions.sort()
                    val p2TrackLayout =
                        findViewById<View>(R.id.pattern2TrackRow) as LinearLayout
                    p2TrackLayout.removeAllViews()
                    Global.buttonPositions2.clear()
                    var lastP = 1
                    for (p in Global.pattern2SegmentPositions) {
                        if (p - lastP > 0) {
                            val buffer = View(context)
                            val lp = LinearLayout.LayoutParams(
                                300 * (p - lastP),
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            buffer.layoutParams = lp
                            p2TrackLayout.addView(buffer)
                        }
                        val p2B = Button(context)
                        Global.buttonPositions2[p2B] = p
                        p2B.setOnLongClickListener { v -> // TODO Auto-generated method stub
                            val btn = v as Button
                            val builder =
                                AlertDialog.Builder(
                                    context
                                )
                            builder.setTitle(R.string.removePattern)
                            builder.setMessage(
                                R.string.confirmation
                            )
                                .setPositiveButton(
                                    R.string.ok,
                                    DialogInterface.OnClickListener { dialog, id ->
                                        val position = Global.buttonPositions2[btn]
                                        Global.buttonPositions2
                                            .remove(btn)
                                        val result =
                                            Global.pattern2SegmentPositions
                                                .remove(position!!)
                                        btn.visibility = View.INVISIBLE
                                        createTrackQueue()
                                    })
                                .setNegativeButton(
                                    R.string.Cancel,
                                    DialogInterface.OnClickListener { dialog, id ->
                                        // User
                                        // cancelled the
                                        // dialog
                                    })
                            // Create the AlertDialog object and
                            // return it
                            builder.create().show()
                            true
                        }
                        val lp = LinearLayout.LayoutParams(
                            Global.pattern2Bars * 300,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        lp.gravity = 17
                        p2B.layoutParams = lp
                        p2B.setBackgroundResource(R.drawable.rounded_button_green)
                        p2TrackLayout.addView(p2B)
                        lastP = p + Global.pattern2Bars
                        createTrackQueue()
                    }
                } else {
                    AlertDialog.Builder(context)
                        .setTitle(
                            "Pattern already exists at this bar!"
                        )
                        .setPositiveButton(
                            android.R.string.yes,
                            null
                        ).create().show()
                }
                addPattern2Dialog.dismiss()
            }
            cancelButton.setOnClickListener { addPattern2Dialog.dismiss() }
            addPattern2Dialog.show()
        }

        //add instances of Pattern-3
        addPattern3Button = findViewById<View>(R.id.addPattern3Button) as ImageButton
        addPattern3Button!!.setOnClickListener {
            num = 1
            val addPattern3Dialog = Dialog(context)
            addPattern3Dialog.setContentView(R.layout.add_pattern_dialog)
            addPattern3Dialog.setTitle("Add Pattern 3 at which bar?")
            val pickerEdit = addPattern3Dialog
                .findViewById<View>(R.id.add_pattern_edit) as EditText
            pickerEdit.setText(
                num.toString(),
                TextView.BufferType.EDITABLE
            )
            val pickerDown = addPattern3Dialog
                .findViewById<View>(R.id.add_pattern_down) as Button
            val pickerUp = addPattern3Dialog
                .findViewById<View>(R.id.add_pattern_up) as Button
            val okButton = addPattern3Dialog
                .findViewById<View>(R.id.add_pattern_ok) as Button
            val cancelButton = addPattern3Dialog
                .findViewById<View>(R.id.add_pattern_cancel) as Button
            pickerUp.setOnClickListener {
                num += 1
                pickerEdit.setText(
                    num.toString(),
                    TextView.BufferType.EDITABLE
                )
            }
            pickerDown.setOnClickListener {
                if (num > 1) {
                    num = num - 1
                    pickerEdit.setText(
                        num.toString(),
                        TextView.BufferType.EDITABLE
                    )
                }
            }
            okButton.setOnClickListener {
                num = Integer.valueOf(pickerEdit.text.toString())
                if (isValidPosition(
                        Global.pattern3Bars,
                        Global.pattern3SegmentPositions, num
                    )
                ) {
                    Global.pattern3SegmentPositions.add(num)
                    Global.pattern3SegmentPositions.sort()
                    val p3TrackLayout =
                        findViewById<View>(R.id.pattern3TrackRow) as LinearLayout
                    p3TrackLayout.removeAllViews()
                    var lastP = 1
                    Global.buttonPositions3.clear()
                    for (p in Global.pattern3SegmentPositions) {
                        if (p - lastP > 0) {
                            val buffer = View(context)
                            val lp = LinearLayout.LayoutParams(
                                300 * (p - lastP),
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            buffer.layoutParams = lp
                            p3TrackLayout.addView(buffer)
                        }
                        val p3B = Button(context)
                        Global.buttonPositions3[p3B] = p
                        p3B.setOnLongClickListener { v -> // TODO Auto-generated method stub
                            val btn = v as Button
                            val builder =
                                AlertDialog.Builder(
                                    context
                                )
                            builder.setTitle(R.string.removePattern)
                            builder.setMessage(
                                R.string.confirmation
                            )
                                .setPositiveButton(
                                    R.string.ok,
                                    DialogInterface.OnClickListener { dialog, id ->
                                        val position = Global.buttonPositions3[btn]
                                        Global.buttonPositions3
                                            .remove(btn)
                                        val result =
                                            Global.pattern3SegmentPositions
                                                .remove(position!!)
                                        btn.visibility = View.INVISIBLE
                                        createTrackQueue()
                                    })
                                .setNegativeButton(
                                    R.string.Cancel,
                                    DialogInterface.OnClickListener { dialog, id ->
                                        // User
                                        // cancelled the
                                        // dialog
                                    })
                            // Create the AlertDialog object and
                            // return it
                            builder.create().show()
                            true
                        }
                        val lp = LinearLayout.LayoutParams(
                            Global.pattern3Bars * 300,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        lp.gravity = 17
                        p3B.layoutParams = lp
                        p3B.setBackgroundResource(R.drawable.rounded_button_blue)
                        p3TrackLayout.addView(p3B)
                        lastP = p + Global.pattern3Bars
                        createTrackQueue()
                    }
                } else {
                    AlertDialog.Builder(context)
                        .setTitle(
                            "Pattern already exists at this bar!"
                        )
                        .setPositiveButton(
                            android.R.string.yes,
                            null
                        ).create().show()
                }
                addPattern3Dialog.dismiss()
            }
            cancelButton.setOnClickListener { addPattern3Dialog.dismiss() }
            addPattern3Dialog.show()
        }

        //add instances of Pattern-4
        addPattern4Button = findViewById<View>(R.id.addPattern4Button) as ImageButton
        addPattern4Button!!.setOnClickListener {
            num = 1
            val addPattern4Dialog = Dialog(context)
            addPattern4Dialog.setContentView(R.layout.add_pattern_dialog)
            addPattern4Dialog.setTitle("Add Pattern 4 at which bar?")
            val pickerEdit = addPattern4Dialog
                .findViewById<View>(R.id.add_pattern_edit) as EditText
            pickerEdit.setText(
                num.toString(),
                TextView.BufferType.EDITABLE
            )
            val pickerDown = addPattern4Dialog
                .findViewById<View>(R.id.add_pattern_down) as Button
            val pickerUp = addPattern4Dialog
                .findViewById<View>(R.id.add_pattern_up) as Button
            val okButton = addPattern4Dialog
                .findViewById<View>(R.id.add_pattern_ok) as Button
            val cancelButton = addPattern4Dialog
                .findViewById<View>(R.id.add_pattern_cancel) as Button
            pickerUp.setOnClickListener {
                num += 1
                pickerEdit.setText(
                    num.toString(),
                    TextView.BufferType.EDITABLE
                )
            }
            pickerDown.setOnClickListener {
                if (num > 1) {
                    num = num - 1
                    pickerEdit.setText(
                        num.toString(),
                        TextView.BufferType.EDITABLE
                    )
                }
            }
            okButton.setOnClickListener {
                num = Integer.valueOf(pickerEdit.text.toString())
                if (isValidPosition(
                        Global.pattern4Bars,
                        Global.pattern4SegmentPositions, num
                    )
                ) {
                    Global.pattern4SegmentPositions.add(num)
                    Global.pattern4SegmentPositions.sort()
                    val p4TrackLayout =
                        findViewById<View>(R.id.pattern4TrackRow) as LinearLayout
                    p4TrackLayout.removeAllViews()
                    Global.buttonPositions4.clear()
                    var lastP = 1
                    for (p in Global.pattern4SegmentPositions) {
                        if (p - lastP > 0) {
                            val buffer = View(context)
                            val lp = LinearLayout.LayoutParams(
                                300 * (p - lastP),
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            buffer.layoutParams = lp
                            p4TrackLayout.addView(buffer)
                        }
                        val p4B = Button(context)
                        Global.buttonPositions4[p4B] = p
                        p4B.setOnLongClickListener { v -> // TODO Auto-generated method stub
                            val btn = v as Button
                            val builder =
                                AlertDialog.Builder(
                                    context
                                )
                            builder.setTitle(R.string.removePattern)
                            builder.setMessage(
                                R.string.confirmation
                            )
                                .setPositiveButton(
                                    R.string.ok,
                                    DialogInterface.OnClickListener { dialog, id ->
                                        val position = Global.buttonPositions4[btn]
                                        Global.buttonPositions4
                                            .remove(btn)
                                        val result =
                                            Global.pattern4SegmentPositions
                                                .remove(position!!)
                                        btn.visibility = View.INVISIBLE
                                        createTrackQueue()
                                    })
                                .setNegativeButton(
                                    R.string.Cancel,
                                    DialogInterface.OnClickListener { dialog, id ->
                                        // User
                                        // cancelled the
                                        // dialog
                                    })
                            // Create the AlertDialog object and
                            // return it
                            builder.create().show()
                            true
                        }
                        val lp = LinearLayout.LayoutParams(
                            Global.pattern4Bars * 300,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        lp.gravity = 17
                        p4B.layoutParams = lp
                        p4B.setBackgroundResource(R.drawable.rounded_button_red)
                        p4TrackLayout.addView(p4B)
                        lastP = p + Global.pattern4Bars
                        createTrackQueue()
                    }
                } else {
                    AlertDialog.Builder(context)
                        .setTitle(
                            "Pattern already exists at this bar!"
                        )
                        .setPositiveButton(
                            android.R.string.yes,
                            null
                        ).create().show()
                }
                addPattern4Dialog.dismiss()
            }
            cancelButton.setOnClickListener { addPattern4Dialog.dismiss() }
            addPattern4Dialog.show()
        }
    }

    //disables the buttons while playback
    protected fun disableAllButtons() {
        // TODO Auto-generated method stub
        val btn1 =
            findViewById<View>(R.id.addPattern1Button) as ImageButton
        val btn2 =
            findViewById<View>(R.id.addPattern2Button) as ImageButton
        val btn3 =
            findViewById<View>(R.id.addPattern3Button) as ImageButton
        val btn4 =
            findViewById<View>(R.id.addPattern4Button) as ImageButton
        val patBtn1 =
            findViewById<View>(R.id.pattern1Button) as Button
        val patBtn2 =
            findViewById<View>(R.id.pattern2Button) as Button
        val patBtn3 =
            findViewById<View>(R.id.pattern3Button) as Button
        val patBtn4 =
            findViewById<View>(R.id.pattern4Button) as Button
        btn1.isEnabled = false
        btn2.isEnabled = false
        btn3.isEnabled = false
        btn4.isEnabled = false
        patBtn1.isEnabled = false
        patBtn2.isEnabled = false
        patBtn3.isEnabled = false
        patBtn4.isEnabled = false
    }

    //enables the buttons after playback
    protected fun enableAllButtons() {
        // TODO Auto-generated method stub
        val btn1 =
            findViewById<View>(R.id.addPattern1Button) as ImageButton
        val btn2 =
            findViewById<View>(R.id.addPattern2Button) as ImageButton
        val btn3 =
            findViewById<View>(R.id.addPattern3Button) as ImageButton
        val btn4 =
            findViewById<View>(R.id.addPattern4Button) as ImageButton
        val patBtn1 =
            findViewById<View>(R.id.pattern1Button) as Button
        val patBtn2 =
            findViewById<View>(R.id.pattern2Button) as Button
        val patBtn3 =
            findViewById<View>(R.id.pattern3Button) as Button
        val patBtn4 =
            findViewById<View>(R.id.pattern4Button) as Button
        btn1.isEnabled = true
        btn2.isEnabled = true
        btn3.isEnabled = true
        btn4.isEnabled = true
        patBtn1.isEnabled = true
        patBtn2.isEnabled = true
        patBtn3.isEnabled = true
        patBtn4.isEnabled = true
    }

    //checks if there is already instance of patterns added at that position
    fun isValidPosition(
        p: Int,
        patternSegmentPositions: ArrayList<Int>, startBar: Int
    ): Boolean {
        val endBar = startBar + p
        for (n in patternSegmentPositions) {
            if (startBar >= n && startBar < n + p
                || endBar <= n + p
            ) return false
        }
        return true
    }

    //creates track queue from pattern instances (all 4) and forms a new queue which can be played by pressing play
    fun createTrackQueue() {
        Global.trackSoundQueue.clear()
        Global.trackSoundQueueMS.clear()
        for (n in Global.pattern1SegmentPositions) {
            if (n + Global.pattern1Bars > maxBars) {
                maxBars = n + Global.pattern1Bars
            }
            for (s in Global.patternSoundQueues[0]) {
                val offset: Double = s.offset + ((n - 1) * 4).toDouble()
                Global.trackSoundQueue.add(
                    Sound(
                        s.soundPoolId, s
                            .buttonId_i, s.buttonId_j, offset, s
                            .patternId
                    )
                )
            }
        }
        for (n in Global.pattern2SegmentPositions) {
            if (n + Global.pattern2Bars > maxBars) {
                maxBars = n + +Global.pattern2Bars
            }
            for (s in Global.patternSoundQueues[1]) {
                val offset: Double = s.offset + ((n - 1) * 4).toDouble()
                Global.trackSoundQueue.add(
                    Sound(
                        s.soundPoolId, s
                            .buttonId_i, s.buttonId_j, offset, s
                            .patternId
                    )
                )
            }
        }
        for (n in Global.pattern3SegmentPositions) {
            if (n + Global.pattern3Bars > maxBars) {
                maxBars = n + +Global.pattern3Bars
            }
            for (s in Global.patternSoundQueues[2]) {
                val offset: Double = s.offset + ((n - 1) * 4).toDouble()
                Global.trackSoundQueue.add(
                    Sound(
                        s.soundPoolId, s
                            .buttonId_i, s.buttonId_j, offset, s
                            .patternId
                    )
                )
            }
        }
        for (n in Global.pattern4SegmentPositions) {
            if (n + Global.pattern4Bars > maxBars) {
                maxBars = n + +Global.pattern4Bars
            }
            for (s in Global.patternSoundQueues[3]) {
                val offset: Double = s.offset + ((n - 1) * 4).toDouble()
                Global.trackSoundQueue.add(
                    Sound(
                        s.soundPoolId, s
                            .buttonId_i, s.buttonId_j, offset, s
                            .patternId
                    )
                )
            }
        }
        for (s in Global.trackSoundQueue) {
            val offset: Double = s.offset * 60000 / Global.bpm.toDouble() - 1
            Global.trackSoundQueueMS.add(
                Sound(
                    s.soundPoolId, s
                        .buttonId_i, s.buttonId_j, offset, s
                        .patternId
                )
            )
        }
        mainHandler.post {
            val barNumbersLayout =
                findViewById<View>(R.id.barNumbersLayout) as LinearLayout
            barNumbersLayout.removeAllViews()
            for (n in 1 until maxBars) {
                val view = TextView(context)
                val lp = LinearLayout.LayoutParams(
                    300, ViewGroup.LayoutParams.FILL_PARENT
                )
                view.layoutParams = lp
                view.text = Integer.toString(n)
                barNumbersLayout.addView(view)
            }
        }
    }

    //asks the user if it wants to exit from the app
    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Really Exit?")
            .setMessage("Are you sure you want to exit?")
            .setNegativeButton(android.R.string.no, null)
            .setPositiveButton(
                android.R.string.yes
            ) { arg0, arg1 ->
                Global.trackSoundQueue.clear()
                Global.buttonPositions1.clear()
                Global.buttonPositions2.clear()
                Global.buttonPositions3.clear()
                Global.buttonPositions4.clear()
                Global.pattern1SegmentPositions.clear()
                Global.pattern2SegmentPositions.clear()
                Global.pattern3SegmentPositions.clear()
                Global.pattern4SegmentPositions.clear()
                for (i in 0..3) {
                    if (Global.soundPool != null) {
                        Global.patternSoundQueues[i]
                            .clear()
                    }
                }
                super@TrackActivity.onBackPressed()
                super@TrackActivity.onDestroy()
                finish()
            }.create().show()
    }

    companion object {
        private var playbackThread1: Thread? = null
        private var l1: Looper? = null
        private val mainHandler = Handler()
    }
}