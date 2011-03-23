package edu.umd.mith.hathi

import java.io.File
import scala.io.Source

import edu.umd.mith.util.Implicits._
import edu.umd.mith.util.ZipReader

case class HathiTextInfo(
  val id: String,
  val metsFile: File,
  val zipFile: File
)

class HathiCollection(private val base: String) {
  def escape(id: String): (String, String) = {
    val first = id.indexOf(".")
    val collection = id.substring(0, first)
    val remainder = id.substring(first + 1)
    val dirName = remainder.replaceAll("""\.""", ",")
                           .replaceAll("""\:""", "+")
                           .replaceAll("""\/""", "=")
    (collection, dirName)
  }

  def unescape(dirName: String) =
    dirName.replaceAll("""\,""", ".")
           .replaceAll("""\+""", ":")
           .replaceAll("""\=""", "/")

  def texts: Iterator[HathiTextInfo] = {
    (new File(this.base)).listSortedFiles.toIterator.flatMap { collection =>
      (new File(collection, "pairtree_root")).leaves.map { path =>
        val metsFile = new File(path, path.getName + ".mets.xml")
        val zipFile = new File(path, path.getName + ".zip")
        assert(metsFile.exists)
        assert(zipFile.exists) 
        HathiTextInfo(
          collection.getName + "." + this.unescape(path.getName),
          metsFile,
          zipFile
        )
      }
    }
  }

  private def split2(dirName: String): List[String] = dirName.length match {
    case 0 => Nil
    case 1 => List(dirName)
    case _ => dirName.substring(0, 2) :: this.split2(dirName.substring(2))
  }

  def findTextInfo(id: String): Option[HathiTextInfo] = {
    val (collection, name) = this.escape(id)
    val parts = collection :: "pairtree_root" :: (this.split2(name) :+ name)
    val path = new File(this.base, parts.mkString(File.separator))
    if (path.exists) {
      val metsFile = new File(path, path.getName + ".mets.xml")
      val zipFile = new File(path, path.getName + ".zip")
      if (metsFile.exists && zipFile.exists) {
        Some(HathiTextInfo(id, metsFile, zipFile))
      } else None
    } else {
      System.err.println("ERROR: no such file: " + path)
      None
    }
  }

  def extractPages(text: HathiTextInfo): Iterator[(Int, String)] = {
    val reader = new ZipReader(text.zipFile)
    reader.iterator.map {
      case (path, source) => {
        val Array(_, name) = path.split("""\/""")
        val Array(number, _) = name.split("""\.""")
        (number.toInt, source.mkString)
      }
    }
  }

  def malletOutput(id: String): Option[Iterator[String]] = {
    this.findTextInfo(id).map { 
      this.extractPages(_).map {
        case (number, content) => {
          id + "." +
          number + " _ " +
          content.replaceAll("\n", " ")
        }
      }
    }
  }
}

object HathiCollection {
  def main(args: Array[String]) {
    val e = new HathiCollection(args(0))
    val f = new File(args(1))
    if (f.exists) {
      Source.fromFile(f).getLines.foreach {
        e.malletOutput(_).foreach(_.foreach(println(_)))
      }
    } else {
      e.findTextInfo(args(1)) match {
        case Some(info) => e.extractPages(info).foreach {
          case (page, content) => printf("%06d ==============\n%s\n", page, content)
        }
        case None => println("No such file.")
      }
    }
  }
}

