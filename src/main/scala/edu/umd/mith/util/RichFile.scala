package edu.umd.mith.util

import java.io.File
import Implicits._

class RichFile(private val file: File) {
  private val fileComparator = new java.util.Comparator[File] {
    def compare(f1: File, f2: File) = f1.compareTo(f2)
  }

  def listSortedFiles: Array[File] = {
    val children = this.file.listFiles
    java.util.Arrays.sort(children, this.fileComparator)
    children
  }

  def leaves: Iterator[File] = {
    val children = this.file.listSortedFiles
    if (children.forall(_.isDirectory)) {
      children.toIterator.flatMap(_.leaves)
    } else {
      Iterator(this.file)
    }
  }
}

