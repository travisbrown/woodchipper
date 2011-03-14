package edu.umd.mith.cc.util

import java.io._
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import net.liftweb.json._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import scala.io._

class Corrector(lang: String) {
  def this() = this("en")
  private val words = {
    val source = Source.fromInputStream(this.getClass.getResourceAsStream("corrections.%s.txt".format(lang)))
    val lines = source.getLines.toList
    source.close
    lines.map { line =>
      val Array(wrong, right) = line.split("""\s""")
      (wrong, right)
    }
  }

  def correct(text: String) = (text /: this.words) {
    case (current, (wrong, right)) => current.replaceAll("""\b""" + wrong + """\b""", right)
  }
}

trait Jsonable {
  def toJson: JObject

  override def toString = pretty(render(this.toJson))
}

case class JsonText(
  id: String,
  collection: String,
  title: String,
  author: String,
  year: Int,
  documents: List[JsonDocument]
) extends Jsonable with Iterable[JsonDocument] {
  def iterator = this.documents.iterator
  
  def toJson: JObject = {
    val metadata = ("collection" -> this.collection) ~
                   ("textid" -> this.id) ~
                   ("title" -> this.title) ~
                   ("author" -> this.author) ~
                   ("date" -> this.year)
    ("metadata" -> metadata) ~ ("chunks" -> this.documents.map(_.toJson))
  }
}

case class JsonDocument(
  id: String,
  plain: String,
  html: String,
  features: Map[String, IndexedSeq[Double]]
) extends Jsonable {
  def toJson: JObject = {
    val doc = ("metadata" -> ("chunkid" -> this.id)) ~
              ("representations" -> ("plain" -> this.plain) ~
                                    ("html" -> this.html))
    if (features.isEmpty) doc else {
      doc ~ ("features" -> (JObject(Nil) /: features)(_ ~ _))
    }
  }
}

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
          JsonDocument(chunkId, plain, html, Map())
        }
      )
    } 
  }

  def withFiles = this.files.iterator.zip(this.iterator)

  def asMallet: Iterator[String] = {
    val corrector = new Corrector

    this.iterator.flatMap { text =>
      text.documents.iterator.filter { document =>
        true //document.plain.trim.length > 64
      }.map { document =>
        "%s~%s _ %s".format(text.id, document.id, corrector.correct(document.plain.trim.replaceAll("""\s+""", " ")))
      }
    }
  }

  def withFeatures(name: String, features: Map[String, Seq[IndexedSeq[Double]]]): Iterator[(File, JsonText)] = {
    this.files.iterator.zip {
      this.iterator.map { text =>
        features.get(text.id) match {
          case None => text
          case Some(textFeatures) => {
            text.copy(documents = text.documents.zip(textFeatures).map { case (document, documentFeatures) =>
             document.copy(features = document.features.updated(name, documentFeatures))
            })
          }
        }
      }
    }
  }

  def withMalletFeatures(name: String, source: Source): Iterator[(File, JsonText)] = {
    var features = scala.collection.mutable.Map[String, Seq[IndexedSeq[Double]]]()
    source.getLines.foreach { line =>
      if (!line.startsWith("#")) {
      val fields = line.split("""\s""")
      val topics = (fields.size - 2) / 2
      val Array(text, document) = fields(1).split("~")
      val v = Array.fill(topics)(0.0)

      for (i <- 2 to (fields.size - 1)) {
        if (i % 2 == 0) {
          v(fields(i).toInt) = fields(i + 1).toDouble
        }
      }
      if (!features.contains(text)) {
        features(text) = Seq[IndexedSeq[Double]]()
      }
      features(text) = features(text) :+ v.toIndexedSeq
    }
    }

    this.withFeatures(name, features.toMap)
  }

  def withMalletFeatures(name: String, path: String): Iterator[(File, JsonText)] = this.withMalletFeatures(name, Source.fromFile(path))
}

object JsonManager {
  def main(args: Array[String]) {
    args(0) match {
      case "echo" => echo(args(1), args(2))
      case "mallet-export" => {
        val manager = new JsonManager(args(1))
        manager.asMallet.foreach(println(_))
      }
      case "mallet-add-features" => {
        val manager = new JsonManager(args(2))
        manager.withMalletFeatures("topics-01", args(1)).foreach { 
          case (file, text) => {
            val out = new File(args(3), file.getName)
            val writer = new BufferedWriter(new FileWriter(out))
            writer.write(pretty(render(text.toJson)))
            writer.close
          }
        }
      }
    }
  }

  def echo(inPath: String, outPath: String) {
    val manager = new JsonManager(inPath)
    manager.withFiles.foreach {
      case (file, text) => {
        val out = new File(outPath, file.getName)
        val writer = new BufferedWriter(new FileWriter(out))
        writer.write(pretty(render(text.toJson)))
        writer.close
      }
    }
  }
}

