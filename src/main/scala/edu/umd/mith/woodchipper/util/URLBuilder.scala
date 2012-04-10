package edu.umd.mith.woodchipper.util

trait URLBuilder {
  def buildTextURL(
    collection: String,
    textId: String,
    suppIds: Map[String, String]
  ): String

  def buildTextURL(
    collection: String,
    textId: String
  ): String =
    this.buildTextURL(collection, textId, Map.empty[String, String])

  def buildChunkURL(
    collection: String,
    textId: String,
    chunkId: String,
    suppIds: Map[String, String]
  ): String

  def buildChunkURL(
    collection: String,
    textId: String,
    chunkId: String
  ): String =
    this.buildChunkURL(collection, textId, chunkId, Map.empty[String, String])
}

class DefaultURLBuilder extends URLBuilder {
  def buildTextURL(
    collection: String,
    textId: String,
    suppIds: Map[String, String]
  ) = collection match {
    case "hathi" => "http://hdl.handle.net/2027/%s".format(textId)
    case "perseus" => throw new Exception("Cannot create URLs for Perseus.")
    case "eebo" if suppIds.contains("marc") =>
      "http://gateway.proquest.com/openurl?ctx_ver=Z39.88-2003&res_id=xri:eebo&rft_id=xri:eebo:citation:%s".format(suppIds("marc"))
    case "eebo" => throw new Exception("No marc identifier provided.")
    case "ecco" => throw new Exception("Cannot create URLs for ECCO.")
    case "evans" => throw new Exception("Cannot create URLs for Evans.")
    case _ => ""
  }

  def buildChunkURL(
    collection: String,
    textId: String,
    chunkId: String,
    suppIds: Map[String, String]
  ) = collection match {
    case "hathi" =>
      val encodedId = java.net.URLEncoder.encode(textId, "ASCII")
      "http://babel.hathitrust.org/cgi/pt?view=image&size=100&id=%s&u=1&seq=%d".format(encodedId, chunkId.toInt)
    case "perseus" => throw new Exception("Cannot create URLs for Perseus.")
    case "eebo" if suppIds.contains("vid") =>
      "http://gateway.proquest.com/openurl?ctx_ver=Z39.88-2003&res_id=xri:eebo&rft_id=xri:eebo:image:%s:%d".format(suppIds("vid"), chunkId.toInt)
    case "eebo" => throw new Exception("No marc identifier provided.")
    case "ecco" => throw new Exception("Cannot create URLs for ECCO.")
    case "evans" => throw new Exception("Cannot create URLs for Evans.")
    case _ => ""
  }
}

