package edu.umd.mith.cc.snippet

import _root_.scala.xml.NodeSeq
import _root_.net.liftweb.http._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.mapper._
import _root_.java.util.Date
import edu.umd.mith.cc.lib._
import edu.umd.mith.cc.model._
import edu.umd.mith.cc.analysis._
import Helpers._
import net.liftweb.http.js.JsCmds._
import js._
import JsCmds._
import JE._
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import net.liftweb.json.Printer.pretty 

case class JsCompactDouble(d: Double, n: Int) extends JsExp {
  def this(d: Double) = this(d, 6)
  def toJsCmd = JsCompactDouble.formatters(n).format(d)
}

object JsCompactDouble {
  import java.text.DecimalFormat
  val formatters = new DecimalFormat("#") +: (1 until 16).map { n =>
    new DecimalFormat("#." + "#" * n)
  }
}

class PCAViz {
  /*def data: Array[Array[Double]] 
  def variance: Array[Double]
  def loadings: Array[Array[Double]]
  def k: Int = this.variance.length
  def n = this.data.length
  def m = this.variance(0).length*/
  def k: Int = 8;
  def precision: Int = 6
  def view: ((Double, Double), (Double, Double)) = ((-0.4, 0.4), (-0.4, 0.4))

  /*assert(this.data.length > 0)
  assert(this.data(0).length == this.variance.length)
  assert(this.variance.length == this.loadings.length)*/

  def convertDouble1DArray(a: Array[Double]): JsExp = {
    new JsArray(a.map(JsCompactDouble(_, this.precision)).toList)
  }

  def convertDouble1DArrayWithIndex(a: Array[Double]): JsExp = {
    new JsArray(a.zipWithIndex.map { case (x, i) =>
      JsArray(Num(i + 1), JsCompactDouble(x, this.precision))
    }.toList)
  }

  def convertDouble2DArray(a: Array[Array[Double]]): JsExp = {
    new JsArray(a.map { r =>
      new JsArray(r.map(JsCompactDouble(_, this.precision)).toList)
    }.toList)
  }

  private def shortenTitle(title: String) = {
    title.substring(0, math.min(60, title.length)) + "..."
  }

  def renderData(in: NodeSeq): NodeSeq = {
    S.param("texts").foreach { texts =>
      selectedTexts(Text.findAll(ByList(Text.id, texts.split(",").map(_.trim.toLong))))
    }

    val texts = selectedTexts.is.map { text =>
      (text, Document.findAll(By(Document.text, text.id)))
    }
    
    val breaks = texts.map(_._2.size).scanLeft(0)(_ + _)

    val matrix = texts.flatMap { _._2.map { _.features } }
    val reducer = new PCAReducer
    val reduced = reducer.reduce(matrix.toArray, k)

    val data = reduced.data
    val variance = reduced.variance
    val loadings = reduced.loadings

    val topics = Topic.findAll.map { topic =>
      // This isn't ideal.
      topic.words.map(tw => (tw.word.obj.openOr(Word.create).form.is, tw.weight.is))
    }

    Script(
      JsCrVar("pca_viz", JsObj(
        "items" -> new JsArray(texts.map { case (text, documents) => JsObj(
          "id" -> Num(text.id.is),
          "name" -> Str(this.shortenTitle(text.title)),
          "uid" -> Str(text.uid.is),
          "documents" -> new JsArray(documents.map { document => Str(document.uid.is) })
        )}),
        "topics" -> new JsArray(topics.map(topic => new JsArray(topic.map(word => new JsArray(List(Str(word._1), JsCompactDouble(word._2, this.precision)))).toList))),
        "breaks" -> new JsArray(breaks.map(Num(_))),
        "data" -> this.convertDouble2DArray(data),
        "variance" -> this.convertDouble1DArrayWithIndex(variance),
        "loadings" -> this.convertDouble2DArray(loadings),
        "view" -> JsObj("xaxis" -> JsObj("min" -> this.view._1._1, "max" -> this.view._1._2),
                        "yaxis" -> JsObj("min" -> this.view._2._1, "max" -> this.view._2._2))
      ))
    )
  }
}

/*class PCAViz extends PCAResult {
  S.param("texts").foreach { texts =>
    selectedTexts(Text.findAll(ByList(Text.id, texts.split(",").map(_.trim.toLong))))
  }

  val texts = selectedTexts.is.map { text =>
    (text, Document.findAll(By(Document.text, text.id)))
  }

  val matrix = texts.flatMap { _._2.map { _.features } }
  val reducer = new PCAReducer
  val reduced = this.reducer.reduce(matrix.toArray, 10)

  def data = reduced.data
  def variance = reduced.variance
  def loadings = reduced.loadings

}*/

