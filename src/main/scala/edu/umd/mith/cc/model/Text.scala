package edu.umd.mith.cc {
package model {

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._

object Collection extends Collection with LongKeyedMetaMapper[Collection] {
  override def dbTableName = "collections" // define the DB table name

  override def dbIndexes = Index(IndexField(name)) :: Nil

  def findOrAdd(name: String): Collection = {
    val existing = this.findAll(By(Collection.name, name))
    if (existing.isEmpty) {
      val collection = this.create.name(name)
      collection.save
      collection
    } else {
      existing.head
    }
  }
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

  def add(collection: String, uid: String, title: String, author: String, year: Int): Text = {
    val text = this.create.collection(Collection.findOrAdd(collection))
                   .uid(uid)
                   .title(title)
                   .author(author)
                   .year(year)
    text.save
    text
  }
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
  def add(text: Text, uid: String, plain: String, html: String) = {
    val document = this.create.text(text)
                              .uid(uid)
                              .plain(plain)
                              .html(html)
    document.save
    document
  }
}

class Document extends LongKeyedMapper[Document] with IdPK {
  def getSingleton = Document // what's the "meta" server
  object uid extends MappedString(this, 512)
  object plain extends MappedText(this)
  object html extends MappedText(this)
  object text extends MappedLongForeignKey(this, Text)

  def setFeatures(values: Array[Double]) {
    values.zipWithIndex.foreach {
      case (value, i) => Feature.set(this, i + 1, value)
    }
  }
}

object Feature extends Feature with LongKeyedMetaMapper[Feature] {
  override def dbTableName = "features" // define the DB table name

  def set(document: Document, dim: Int, value: Double): Boolean = {
    val existing = this.findAll(By(Feature.document, document), By(Feature.dim, dim))
    if (existing.isEmpty) {
      val feature = this.create.document(document)
                               .dim(dim)
                               .value(value)
      feature.save
      false
    } else {
      true
    }
  }
}

class Feature extends LongKeyedMapper[Feature] with IdPK {
  def getSingleton = Feature
  object dim extends MappedInt(this)
  object document extends MappedLongForeignKey(this, Document)
  object value extends MappedDouble(this)
}

}
}
