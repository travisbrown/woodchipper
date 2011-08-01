package edu.umd.mith.hathi.ui

import java.io._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import net.liftweb.json.Printer._


import scala.io.Source
import org.clapper.argot._

import edu.umd.mith.hathi._
import edu.umd.mith.hathi.util.DateCleaner

object HathiBrowser {

  import ArgotConverters._
  import edu.umd.mith.util.Implicits._

  val parser = new ArgotParser("hathib", preUsage=Some("Version 0.0.1"))

  val action = this.parser.option[String](
    List("a", "action"),
    "action",
    "Action to perform on the Hathi collection.")

  val output = this.parser.option[File](
    List("o", "output"),
    "output",
    "Directory or file for output.")

  val collection = this.parser.option[HathiCollection](
    List("c", "collection"),
    "collection",
    "Base directory of Hathi collection.") { (s, opt) => 
    val dir = new File(s)
    if (!dir.exists || !dir.isDirectory) {
      opt.parent.usage("Error: %s is not a directory.".format(s))
    }
    new HathiCollection(dir)
  }

  val metadata = this.parser.option[MetadataParser](
    List("m", "metadata"),
    "metadata",
    "Metadata file for Hathi collection.") { (s, opt) =>
    val file = new File(s)
    if (!file.exists) {
      opt.parent.usage("Error: %s is not a file.".format(s))
    }
    new MetadataParser(file)
  }

  val id = this.parser.option[String](
    List("i", "id"),
    "id",
    "Hathi identifier.")

  val file = this.parser.option[File](
    List("f", "file"),
    "file",
    "Additional input file (use varies depending on action).") 

  def run() {
    this.action.value match {
      case Some("wc") => {
        var i = 0
        var c = 0

        this.collection.value.map { coll => coll.foreach { entry =>
          val pages = coll.extractPages(entry).map {
            case (_, content) => content.split("\\s").length
          }

          val count = pages.sum
          c += count

          println("%10d (%s): %16d %16d".format(i, entry.id, count, c)) 
          i += 1
        }}
      }
      case Some("malletize") => {
        this.id.value match {
          case Some(id) => 
            this.collection.value.get.formatMallet(id).foreach(_.foreach(println(_)))
          case None => {}
        }
        this.file.value match {
          case Some(f) => {
            Source.fromFile(f).getLines.foreach {
              this.collection.value.get.formatMallet(_).foreach(_.foreach(println(_)))
            }
          }
          case None => {}
        }
      }
      case Some("json") => {
        this.id.value match {
          case Some(id) => 
            this.collection.value.get.formatMallet(id).foreach(_.foreach(println(_)))
          case None => {}
        }
        this.file.value match {
          case Some(f) => {
            val dp = new DateCleaner

            val ids = Source.fromFile(f).getLines.toSet
            val info = this.metadata.value.get.iterator.filter {
              case (id, _) => ids.contains(id)
            }.toMap

            def latestYear(parsed: (Int, Option[Int])): Int = parsed match {
              case (start: Int, Some(end: Int)) => math.max(start, end)
              case (start: Int, None) => start
            }

            ids.foreach { id =>
              val meta = info(id)
              val year = meta.get("date").flatMap { v: List[String] => dp.parseYearField(v(0)) }.map(latestYear(_)) match {
                case Some(year) => year
                case None => 0
              }

              val json = this.collection.value.get.formatJson(id, meta.getOrElse("title", List(""))(0), meta.getOrElse("creator", List(""))(0), year)
              val (collection, text) = this.collection.value.get.escape(id)
              val writer = new BufferedWriter(new FileWriter(new File(this.output.value.get, collection + "." + text + ".json")))
              pretty(render(json), writer)
              writer.close()
            }
          }
          case None => {}
        }
      }
    }
  }

  def main(args: Array[String]) {
    try {
      this.parser.parse(args)
      this.run
    } catch {
      case e: ArgotUsageException => println(e.message)
    }
  }
}

