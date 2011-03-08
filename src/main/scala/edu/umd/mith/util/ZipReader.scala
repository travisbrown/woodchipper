package edu.umd.mith.util

import java.io.File

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

import scala.collection.mutable.ArrayBuffer
import scala.io.BufferedSource
import scala.io.Source

class ZipReader(file: File) {
  private val zipped = new ZipFile(file)

  def contents: Iterator[(String, Source)] = {
    val entries = new ArrayBuffer[ZipEntry](this.zipped.size)
    val enumerator = this.zipped.entries
    while (enumerator.hasMoreElements) {
      val entry = enumerator.nextElement
      if (!entry.isDirectory) entries += entry
    }

    entries.sortWith(_.getName < _.getName).iterator.map { entry =>
      (entry.getName, new BufferedSource(this.zipped.getInputStream(entry)))
    }
  }

  def close() {
    this.zipped.close()
  }
}

