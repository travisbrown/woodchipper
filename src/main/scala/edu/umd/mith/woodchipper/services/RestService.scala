package edu.umd.mith.woodchipper.services

import net.liftweb.http.{ GetRequest, Req }
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonDSL._
import net.liftweb.mapper.By
import edu.umd.mith.woodchipper.model.{ Document, Text }
import edu.umd.mith.woodchipper.util.DefaultURLBuilder

object RestService extends RestHelper {
  private val urlBuilder = new DefaultURLBuilder

  serve {
    case Req("api" :: "text" :: textId :: docSeqId :: _, "json", GetRequest) => {
      val text = Text.findAll(By(Text.id, textId.toLong))(0)
      val doc = Document.findAll(By(Document.text, text.id))(docSeqId.toInt)
      ("text" ->
        ("title" -> text.title.is) ~
        ("author" -> text.author.is) ~
        ("year" -> text.year.is)
      ) ~ ("document" ->
        ("seq" -> doc.uid.is) ~
        ("html" -> scala.xml.Utility.escape(doc.plain.is).replaceAll("\n", "<br />")) ~
        ("url" -> this.urlBuilder.buildChunkURL(text.collectionName, text.uid.is, doc.uid.is)) ~
        ("features" -> doc.features.toList)
      )
    }
  }
}

