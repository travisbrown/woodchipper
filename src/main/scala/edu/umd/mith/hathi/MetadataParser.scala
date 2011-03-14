package edu.umd.mith.hathi

import java.io._
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.xml.pull._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import net.liftweb.json.Printer._

import edu.umd.mith.hathi.util.DateCleaner

class MetadataParser(val path: String, private val blacklist: Set[String])
  extends Iterable[(String, Map[String, List[String]])] {
  type FieldMap = Map[String, List[String]]
  type Record = (String, FieldMap)

  def this(path: String) = this(path, Set())

  private def readText(reader: Iterator[XMLEvent]): String = {
    val builder = new StringBuilder
    var current = reader.next
    while (
      current match {
        case EvText(text) => builder.append(text); true
        case EvEntityRef("amp") => builder.append("&"); true
        case EvEntityRef("lt") => builder.append("<"); true
        case EvEntityRef("gt") => builder.append(">"); true
        case _ => false
      }
    ) current = reader.next 
    builder.toString
  }

  private def readRecord(reader: Iterator[XMLEvent]): FieldMap = {
    val map = scala.collection.mutable.Map[String, List[String]]()
    var current = reader.next
    while (
      current match {
        case EvElemStart(_, name, _, _) => {
          val value = this.readText(reader)
          if (!this.blacklist.contains(name)) {
            map(name) = value :: map.getOrElse(name, List())
          }
          true
        }
        case _ => false
      }
    ) current = reader.next
    map.toMap
  }

  def iterator: Iterator[Record] = {
    val reader = new XMLEventReader(Source.fromFile(this.path)).filter {
      case _: EvElemStart => true
      case _: EvElemEnd => true
      case _: EvEntityRef => true
      case EvText(text) => text.trim.length > 0
      case _ => false
    }

    reader.next
    var current = reader.next

    new Iterator[Record] {
      def hasNext: Boolean = current match {
        case EvElemStart(_, "record", attrs, _) => true
        case _ => false
      }

      def next: Record = {
        val EvElemStart(_, "record", attrs, _) = current
        val record = MetadataParser.this.readRecord(reader)
        current = reader.next
        (attrs.asAttrMap("id"), record)
      }
    }
  }
}

class TextSelector(
  path: String,
  blacklist: Set[String],
  private val range: (Int, Int),
  private val language: String,
  private val rights: String)
  extends MetadataParser(path, blacklist) {

  private val dateCleaner = new DateCleaner

  def latestYear(parsed: (Int, Option[Int])): Int = parsed match {
    case (start: Int, Some(end: Int)) => Math.max(start, end)
    case (start: Int, None) => start
  }

  def validDate(fields: Map[String, List[String]]): Boolean = {
    fields.get("date").flatMap { v: List[String] => this.dateCleaner.parseYearField(v(0)) }.map(this.latestYear(_)) match {
        case Some(year: Int) => year >= this.range._1 && year < this.range._2
        case _ => false
    }
  }

  def validLanguage(fields: Map[String, List[String]]): Boolean = {
    fields.get("language") match {
      case Some(List(language)) => language == this.language
      case _ => false
    }
  }
  
  def validRights(fields: Map[String, List[String]]): Boolean = {
    fields.get("rights") match {
      case Some(List(rights)) => rights == this.rights
      case _ => false
    }
  }

  override def iterator: Iterator[Record] = super.iterator.filter {
    case (_, fields) => this.validDate(fields) && this.validLanguage(fields) && this.validRights(fields)
  }
}

class SelectionSelector(
  path: String,
  blacklist: Set[String],
  selectedIds: Set[String])
  extends MetadataParser(path, blacklist) {

  override def iterator: Iterator[Record] = super.iterator.filter {
    case (id, fields) => selectedIds.contains(id)
  }
}

class Dedup(wrapped: Iterable[(String, Map[String, List[String]])])
  extends Iterable[(String, Map[String, List[String]])] {

  val seen = scala.collection.mutable.Set[String]()

  override def iterator: Iterator[(String, Map[String, List[String]])] = wrapped.iterator.filter {
    case (_, fields: Map[String, List[String]]) => {
      val bibs = fields.getOrElse("identifier", List("_")).filter {
        case value => value.startsWith("(BIB)")
      }

      val dup = (bibs.size > 0) && seen.contains(bibs(0)) 

      if (bibs.size > 0) {
        println(bibs(0))
        seen += bibs(0)
      }

      !dup
    }
  }
}

object HathiExtractor {
  def main(args: Array[String]) {

  }
}

object MetadataParser {

  private def latestYear(parsed: (Int, Option[Int])): Int = parsed match {
    case (start: Int, Some(end: Int)) => Math.max(start, end)
    case (start: Int, None) => start
  }

  def main(args: Array[String]) {
    val dp = new DateCleaner
    val hc = new HathiCollection(args(1))

    val selector = if (args.length > 3) {
      val selection = Source.fromFile(args(3)).getLines.map(_.trim).filterNot(_.isEmpty).toSet
      new SelectionSelector(args(0), Set("description"), selection)
    } else {
      new TextSelector(args(0), Set("description"), (0, 1850), "eng", "pd")
    }

    selector.foreach {
      case (id: String, metadata: Map[String, List[String]]) => {
        val year = metadata.get("date").flatMap { v: List[String] => dp.parseYearField(v(0)) }.map(latestYear(_)) match {
          case Some(year) => year
        }

        val pages = hc.findTextInfo(id) match {
          case Some(info) => {
            hc.extractPages(info).map {
              case (page, content) => {
                val meta = JObject(List(JField("chunkid", "%03d".format(page))))
                val representations = JObject(List(JField("plain", content),
                                                   JField("html", content.replaceAll("\n", "<br />"))))
                JObject(List(JField("metadata", meta), JField("representations", representations)))
              }
            }.toList
          }
        }

        val text = JObject(List(JField("metadata", JObject(List(JField("textid", id),
                                            JField("collection", "hathi"),
                                            JField("title", metadata.getOrElse("title", List("_"))(0)),
                                            JField("author", metadata.getOrElse("creator", List("_"))(0)),
                                            JField("date", year)))),
                                JField("chunks", JArray(pages))))

        val (col, tex) = hc.escape(id)
        val writer = new BufferedWriter(new FileWriter(new File(args(2), col + "." + tex + ".json")))
        writer.write(net.liftweb.json.Printer.pretty(render(text)))
        writer.close()
      }
    }
  }
}

