package edu.umd.mith.cc {
package snippet {

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
import net.liftweb.widgets.flot._
import js._
import JsCmds._
import JE._
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import net.liftweb.json.Printer.pretty 

class WoodchipperSerie extends FlotSerie {
  override val points = Full(new FlotPointsOptions {
    override val radius = Full(4)
    override val show = Full(true)
  })
  override val lines = Full(new FlotLinesOptions {
    override val show = Full(false)
  })
  override val shadowSize = Full(8)
}

class Visualization {
  var textIds: List[Long] = List[Long]()
  var reduced: Option[PCAReduction] = None 

  val colors = List("#7FFF00", "#FF6347", "#7FFFD4", "#DDA0DD",
                    "#B0C4DE", "#FFE4C4", "#B22222")

  val (minX, maxX) = (-0.4, 0.4)
  val (minY, maxY) = (minX, maxX)

  val options = new FlotOptions {
    override val grid = Full(new FlotGridOptions {
      override val hoverable = Full(true)
      override val clickable = Full(true)
    })

    override val xaxis = Full(new FlotAxisOptions {
      override val min = Full(Visualization.this.minX)
      override val max = Full(Visualization.this.maxX)
    })

    override val yaxis = Full(new FlotAxisOptions {
      override val min = Full(Visualization.this.minY)
      override val max = Full(Visualization.this.maxY)
    })
  }

  def draw(in: NodeSeq): NodeSeq = {
    val reducer = new PCAReducer

    S.param("texts").foreach { textsParam =>
      val textIds = textsParam.split(",").map(_.trim.toLong)
      selectedTexts(Text.findAll(ByList(Text.id, textIds)))
    }

    val (pcaX, pcaY) = S.param("pcs").map { pcasParam => 
      val pcas = pcasParam.split(",").map(_.trim.toInt)
      (pcas(0) - 1, pcas(1) - 1)
    }.openOr((0, 1))

    val sel = selectedTexts.is.map { (text: Text) => (text, Document.findAll(By(Document.text, text.id))) }
    textIds = selectedTexts.is.map { text => text.id.is }

    val matrix = sel.flatMap { _._2.map { _.features } }
    reduced = Some(reducer.reduce(matrix.toArray, 10))
    
    var i = 0
    val series = sel.zip(colors).map { case ((text, docs), col) =>
      val vals = reduced.get.data.slice(i, i + docs.size)
      i += docs.size

      new WoodchipperSerie() {
        override val data = vals.toList.map(coords => (coords(pcaX), coords(pcaY)))
        override val color = Full(Left(col))
        override val label = Full(text.title.is.substring(0, Math.min(60, text.title.is.length)) + "...")
      }
    }

    Flot.render("vizmap", series, this.options, Flot.script(in))
  }

  implicit def convertLongList(v: List[Long]): JsExp = new JsArray(v.map(Num(_)))
  implicit def convertDoubleArray(v: Array[Double]) = new JsArray(v.map(Num(_)).toList)

  def clicker(in: NodeSeq): NodeSeq = {
    Script(
      JsCrVar("textIds", textIds) &
      JsCrVar("eigenvalues", reduced.get.loadings)
    )
  }
}

}
}
