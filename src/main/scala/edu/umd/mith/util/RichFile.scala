package edu.umd.mith.util

import java.io.File

class RichFile(private val file: File) {
  def leaves: Iterator[File] = {
    val children = this.file.listFiles.sorted
    if (children.forall(_.isDirectory)) {
      children.toIterator.flatMap(new RichFile(_).leaves)
    } else {
      Iterator(this.file)
    }
  }
}

