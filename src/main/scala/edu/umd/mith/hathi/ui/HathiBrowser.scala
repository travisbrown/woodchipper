package edu.umd.mith.hathi.ui

/*import org.apache.felix.gogo.commands.{
  Action,
  Option => option,
  Argument => argument,
  Command => command
}
import org.apache.karaf.shell.console.OsgiCommandSupport
import org.apache.felix.gogo.runtime.CommandShellImpl
import org.apache.felix.service.command.CommandSession
import org.apache.karaf.shell.console.Main
import org.apache.karaf.shell.console.jline.Console
import org.osgi.service.command.CommandSession*/
import java.io.File

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

/*object HathiCollection {
  def main(args: Array[String]) {
    val e = new HathiCollection(args(0))
    val f = new File(args(1))
    if (f.exists) {
      Source.fromFile(f).getLines.foreach {
        e.formatMallet(_).foreach(_.foreach(println(_)))
      }
    } else {
      e.find(args(1)) match {
        case Some(info) => e.extractPages(info).foreach {
          case (page, content) => printf("%06d ==============\n%s\n", page, content)
        }
        case None => println("No such file.")
      }
    }
  }
}*/

