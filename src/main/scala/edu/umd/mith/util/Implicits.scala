package edu.umd.mith.util

import java.io.File

import org.clapper.argot.CommandLineArgument

object Implicits {
  implicit def enrichFile(file: File): RichFile = new RichFile(file)

  implicit def convertFileParameter(s: String, opt: CommandLineArgument[File]): File = {
    val file = new File(s)
    if (!file.exists)
      opt.parent.usage("Input file \"%s\" does not exist.".format(s))
    else file
  }
}

