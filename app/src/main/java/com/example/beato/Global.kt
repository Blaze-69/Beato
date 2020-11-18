package com.example.beato

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.widget.Button
import com.example.beatmakingapp.R
import java.util.*
object Global {
    var initialized = false
    var soundPool = SoundPool(16, AudioManager.STREAM_MUSIC, 0)
    var metroPool = SoundPool(1, AudioManager.STREAM_MUSIC, 0)
    var comp=DoubleComparator()
    var patternSoundQueues =
        ArrayList<PriorityQueue<Sound>>()
    var trackSoundQueue =
        PriorityQueue<Sound>(10, Global.comp)
    var trackSoundQueueMS =
        PriorityQueue<Sound>(10, Global.comp)
    var pattern1SegmentPositions = ArrayList<Int>()
    var pattern2SegmentPositions = ArrayList<Int>()
    var pattern3SegmentPositions = ArrayList<Int>()
    var pattern4SegmentPositions = ArrayList<Int>()
    var pattern1Bars = 4
    var pattern2Bars = 4
    var pattern3Bars = 4
    var pattern4Bars = 4
    var buttonPositions1 =
        HashMap<Button, Int>()
    var buttonPositions2 =
        HashMap<Button, Int>()
    var buttonPositions3 =
        HashMap<Button, Int>()
    var buttonPositions4 =
        HashMap<Button, Int>()
    var bpm = 120
    var soundIds =
        Array(6) { IntArray(6) }
    var metroId = 0
    var patternContext: Context? = null
    var metronome = false
    var p1SnapValue = 0.5
    var p2SnapValue = 0.5
    var p3SnapValue = 0.5
    var p4SnapValue = 0.5
    fun initialize() {
        Global.metronome = false
        Global.patternSoundQueues.add(
            PriorityQueue<Sound>(
                10,
                Global.comp
            )
        )
        Global.patternSoundQueues.add(
            PriorityQueue<Sound>(
                10,
                Global.comp
            )
        )
        Global.patternSoundQueues.add(
            PriorityQueue<Sound>(
                10,
                Global.comp
            )
        )
        Global.patternSoundQueues.add(
            PriorityQueue<Sound>(
                10,
                Global.comp
            )
        )
        Global.metroId = Global.metroPool.load(
            Global.patternContext,
            R.raw.closedhat,
            1
        )
        Global.soundIds.get(0)[0] = Global.soundPool.load(
            Global.patternContext,
            R.raw.sabar_kick,
            1
        )
        Global.soundIds.get(0)[1] = Global.soundPool.load(
            Global.patternContext,
            R.raw.sabar_kick_2,
            1
        )
        Global.soundIds.get(0)[2] = Global.soundPool.load(
            Global.patternContext,
            R.raw.sabar_kick_cool,
            1
        )
        Global.soundIds.get(0)[3] = Global.soundPool.load(
            Global.patternContext,
            R.raw.sabar_snare,
            1
        )
        Global.soundIds.get(0)[4] = Global.soundPool.load(
            Global.patternContext,
            R.raw.a4,
            1
        )
        Global.soundIds.get(0)[5] = Global.soundPool.load(
            Global.patternContext,
            R.raw.crash2,
            1
        )
        Global.soundIds.get(1)[0] = Global.soundPool.load(
            Global.patternContext,
            R.raw.sabar_snare_2,
            1
        )
        Global.soundIds.get(1)[1] = Global.soundPool.load(
            Global.patternContext,
            R.raw.sabar_snare_3,
            1
        )
        Global.soundIds.get(1)[2] = Global.soundPool.load(
            Global.patternContext,
            R.raw.sabar_snare_cool_2,
            1
        )
        Global.soundIds.get(1)[3] = Global.soundPool.load(
            Global.patternContext,
            R.raw.sabar_snare_flam,
            1
        )
        Global.soundIds.get(1)[4] = Global.soundPool.load(
            Global.patternContext,
            R.raw.a5,
            1
        )
        Global.soundIds.get(1)[5] = Global.soundPool.load(
            Global.patternContext,
            R.raw.a7,
            1
        )
        Global.soundIds.get(2)[0] = Global.soundPool.load(
            Global.patternContext,
            R.raw.sabar_snare_reverb,
            1
        )
        Global.soundIds.get(2)[1] = Global.soundPool.load(
            Global.patternContext,
            R.raw.sabar_snare_reverb_3,
            1
        )
        Global.soundIds.get(2)[2] = Global.soundPool.load(
            Global.patternContext,
            R.raw.sabar_snare_roll,
            1
        )
        Global.soundIds.get(2)[3] = Global.soundPool.load(
            Global.patternContext,
            R.raw.sabar_tom,
            1
        )
        Global.soundIds.get(2)[4] = Global.soundPool.load(
            Global.patternContext,
            R.raw.a6,
            1
        )
        Global.soundIds.get(2)[5] = Global.soundPool.load(
            Global.patternContext,
            R.raw.a8,
            1
        )
        Global.soundIds.get(3)[0] = Global.soundPool.load(
            Global.patternContext,
            R.raw.piano1,
            1
        )
        Global.soundIds.get(3)[1] = Global.soundPool.load(
            Global.patternContext,
            R.raw.piano2,
            1
        )
        Global.soundIds.get(3)[2] = Global.soundPool.load(
            Global.patternContext,
            R.raw.piano3,
            1
        )
        Global.soundIds.get(3)[3] = Global.soundPool.load(
            Global.patternContext,
            R.raw.a1,
            1
        )
        Global.soundIds.get(3)[4] = Global.soundPool.load(
            Global.patternContext,
            R.raw.z_gavgoodopen,
            1
        )
        Global.soundIds.get(3)[5] = Global.soundPool.load(
            Global.patternContext,
            R.raw.a9,
            1
        )
        Global.soundIds.get(4)[0] = Global.soundPool.load(
            Global.patternContext,
            R.raw.piano4,
            1
        )
        Global.soundIds.get(4)[1] = Global.soundPool.load(
            Global.patternContext,
            R.raw.piano5,
            1
        )
        Global.soundIds.get(4)[2] = Global.soundPool.load(
            Global.patternContext,
            R.raw.piano6,
            1
        )
        Global.soundIds.get(4)[3] = Global.soundPool.load(
            Global.patternContext,
            R.raw.a1,
            1
        )
        Global.soundIds.get(4)[4] = Global.soundPool.load(
            Global.patternContext,
            R.raw.tom2,
            1
        )
        Global.soundIds.get(4)[5] = Global.soundPool.load(
            Global.patternContext,
            R.raw.a9,
            1
        )
        Global.soundIds.get(5)[0] = Global.soundPool.load(
            Global.patternContext,
            R.raw.piano7,
            1
        )
        Global.soundIds.get(5)[1] = Global.soundPool.load(
            Global.patternContext,
            R.raw.piano8,
            1
        )
        Global.soundIds.get(5)[2] = Global.soundPool.load(
            Global.patternContext,
            R.raw.piano9,
            1
        )
        Global.soundIds.get(5)[3] = Global.soundPool.load(
            Global.patternContext,
            R.raw.a3,
            1
        )
        Global.soundIds.get(5)[4] = Global.soundPool.load(
            Global.patternContext,
            R.raw.clap2,
            1
        )
        Global.soundIds.get(5)[5] = Global.soundPool.load(
            Global.patternContext,
            R.raw.clap1,
            1
        )
    }
}