package edu.umd.mith.hathi

import java.io.File
import scala.io.Source

import edu.umd.mith.util.Implicits._
import edu.umd.mith.util.ZipReader

case class HathiEntry(
  val id: String,
  val metsFile: File,
  val zipFile: File
)

class HathiCollection(private val base: File) extends Iterable[HathiEntry] {
  def this(basePath: String) = this(new File(basePath))

  def escape(id: String): (String, String) = {
    val first = id.indexOf('.')
    val collection = id.substring(0, first)
    val remainder = id.substring(first + 1)
    val dirName = remainder.replace('.', ',')
                           .replace(':', '+')
                           .replace('/', '=')
    (collection, dirName)
  }

  def unescape(dirName: String) =
    dirName.replace(',', '.')
           .replace('+', ':')
           .replace('=', '/')

  def iterator: Iterator[HathiEntry] = {
    this.base.listSortedFiles.toIterator.flatMap { collection =>
      (new File(collection, "pairtree_root")).leaves.map { path =>
        val metsFile = new File(path, path.getName + ".mets.xml")
        val zipFile = new File(path, path.getName + ".zip")
        assert(metsFile.exists)
        assert(zipFile.exists) 
        HathiEntry(
          collection.getName + "." + this.unescape(path.getName),
          metsFile,
          zipFile
        )
      }
    }
  }

  def find(id: String): Option[HathiEntry] = {
    val (collection, name) = this.escape(id)
    val parts = collection :: "pairtree_root" :: (name.grouped(2).toList :+ name)
    val path = new File(this.base, parts.mkString(File.separator))
    if (path.exists) {
      val metsFile = new File(path, path.getName + ".mets.xml")
      val zipFile = new File(path, path.getName + ".zip")
      if (metsFile.exists && zipFile.exists) {
        Some(HathiEntry(id, metsFile, zipFile))
      } else None
    } else {
      System.err.println("ERROR: no such file: " + path)
      None
    }
  }

  def extractPages(text: HathiEntry): Iterator[(Int, String)] = {
    val reader = new ZipReader(text.zipFile)
    // We drop the first item returned, since it's just the container.
    reader.iterator.drop(1).map {
      case (path, source) => {
        val Array(_, name) = path.split("""\/""")
        val Array(number, _) = name.split("""\.""")
        (number.toInt, source.mkString)
      }
    }
  }

  def extractPages(id: String): Option[Iterator[(Int, String)]] = {
    this.find(id).map(this.extractPages)
  }

  def formatMallet(id: String): Option[Iterator[String]] = {
    this.find(id).map { 
      this.extractPages(_).map {
        case (number, content) => "%s.%06d _ %s".format(
          id, number, content.replaceAll("\n", " ")
        )
      }
    }
  }
}

