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


class Visualization {
  def draw(xhtml: NodeSeq) = {

    val colors = List("#7FFF00", "#FF6347", "#7FFFD4", "#DDA0DD", "#B0C4DE", "#FFE4C4", "#B22222")

    val reducer = new PCAReducer
    val matrix = scala.collection.mutable.ArrayBuffer[Array[Double]]()
    val colass = scala.collection.mutable.ArrayBuffer[String]()

    selectedTexts.is.toList.zip(colors).map { case (text, color) =>
      Document.findAll(By(Document.text, text.id)).map { document =>
        matrix += document.features
        //colass += color
      }
    }

    val reduced = reducer.reduce(matrix.toArray).data
    //reduced.foreach { _.foreach { println(_) }}
    var i = 0

    val series = selectedTexts.is.toList.zip(colors).map { case (text, col) =>
      val vals = Document.findAll(By(Document.text, text.id)).map { doc =>
        val j = i
        i += 1
        (reduced(j)(0), reduced(j)(1))
      }

      new FlotSerie() {
        override val data = vals
        override val label = Full(text.title.is.substring(0, 60) + "...")
        override val points = Full(new FlotPointsOptions {
          override val radius = Full(3)
          override val show = Full(true)
        })
        override val lines = Full(new FlotLinesOptions { override val show = Full(false) })
        override val color = Full(Left(col))
        override val shadowSize = Full(3)
      }
    }

    



    /*val data_values: List[(Double, Double)] = for (i <- List.range (-100, 100))
      yield (i / 100.0, Math.sin(i / 100.0)
    )

    val data_to_plot = new FlotSerie() {
      override val data = data_values
      override val points = Full(new FlotPointsOptions {
        override val radius = Full(3)
        override val show = Full(true)
      })
      override val lines = Full(new FlotLinesOptions { override val show = Full(false) })
      override val color = Full(Left("#7fff00"))
      override val shadowSize = Full(3)

    }*/

    val options = new FlotOptions {
      override val grid = Full(new FlotGridOptions {
        override val hoverable = Full(true)
        override val clickable = Full(true)
      })

      override val xaxis = Full(new FlotAxisOptions {
        override val min = Full(-0.4)
        override val max = Full(0.4)
      })

      override val yaxis = Full(new FlotAxisOptions {
        override val min = Full(-0.4)
        override val max = Full(0.4)
      })
    }

    Flot.render("vizmap", series, options, Flot.script(xhtml))
  }

  def clicker(xhtml: NodeSeq) = {
    Script(JsRaw(
      """jQuery("#vizmap").bind("plotclick", function (event, pos, item) {
           if (item) {
             alert(item.dataIndex, item.series.label);
             plot_vizmap.highlight(item.series, item.datapoint);
           }

         });

        /*jQuery("#vizmap").bind("plothover", function (event, pos, item) {
          jQuery("#x").text(pos.x.toFixed(2));
          jQuery("#y").text(pos.y.toFixed(2));
        });*/"""))
  }
}

}
}
