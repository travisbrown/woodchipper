package edu.umd.mith.cc {
package model {

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._

object Collection extends Collection with LongKeyedMetaMapper[Collection] {
  override def dbTableName = "collections" // define the DB table name
}

class Collection extends LongKeyedMapper[Collection] with IdPK {
  def getSingleton = Collection // what's the "meta" server
  object name extends MappedString(this, 512)
}

object Text extends Text with LongKeyedMetaMapper[Text] {
  override def dbTableName = "texts" // define the DB table name

  // define the order fields will appear in forms and output
  override def fieldOrder = List(collection, uid, title, author, year, url)

  override def dbIndexes = Index(IndexField(uid)) ::
                           Index(IndexField(title)) ::
                           Index(IndexField(author)) ::
                           Index(IndexField(year)) :: Nil
                           //Index(IndexField(collection)) :: Nil
}

class Text extends LongKeyedMapper[Text] with IdPK {
  def getSingleton = Text // what's the "meta" server
  object uid extends MappedString(this, 512)
  object title extends MappedString(this, 512)
  object author extends MappedString(this, 512)
  object url extends MappedString(this, 512)
  object year extends MappedInt(this)
  object collection extends MappedLongForeignKey(this, Collection)
}

object Document extends Document with LongKeyedMetaMapper[Document] {
  override def dbTableName = "documents" // define the DB table name

  // define the order fields will appear in forms and output
  override def fieldOrder = List(text, uid, plain, html)

  override def dbIndexes = Index(IndexField(uid)) ::
                           Index(IndexField(plain)) :: Nil
                           //Index(IndexField(text)) :: Nil
}

class Document extends LongKeyedMapper[Document] with IdPK {
  def getSingleton = Document // what's the "meta" server
  object uid extends MappedString(this, 512)
  object plain extends MappedText(this)
  object html extends MappedText(this)
  object text extends MappedLongForeignKey(this, Text)
}

object Feature extends Feature with LongKeyedMetaMapper[Feature] {
  override def dbTableName = "features" // define the DB table name
}

class Feature extends LongKeyedMapper[Feature] with IdPK {
  def getSingleton = Feature
  object dim extends MappedInt(this)
  object document extends MappedLongForeignKey(this, Document)
  object value extends MappedDouble(this)
}

}
}
