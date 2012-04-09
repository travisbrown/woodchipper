package edu.umd.mith.cc.snippet

import _root_.scala.xml.NodeSeq
import _root_.net.liftweb.http._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.mapper._
import _root_.java.util.Date
import edu.umd.mith.cc.model._
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

