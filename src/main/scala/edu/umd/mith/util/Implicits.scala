package edu.umd.mith.util

import java.io.File
import java.util.Enumeration

object Implicits {
  implicit def enrichEnumeration[A](enumeration: Enumeration[A]): RichEnumeration[A] =
    new RichEnumeration(enumeration)

  implicit def enrichFile(file: File): RichFile = new RichFile(file)
}

