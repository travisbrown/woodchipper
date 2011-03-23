package edu.umd.mith.util

import java.util.Enumeration

class RichEnumeration[A](enumeration: Enumeration[A]) extends Iterator[A] {
  def hasNext: Boolean =  enumeration.hasMoreElements()
  def next: A = enumeration.nextElement()
}

