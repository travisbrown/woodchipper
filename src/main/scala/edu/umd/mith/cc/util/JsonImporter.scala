package edu.umd.mith.cc {
package util {

import java.io.File
import scala.io.Source
import net.liftweb.json._
import net.liftweb.json.JsonAST._

import _root_.bootstrap.liftweb.Boot

import edu.umd.mith.cc.model._

class JsonImporter(path: String) {
  (new File(path)).listFiles.foreach { println(_) }
  private val files = (new File(path)).listFiles.filter(_.getName.endsWith(".json"))

  def importAll {
    this.files.toIterator.map(Source.fromFile(_).mkString).foreach { contents =>
      val doc = JsonParser.parse(contents)
      val JField(_, JString(collection)) = doc \ "metadata" \ "collection"
      val JField(_, JString(textId)) = doc \ "metadata" \ "textid"
      val JField(_, JString(title)) = doc \ "metadata" \ "title"
      val JField(_, JString(author)) = doc \ "metadata" \ "author"
      val JField(_, JInt(year)) = doc \ "metadata" \ "date"

      val JField(_, JArray(chunks)) = doc \ "chunks"

      val text = Text.add(collection, textId, title, author, year.toInt)

      chunks.map { chunk =>
        val JField(_, JString(chunkId)) = chunk \ "metadata" \ "chunkid"
        val JField(_, JString(plain)) = chunk \ "representations" \ "plain"
        val JField(_, JString(html)) = chunk \ "representations" \ "html"

        val document = Document.add(text, chunkId, plain, html) 
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

}
}
