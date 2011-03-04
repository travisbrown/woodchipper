package edu.umd.mith.cc {
package util {

import java.io._
import _root_.bootstrap.liftweb.Boot
import _root_.net.liftweb.mapper._

import edu.umd.mith.cc.model._

class MalletExporter(collection: String, count: Int) {
  def printSamples(writer: BufferedWriter) {
    val coll = Collection.findAll(By(Collection.name, collection))(0)
    val texts = Text.findAll(By(Text.collection, coll.id), OrderBy(Text.uuid, Ascending), MaxRows(count))
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
    val exporter = new MalletExporter(args(0), args(1).toInt)
    exporter.printSamples(new BufferedWriter(new FileWriter(args(2))))
  }
}

}
}
