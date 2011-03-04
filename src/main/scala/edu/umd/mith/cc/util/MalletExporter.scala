package edu.umd.mith.cc {
package util {

import java.io._
import _root_.bootstrap.liftweb.Boot
import _root_.net.liftweb.mapper._

import edu.umd.mith.cc.model._

class MalletExporter(collection: String, random: Boolean, count: Int) {
  def this(collection: String, random: Boolean) = this(collection, random, scala.Int.MaxValue)
  def this(collection: String) = this(collection, true)

  def printSamples(writer: BufferedWriter) {
    val coll = Collection.findAll(By(Collection.name, collection))(0)
    val order = if (this.random) OrderBy(Text.uuid, Ascending) else OrderBy(Text.uid, Ascending)
    val texts = Text.findAll(By(Text.collection, coll.id), order, MaxRows(count))
    texts.foreach { text =>
      val documents = Document.findAll(By(Document.text, text.id), OrderBy(Document.id, Ascending))
      documents.foreach { document =>
        writer.write("%s#%s _ %s\n".format(text.uid.is, document.uid.is, document.plain.is.replaceAll("""\s+""", " ")))
      }
    }
  }
}

object MalletExporter {
  def main(args: Array[String]) {
    val boot = new bootstrap.liftweb.Boot
    boot.boot

    val exporter = if (args.size == 2) {
      new MalletExporter(args(1), false)
    } else {
      new MalletExporter(args(1), true, args(2).toInt)
    }

    exporter.printSamples(new BufferedWriter(new FileWriter(args(0))))
  }
}

}
}
