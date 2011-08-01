package edu.umd.mith.cc.util

import java.io._
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.regex._
import net.liftweb.json._
//import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import scala.io.Source

class Corrector(lang: String) {
  def this() = this("en")
  private val words = {
    val source = Source.fromInputStream(this.getClass.getResourceAsStream("corrections.%s.txt".format(lang)))
    val lines = source.getLines.toList
    source.close
    lines.map { line =>
      val Array(wrong, right) = line.split("""\s""")
      val pattern = Pattern.compile("""\b""" + wrong + """\b""")
      (text: String) => pattern.matcher(text).replaceAll(right)
    }
  }

  def correct(text: String) = (text /: this.words) {
    case (current, replacer) => replacer(current)
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
      val JString(collection) = metadata \ "collection"
      val JString(textId) = metadata \ "textid"
      val JString(title) = metadata \ "title"
      val JString(author) = metadata \ "author"
      val JInt(year) = metadata \ "date"
      //val JField(_, JString(collection)) = metadata \ "collection"
      //val JField(_, JString(textId)) = metadata \ "textid"
      //val JField(_, JString(title)) = metadata \ "title"
      //val JField(_, JString(author)) = metadata \ "author"
      //val JField(_, JInt(year)) = metadata \ "date"

      val JArray(chunks) = doc \ "chunks"

      val text = JsonText(textId, collection, title, author, year.toInt,
        chunks.map { chunk =>
          val JString(chunkId) = chunk \ "metadata" \ "chunkid"
          val JString(plain) = chunk \ "representations" \ "plain"
          val JString(html) = chunk \ "representations" \ "html"
          JsonDocument(chunkId, plain, html, Map())
        }
      )

      reader.close
      text
    } 
  }

  def withFiles = this.files.iterator.zip(this.iterator)

  private val corrector = new Corrector
  private val wsPattern = Pattern.compile("""\s+""")

  def documentToMallet(text: JsonText, document: JsonDocument) = {
    val content = wsPattern.matcher(document.plain.trim.split("\n").drop(3).mkString("\n")).replaceAll(" ")
    val corrected = if (text.year < 1800) this.corrector.correct(content) else content
    "%s~%s _ %s".format(text.id, document.id, corrected)
  }

  def asMallet: Iterator[String] = {
    this.iterator.flatMap { text =>
      text.documents.iterator.map(this.documentToMallet(text, _))
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
        val writer = new BufferedWriter(new FileWriter(args(2)))
        manager.asMallet.foreach((line: String) => writer.write(line + "\n"))
        writer.close
      }
      case "mallet-partition-export" => {
        val manager = new JsonManager(args(1))
        val output = new File(args(2))
        val n = args(3).toInt
        val writers = (0 until n).map { i =>
          new BufferedWriter(new FileWriter(new File(output, "%03d.txt".format(i))))
        }
        
        manager.iterator.zip(Stream.continually((0 until n).toStream).flatten.toIterator).foreach {
          case (text, i) => {
            printf("%03d: %s\n", i, text.id)
            text.documents.iterator.foreach { document =>
              writers(i).write(manager.documentToMallet(text, document))
              writers(i).newLine
            }
          }
        }

        writers.map(_.close)
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

