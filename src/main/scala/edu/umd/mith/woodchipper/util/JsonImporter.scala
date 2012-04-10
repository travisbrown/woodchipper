package edu.umd.mith.woodchipper.util

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import net.liftweb.json._

import bootstrap.liftweb.Boot
import edu.umd.mith.woodchipper.model._

class JsonImporter(path: String) {
  private val files = (new File(path)).listFiles.filter(_.getName.endsWith(".json"))

  def importAll {
    this.files.toIterator.map((file: File) => new BufferedReader(new FileReader(file))).foreach { reader =>
      val doc = JsonParser.parse(reader)
      val metadata = doc \ "metadata"
      val JField(_, JString(collection)) = metadata \ "collection"
      val JField(_, JString(textId)) = metadata \ "textid"
      val JField(_, JString(title)) = metadata \ "title"
      val JField(_, JString(author)) = metadata \ "author"
      val JField(_, JInt(year)) = metadata \ "date"

      val JField(_, JArray(chunks)) = doc \ "chunks"

      val text = Text.add(collection, textId, title, author, year.toInt)

      chunks.map { chunk =>
        val JField(_, JString(chunkId)) = chunk \ "metadata" \ "chunkid"
        val JField(_, JString(plain)) = chunk \ "representations" \ "plain"
        val JField(_, JString(html)) = chunk \ "representations" \ "html"
        val JField(_, JArray(topics)) = chunk \ "features" \ "topics-01"

        if (plain.trim.size > 32) {
          val document = Document.add(text, chunkId, plain, html)
          val features = topics.map {
            case JDouble(value) => value
            case JInt(value) => value.toDouble
            case _ => 0.0
          }.toArray
          document.setFeatures(features)
        }
      }
    }
  }
}

object JsonImporter {
  def main(args: Array[String]) {
    val boot = new bootstrap.liftweb.Boot
    boot.boot
    val importer = new JsonImporter(args(0))
    importer.importAll
  }
}

