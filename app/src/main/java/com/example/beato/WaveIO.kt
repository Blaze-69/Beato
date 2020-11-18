package com.example.beato

import android.content.Context
import android.os.Environment
import com.example.beatmakingapp.R
import java.io.*
import java.util.*
import kotlin.experimental.and
class WavIO {
    var path: String
    private var myChunkSize: Long = 0
    private var mySubChunk1Size: Long = 0
    private var myFormat = 0
    private var myChannels: Long = 0
    private var mySampleRate: Long = 0
    private var myByteRate: Long = 0
    private var myBlockAlign = 0
    private var myBitsPerSample = 0
    private var myDataSize: Long = 0

    // empty constructor
    constructor() {
        path = ""
    }

    // constructor takes a wav path
    constructor(tmpPath: String) {
        path = tmpPath
    }

    fun createDataBuffer(
        exportAsFileName: String?,
        trackSoundQueue: PriorityQueue<Sound>?, ctxt: Context
    ): Boolean {
        val dataBuffer = ArrayList<Byte>()
        val tempPQ = PriorityQueue(trackSoundQueue)
        var sound: Sound
        var fileName: String? = null
        var differenceInMillisecs = 0.0
        var differenceInBeats = 0.0
        var offset = 0.0
        // int startTime = 0;
        var currentBeat =
            0.0 // shows the amount of track (in millisecs) already processed.
        while (tempPQ.size > 0) {
            dataBuffer.clear()
            sound = tempPQ.poll()
            offset = sound.offset
            if (offset > currentBeat) {
                differenceInBeats = sound.offset - currentBeat
                differenceInMillisecs = ((differenceInBeats * 60 * 1000)
                        / Global.bpm.toDouble())
                // differenceInMillisecs = sound.offset - currentTime;
                val silenceBytes =
                    ArrayList<Byte>()
                val silence: Byte = 0
                /*
				 * Toast.makeText(ctxt, myByteRate+"",
				 * Toast.LENGTH_SHORT).show();
				 */
                val numberofsilencebytes = Math
                    .ceil(
                        myByteRate.toFloat() / 1000.00
                                * differenceInMillisecs
                    ).toInt()

                // myByteRate = 28 bytes/sec = 28/1000 bytes per millisecs.
                for (i in 0 until numberofsilencebytes) {
                    silenceBytes.add(silence)
                }
                dataBuffer.addAll(silenceBytes)
            }
            fileName = getFileNameFromSound(sound)
            val myMusicData = read(ctxt, fileName)
            for (x in myMusicData!!.indices) {
                dataBuffer.add(myMusicData[x])
            }
            // Append to file....
            val dataArray = ByteArray(dataBuffer.size)
            for (x in dataBuffer.indices) {
                dataArray[x] = dataBuffer[x]
            }
            save(ctxt, exportAsFileName, dataArray)
            currentBeat = sound.offset
        }
        return true
    }

    private fun calculateTotalFileSize(
        pQ: PriorityQueue<Sound>,
        ctxt: Context
    ): Long {
        var totalFileSize: Long = 0
        // --------------------------------------

        // ArrayList<Byte> dataBuffer = new ArrayList<Byte>();
        val tempPQ = PriorityQueue(pQ)
        var sound: Sound
        var fileName: String? = null
        var differenceInMillisecs = 0.0
        var differenceInBeats = 0.0
        var offset = 0.0
        // int startTime = 0;
        var currentBeat = 0.0 // shows the amount of track (in millisecs)
        while (tempPQ.size > 0) {
            sound = tempPQ.poll()
            offset = sound.offset
            if (offset > currentBeat) {
                differenceInBeats = sound.offset - currentBeat
                differenceInMillisecs = ((differenceInBeats * 60 * 1000)
                        / Global.bpm.toDouble())
                val numberofsilencebytes = Math
                    .ceil(
                        myByteRate.toFloat() / 1000.00
                                * differenceInMillisecs
                    ).toInt()
                totalFileSize += numberofsilencebytes.toLong()
            }
            fileName = getFileNameFromSound(sound)
            val myMusicDataSize = getSizeFromFileName(fileName, ctxt)
            totalFileSize += myMusicDataSize
            currentBeat = sound.offset
        }
        return totalFileSize
    }

    private fun getSizeFromFileName(
        fileName: String?,
        ctxt: Context
    ): Long {
        // TODO Auto-generated method stub
        val data = read(ctxt, fileName)
        return data!!.size.toLong()
    }

    private fun getFileNameFromSound(s: Sound): String {
        var fileName = "track"
        if (!fileName.contains("/")) {
            //if FileName doesn't contain a "/" , the sound's corresponding file is in the raw folder. Remove .wav from the file name.
            fileName = fileName.substring(0, fileName.length - 4)
        }
        return fileName
    }

    fun read(ctxt: Context, fileName: String?): ByteArray? {
        var inFile: DataInputStream? = null
        var myData: ByteArray? = null
        val tmpLong = ByteArray(4)
        val tmpInt = ByteArray(2)
        var `is`: InputStream? = null
        try {
            // File projectDir = ctxt.getDir("Project1",Context.MODE_PRIVATE);
            // File myFile = new File(fileName);
            `is` = if (fileName!!.contains("/")) {
                // File projectDir = new
                // File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),"/Beats/exported/")
                // ;
                // System.out.println(projectDir.getPath());
                val myFile = File(fileName)
                FileInputStream(myFile)
            } else {
                val resourceId: Int = R.raw::class.java
                    .getDeclaredField(fileName).getInt(0)
                ctxt.resources.openRawResource(resourceId)
                // File myFile = new File("");
            }
            inFile = DataInputStream(`is`)
            val chunkID = ("" + inFile.readByte().toChar()
                    + inFile.readByte().toChar() + inFile.readByte().toChar()
                    + inFile.readByte().toChar())
            inFile.read(tmpLong) // read the ChunkSize
            myChunkSize += byteArrayToLong(tmpLong)
            val format = ("" + inFile.readByte().toChar()
                    + inFile.readByte().toChar() + inFile.readByte().toChar()
                    + inFile.readByte().toChar())
            val subChunk1ID = ("" + inFile.readByte().toChar()
                    + inFile.readByte().toChar() + inFile.readByte().toChar()
                    + inFile.readByte().toChar())
            inFile.read(tmpLong) // read the SubChunk1Size
            mySubChunk1Size = byteArrayToLong(tmpLong)
            inFile.read(tmpInt) // read the audio format. This should be 1 for
            // PCM
            if (myFormat == 0) {
                myFormat = byteArrayToInt(tmpInt)
            }
            inFile.read(tmpInt) // read the # of channels (1 or 2)
            myChannels = byteArrayToInt(tmpInt).toLong()
            inFile.read(tmpLong) // read the samplerate
            if (mySampleRate == 0L) {
                mySampleRate = byteArrayToLong(tmpLong)
            }
            inFile.read(tmpLong) // read the byterate
            if (myByteRate == 0L) {
                myByteRate = byteArrayToLong(tmpLong)
                // myByteRate = 28;
                /*
				 * Toast.makeText(ctxt, myByteRate+" 2",
				 * Toast.LENGTH_SHORT).show();
				 */
            }
            inFile.read(tmpInt) // read the blockalign
            if (myBlockAlign == 0) {
                myBlockAlign = byteArrayToInt(tmpInt)
            }
            inFile.read(tmpInt) // read the bitspersample
            if (myBitsPerSample == 0) {
                myBitsPerSample = byteArrayToInt(tmpInt)
            }
            // print what we've read so far
            // System.out.println("SubChunk1ID:" + subChunk1ID +
            // " SubChunk1Size:" + mySubChunk1Size + " AudioFormat:" + myFormat
            // + " Channels:" + myChannels + " SampleRate:" + mySampleRate);
            val dataChunkID = ("" + inFile.readByte().toChar()
                    + inFile.readByte().toChar() + inFile.readByte().toChar()
                    + inFile.readByte().toChar())
            inFile.read(tmpLong) // read the size of the data
            myDataSize = byteArrayToLong(tmpLong)

            // read the data chunk
            myData = ByteArray(myDataSize.toInt())
            inFile.read(myData)

            // close the input stream
            inFile.close()
        } catch (e: Exception) {
            return null
        }
        return myData
    }

    fun writeWaveFileHeaders(
        ctxt: Context?, fileName: String?,
        totalChunkSize: Long
    ): Boolean {
        try {
            val projectDir = File(
                Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                "/Beats/exported/"
            )
            if (!projectDir.exists()) {
                val ret = projectDir.mkdirs()
            }
            val myFile = File(projectDir, fileName)
            val outFile = DataOutputStream(
                FileOutputStream(myFile)
            )
            // ProjectManager pm = new ProjectManager();
            // pm.createProject(ctxt);
            // write the wav file per the wav file format
            outFile.writeBytes("RIFF") // 00 - RIFF
            // work around ---------------
            myChunkSize = 36 + totalChunkSize
            outFile.write(intToByteArray(myChunkSize.toInt()), 0, 4)
            outFile.writeBytes("WAVE") // 08 - WAVE
            outFile.writeBytes("fmt ") // 12 - fmt
            outFile.write(
                intToByteArray(mySubChunk1Size.toInt()),
                0,
                4
            ) // 16 - size of this chunk
            outFile.write(shortToByteArray(myFormat.toShort()), 0, 2)
            outFile.write(
                shortToByteArray(myChannels.toShort()),
                0,
                2
            ) // 22 - mono or stereo? 1 or 2?
            outFile.write(
                intToByteArray(mySampleRate.toInt()),
                0,
                4
            ) // 24 -samples per second numbers per second)
            outFile.write(intToByteArray(myByteRate.toInt()), 0, 4) // 28 - bytes
            outFile.write(
                shortToByteArray(myBlockAlign.toShort()),
                0,
                2
            ) // 32 -
            outFile.write(
                shortToByteArray(myBitsPerSample.toShort()),
                0,
                2
            ) // 34
            outFile.writeBytes("data") // 36 - data
            outFile.write(intToByteArray(totalChunkSize.toInt()), 0, 4)
            outFile.close()
        } catch (ex: Exception) {
            ex.printStackTrace()
            return false
        }
        return true
    }

    // write the output to the wav file
    fun save(
        ctxt: Context?,
        fileName: String?,
        myData: ByteArray?
    ): Boolean {
        try {
            val projectDir = File(
                Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                "/Beats/exported/"
            )
            println(projectDir.path)
            if (!projectDir.exists()) {
                val ret = projectDir.mkdirs()
            }
            val myFile = File(projectDir, fileName)
            val outFile = DataOutputStream(
                FileOutputStream(myFile, true)
            )
            outFile.write(myData) // 44 - the actual data itself - just a long
            // string of numbers
            outFile.close()
        } catch (e: Exception) {
            println(e.message)
            return false
        }
        return true
    }

    fun exportSound(
        fileName: String?,
        priorityQueue: PriorityQueue<Sound>, ctxt: Context
    ): Boolean {
        // TODO Auto-generated method stub
        val fileSize = calculateTotalFileSize(priorityQueue, ctxt)
        return if (fileSize > 1) {
            val result = writeWaveFileHeaders(ctxt, fileName, fileSize)
            if (result) {
                createDataBuffer(fileName, priorityQueue, ctxt)
                return true
            }
            false
        } else {
            false
        }
    }

    companion object {
        // ===========================
        // CONVERT BYTES TO JAVA TYPES
        // ===========================
        // these two routines convert a byte array to a unsigned short
        fun byteArrayToInt(b: ByteArray): Int {
            val start = 0
            val low: Byte = b[start] and 0xff.toByte()
            val high: Byte = b[start + 1] and 0xff.toByte()
            return (high.toInt() shl 8 or low.toInt())
        }

        // these two routines convert a byte array to an unsigned integer
        fun byteArrayToLong(b: ByteArray): Long {
            val start = 0
            var i = 0
            val len = 4
            var cnt = 0
            val tmp = ByteArray(len)
            i = start
            while (i < start + len) {
                tmp[cnt] = b[i]
                cnt++
                i++
            }
            var accum: Long = 0
            i = 0
            var shiftBy = 0
            while (shiftBy < 32) {
                accum = accum or ((tmp[i] and 0xff.toByte()) as Long shl shiftBy)
                i++
                shiftBy += 8
            }
            return accum
        }

        // ===========================
        // CONVERT JAVA TYPES TO BYTES
        // ===========================
        // returns a byte array of length 4
        private fun intToByteArray(i: Int): ByteArray {
            val b = ByteArray(4)
            b[0] = (i and 0x00FF).toByte()
            b[1] = (i shr 8 and 0x000000FF).toByte()
            b[2] = (i shr 16 and 0x000000FF).toByte()
            b[3] = (i shr 24 and 0x000000FF).toByte()
            return b
        }

        // convert a short to a byte array
        fun shortToByteArray(data: Short): ByteArray {
            return byteArrayOf(
                (data and 0xff) as Byte,
                (data.toInt() ushr 8 and 0xff) as Byte
            )
        }
    }
}