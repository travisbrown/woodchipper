package edu.umd.mith.util

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

import edu.umd.mith.util.Implicits._

class ZipReader(file: File) extends Iterable[(String, Source)] {
  private val zipped = new ZipFile(file)

  def iterator: Iterator[(String, Source)] = 
    this.zipped.entries.toSeq.sortWith(_.getName < _.getName).iterator.map {
      entry => (entry.getName, Source.fromInputStream(this.zipped.getInputStream(entry)))
    }

  def close() {
    this.zipped.close()
  }
}

