package edu.umd.mith.cc.util

//import scala.collection.JavaConversions._

trait URLBuilder {
  /*private def convertMap(map: java.util.Map[java.lang.Object, java.lang.Object]): Map[String, String] = {
    map.map {
      case (k: String, v: String) => (k, v)
    }.toMap
  }*/

  def buildTextURL(
    collection: String,
    textId: String,
    suppIds: Map[String, String]): String

  def buildTextURL(
    collection: String,
    textId: String): String = this.buildTextURL(collection, textId, Map[String, String]())

  /*def buildTextURL(
    collection: String,
    textId: String,
    suppIds: java.util.Map[java.lang.Object, java.lang.Object]): String = {
    this.buildTextURL(collection, textId, this.convertMap(suppIds))
  }*/

  def buildChunkURL(
    collection: String,
    textId: String,
    chunkId: String,
    suppIds: Map[String, String]): String

  def buildChunkURL(
    collection: String,
    textId: String,
    chunkId: String): String = this.buildChunkURL(collection, textId, chunkId, Map[String, String]())

  /*def buildChunkURL(
    collection: String,
    textId: String,
    chunkId: String,
    suppIds: java.util.Map[java.lang.Object, java.lang.Object]): String = {
    this.buildChunkURL(collection, textId, chunkId, this.convertMap(suppIds))
  }*/
}

import java.net.URLEncoder

class CCURLBuilder extends URLBuilder {
  def buildTextURL(
    collection: String,
    textId: String,
    suppIds: Map[String, String]): String = {
    collection match {
      case "hathi" => {
        "http://hdl.handle.net/2027/%s".format(textId)
      }
      case "rdd" => ""
      case "perseus" => {
        throw new Exception("Cannot create URLs for Perseus.")
      }
      case "eebo" => {
        if (suppIds.contains("marc")) {    
          "http://gateway.proquest.com/openurl?ctx_ver=Z39.88-2003&res_id=xri:eebo&rft_id=xri:eebo:citation:%s".format(suppIds("marc"))
        } else {
          throw new Exception("No marc identifier provided.")
        }
      }
      case "ecco" => {
        throw new Exception("Cannot create URLs for ECCO.")
      }
      case "evans" => {
        throw new Exception("Cannot create URLs for Evans.")
      }
    }
  }

  def buildChunkURL(
    collection: String,
    textId: String,
    chunkId: String,
    suppIds: Map[String, String]): String = {
    collection match {
      case "rdd" => ""
      case "hathi" => {
        val encodedId = URLEncoder.encode(textId, "ASCII")
        "http://babel.hathitrust.org/cgi/pt?view=image&size=100&id=%s&u=1&seq=%d".format(encodedId, chunkId.toInt)
      }
      case "perseus" => {
        throw new Exception("Cannot create URLs for Perseus.")
      }
      case "eebo" => {
        if (suppIds.contains("vid")) {
          "http://gateway.proquest.com/openurl?ctx_ver=Z39.88-2003&res_id=xri:eebo&rft_id=xri:eebo:image:%s:%d".format(suppIds("vid"), chunkId.toInt)
        } else {
          throw new Exception("No marc identifier provided.")
        }
      }
      case "ecco" => {
        throw new Exception("Cannot create URLs for ECCO.")
      }
      case "evans" => {
        throw new Exception("Cannot create URLs for Evans.")
      }
    }
  }
}

