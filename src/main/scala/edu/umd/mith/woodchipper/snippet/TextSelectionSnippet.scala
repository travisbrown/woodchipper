package edu.umd.mith.woodchipper.snippet

import scala.xml.NodeSeq
import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.mapper._
import java.util.Date
import edu.umd.mith.woodchipper.model.Text
import Helpers._

class TextSelectionSnippet {
  private def redirect = {
    "/search?offset=%d".format(offsetVar.is)
  }

  def remove(in: NodeSeq): NodeSeq = {
    val id = S.param("id").map(_.toInt).openOr(0)
    selectedTexts(selectedTexts.is.filterNot(_.id == id))
    S.redirectTo(this.redirect)
    new scala.xml.Text("remove")
  }

  def add(in: NodeSeq): NodeSeq = {
    val id = S.param("id").map(_.toInt).openOr(0)
    val text = Text.findAll(By(Text.id, id))(0)
    selectedTexts(text :: selectedTexts.is.filterNot(_.id == id))
    S.redirectTo(this.redirect)
    new scala.xml.Text("add")
  }
}

