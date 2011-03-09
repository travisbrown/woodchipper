package edu.umd.mith.cc.util

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import net.liftweb.json._
import net.liftweb.json.JsonAST._

case class JsonText(
  id: String,
  collection: String,
  title: String,
  author: String,
  year: Int,
  documents: List[JsonDocument]
) extends Iterable[JsonDocument] {
  def iterator = this.documents.iterator
}

case class JsonDocument(
  id: String,
  plain: String,
  html: String
)

class JsonManager(
  private val files: Iterable[File]
) extends Iterable[JsonText] {
  def this(path: String) = this {
    val files = (new File(path)).listFiles.filter(_.getName.endsWith(".json"))
    java.util.Arrays.sort(files, new java.util.Comparator[File] {
      def compare(f1: File, f2: File): Int = f1.getPath.compareTo(f2.getPath)
    })
    files.toIterable
  }

  def iterator: Iterator[JsonText] = {
    this.files.iterator.map { file =>
      val reader = new BufferedReader(new FileReader(file))
      val doc = JsonParser.parse(reader)
      val metadata = doc \ "metadata"
      val JField(_, JString(collection)) = metadata \ "collection"
      val JField(_, JString(textId)) = metadata \ "textid"
      val JField(_, JString(title)) = metadata \ "title"
      val JField(_, JString(author)) = metadata \ "author"
      val JField(_, JInt(year)) = metadata \ "date"

      val JField(_, JArray(chunks)) = doc \ "chunks"

      JsonText(textId, collection, title, author, year.toInt,
        chunks.map { chunk =>
          val JField(_, JString(chunkId)) = chunk \ "metadata" \ "chunkid"
          val JField(_, JString(plain)) = chunk \ "representations" \ "plain"
          val JField(_, JString(html)) = chunk \ "representations" \ "html"
          JsonDocument(chunkId, plain, html)
        }
      )
    } 
  }

  def asMallet: Iterator[String] = {
    this.iterator.flatMap { text =>
      text.documents.iterator.map { document =>
        "%s~%s _ %s".format(text.id, document.id, document.plain.trim.replaceAll("""\s+""", " "))
      }
    }
  }
}

object JsonManager {
  def main(args: Array[String]) {
    val manager = new JsonManager(args(0))
    manager.asMallet.foreach(println(_))
  }
}

