package com.example.beato

import android.content.Context
import com.example.mybeat.Pattern
import org.w3c.dom.Element
import java.io.File
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
class ProjectManager {
    var name: String
    var createdBy: String
    var dt: Date
    var track: Track

    constructor() {
        name = String()
        createdBy = String()
        dt = Calendar.getInstance().time
        track = Track()
    }

    constructor(name: String, createdBy: String, dt: Date, track: Track) {
        this.name = name
        this.createdBy = createdBy
        this.dt = dt
        this.track = track
    }

    fun createProject(ctxt: Context) {
        try {
            val projectDir =
                ctxt.getDir("Project1", Context.MODE_PRIVATE)
            val myFile = File(projectDir, "ProjectDesc")
            val patternDir = File(projectDir, "Patterns")
            patternDir.mkdir()
            val trackDir = File(projectDir, "Track")
            trackDir.mkdir()
            writeProjectFileContents(myFile)
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    private fun writeProjectFileContents(fptr: File) {
        var result: StreamResult? = null
        try {
            val docFactory =
                DocumentBuilderFactory
                    .newInstance()
            val docBuilder = docFactory.newDocumentBuilder()

            // root elements
            val doc = docBuilder.newDocument()
            val rootElement = doc.createElement("Project")
            doc.appendChild(rootElement)
            val name = doc.createElement("Name")
            name.appendChild(doc.createTextNode(this.name))
            rootElement.appendChild(name)
            val createdBy = doc.createElement("CreatedBy")
            createdBy.appendChild(doc.createTextNode(this.createdBy))
            rootElement.appendChild(createdBy)
            val date = doc.createElement("Date")
            val todaysDate = Calendar.getInstance().time
            date.appendChild(doc.createTextNode(todaysDate.toString()))
            rootElement.appendChild(date)
            val patternNames: ArrayList<Pattern> = track?.patterns!!
            val patterns = doc.createElement("Patterns")
            var tempPattern: Element? = null
            for (i in patternNames.indices) {
                tempPattern = doc.createElement("Pattern")
                val patternName = doc.createElement("Name")
                patternName.appendChild(doc.createTextNode(patternNames[i].patternName))
                tempPattern.appendChild(patternName)
                val patternTempo = doc.createElement("Tempo")
                patternTempo.appendChild(doc.createTextNode("" + patternNames[i].tempo))
                tempPattern.appendChild(patternTempo)
                val patternBars = doc.createElement("Bars")
                patternBars.appendChild(doc.createTextNode("" + patternNames[i].bars))
                tempPattern.appendChild(patternBars)
                patterns.appendChild(tempPattern)
            }
            val track = doc.createElement("Track")
            val trackName = doc.createElement("TrackName")
            trackName.appendChild(doc.createTextNode(this.track.name))
            track.appendChild(trackName)
            val trackLength = doc.createElement("TrackLength")
            trackLength.appendChild(doc.createTextNode("" + this.track.length))
            track.appendChild(trackLength)
            track.appendChild(patterns)
            rootElement.appendChild(track)
            //rootElement.appendChild(patterns);
            val transformerFactory =
                TransformerFactory
                    .newInstance()
            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            val source = DOMSource(doc)
            result = StreamResult(fptr)
            transformer.transform(source, result)

            //System.out.println("File saved!");
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}