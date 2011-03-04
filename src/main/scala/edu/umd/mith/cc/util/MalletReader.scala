package edu.umd.mith.cc {
package util {

import java.io._
import _root_.bootstrap.liftweb.Boot
import _root_.net.liftweb.mapper._

import edu.umd.mith.cc.model._

class MalletReader(collection: String) {
  def readFeatures(reader: BufferedReader) {
    reader.readLine
    val coll = Collection.findAll(By(Collection.name, collection))(0)
    val order = OrderBy(Text.uuid, Ascending)
    val texts = Text.findAll(By(Text.collection, coll.id), order)
    texts.foreach { text =>
      val documents = Document.findAll(By(Document.text, text.id), OrderBy(Document.id, Ascending))
      documents.foreach { document =>
        val fields = reader.readLine.trim.split(" ")
        val tops = (fields.size - 2) / 2
        val features = Array.ofDim[Double](tops)
        val valid = fields.slice(2, fields.size)
        var last: Option[Int] = None
        valid.foreach { s =>
          last match {
            case None => last = Some(s.toInt)
            case Some(fn) => features(fn) = s.toDouble; last = None
          }
        }
        document.setFeatures(features)
      }
    }
  }
}

object MalletReader {
  def main(args: Array[String]) {
    val boot = new bootstrap.liftweb.Boot
    boot.boot

    if (args.size == 0) {
      val fs = Feature.findAll
      println("There are " + fs.size + " features")
      fs.foreach { f => f.delete_! }
    } else {
      val reader = new MalletReader(args(1))
      reader.readFeatures(new BufferedReader(new FileReader(args(0))))
    }
  }
}

}
}
