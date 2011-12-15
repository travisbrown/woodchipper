package edu.umd.mith.hathi.ui

import java.io.File
import scala.io.Source

import edu.umd.mith.hathi._
import org.clapper.argot._

object HathiBrowser {

  import ArgotConverters._
  import edu.umd.mith.util.Implicits._

  val parser = new ArgotParser("hathib", preUsage=Some("Version 0.0.1"))

  val action = this.parser.option[String](
    List("a", "action"),
    "action",
    "Action to perform on the Hathi collection.")

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

