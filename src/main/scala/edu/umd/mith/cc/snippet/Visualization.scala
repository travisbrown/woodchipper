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
import net.liftweb.widgets.flot._
import js._
import JsCmds._
import JE._
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import net.liftweb.json.Printer.pretty 

class MapSerie extends FlotSerie {
  override val points = Full(new FlotPointsOptions {
    override val radius = Full(4)
    override val show = Full(true)
  })
  override val lines = Full(new FlotLinesOptions {
    override val show = Full(false)
  })
  override val shadowSize = Full(8)
}

class VariancesSerie extends FlotSerie {
  override val points = Full(new FlotPointsOptions {
    override val radius = Full(4)
    override val show = Full(true)
  })
  override val lines = Full(new FlotLinesOptions {
    override val show = Full(true)
  })
  override val shadowSize = Full(8)
}

class Visualization {
  val colors = List("#7FFF00", "#FF6347", "#7FFFD4", "#DDA0DD",
                    "#B0C4DE", "#FFE4C4", "#B22222")

  val maxKeyTitleLength = 60

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

  S.param("texts").foreach { texts =>
    selectedTexts(Text.findAll(ByList(Text.id, texts.split(",").map(_.trim.toLong))))
  }

  val texts = selectedTexts.is.map { text =>
    (text, Document.findAll(By(Document.text, text.id)))
  }

  val matrix = texts.flatMap { _._2.map { _.features } }
  val reducer = new PCAReducer
  val reduced = this.reducer.reduce(matrix.toArray, 10)
    
  def drawMap(in: NodeSeq): NodeSeq = {
    val (pcaX, pcaY) = S.param("pcs").map { pcasParam => 
      val pcas = pcasParam.split(",").map(_.trim.toInt)
      (pcas(0) - 1, pcas(1) - 1)
    }.openOr((0, 1))

    var i = 0
    val series = this.texts.zip(colors).map { case ((text, docs), col) =>
      val shortenedTitle = text.title.is.substring(0, Math.min(this.maxKeyTitleLength, text.title.is.length)) + "..."

      val vals = this.reduced.data.slice(i, i + docs.size)
      i += docs.size

      new MapSerie() {
        override val data = vals.toList.map(coords => (coords(pcaX), coords(pcaY)))
        override val color = Full(Left(col))
        override val label = Full(shortenedTitle)
      }
    }

    Flot.render("vizmap", series, this.options, Flot.script(in))
  }

  def drawVariances(in: NodeSeq): NodeSeq = {
    val serie = new VariancesSerie() {
      override val data = Visualization.this.reduced.variances.slice(0, 10).toList.zipWithIndex.map { case (v, i) => ((i + 1).toDouble, v) }
      //override val color = Full(Left("#FF6347"))
      override val label = Full("Variance per component")
    }
    Flot.render("vizvariances", List(serie), new FlotOptions {}, Flot.script(in))
  }

  implicit def convertLongList(v: List[Long]) = new JsArray(v.map(Num(_)))
  implicit def convertDoubleArray(v: Array[Double]) = new JsArray(v.map(Num(_)).toList)

  def renderData(in: NodeSeq): NodeSeq = {
    Script(
      JsCrVar("textIds", this.texts.map(_._1.id.is))
    )
  }
}

