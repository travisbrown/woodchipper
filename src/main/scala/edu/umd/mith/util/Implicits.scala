package edu.umd.mith.util

import java.io.File

object Implicits {
  implicit def enrichFile(file: File): RichFile = new RichFile(file)
}

