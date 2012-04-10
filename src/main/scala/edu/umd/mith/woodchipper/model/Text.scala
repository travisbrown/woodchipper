package edu.umd.mith.woodchipper.model

import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.common._

object Collection extends Collection with LongKeyedMetaMapper[Collection] {
  override def dbTableName = "collections"
  override def dbIndexes = List(Index(IndexField(name)))

  def findOrAdd(name: String): Collection =
    this.findAll(By(Collection.name, name)).headOption.getOrElse {
      val collection = this.create.name(name)
      collection.save
      collection
    }
}

class Collection extends LongKeyedMapper[Collection] with IdPK {
  def getSingleton = Collection
  object name extends MappedString(this, 512)
}

object Text extends Text with LongKeyedMetaMapper[Text] {
  override def dbTableName = "texts"
  override def fieldOrder = List(collection, uid, title, author, year, url)
  override def dbIndexes = List(
    Index(IndexField(uid)),
    Index(IndexField(title)),
    Index(IndexField(author)),
    Index(IndexField(year))
  )

  def add(collection: String, uid: String, title: String, author: String, year: Int): Text = {
    val text = this.create.collection(
      Collection.findOrAdd(collection)
    ).uid(uid).title(title).author(author).year(year)
    text.save
    text
  }
}

class Text extends LongKeyedMapper[Text] with IdPK {
  def getSingleton = Text
  object uid extends MappedString(this, 512)
  object title extends MappedString(this, 1024)
  object author extends MappedString(this, 512)
  object url extends MappedString(this, 512)
  object year extends MappedInt(this)
  object uuid extends MappedUniqueId(this, 64)
  object collection extends MappedLongForeignKey(this, Collection)

  def collectionName: String = {
    this.collection.obj.map(_.name.is).openOr("unknown")
  }
}

object Document extends Document with LongKeyedMetaMapper[Document] {
  override def dbTableName = "documents"
  override def fieldOrder = List(text, uid, plain, html)
  override def dbIndexes = List(Index(IndexField(uid)))

  def add(text: Text, uid: String, plain: String, html: String) = {
    val document = this.create.text(text).uid(uid).plain(plain).html(html)
    document.save
    document
  }
}

class Document extends LongKeyedMapper[Document] with IdPK {
  def getSingleton = Document
  object uid extends MappedString(this, 512)
  object plain extends MappedText(this)
  object html extends MappedText(this)
  object text extends MappedLongForeignKey(this, Text)

  def setFeatures(values: Seq[Double]) {
    values.zipWithIndex.foreach {
      case (value, i) => Feature.set(this, i + 1, value)
    }
  }

  def features: IndexedSeq[Double] =
    Feature.findAll(
      By(Feature.document, this.id), OrderBy(Feature.dim, Ascending)
    ).map(_.value.is).toIndexedSeq
}

object Feature extends Feature with LongKeyedMetaMapper[Feature] {
  override def dbTableName = "features"

  def set(document: Document, dim: Int, value: Double) {
    this.findAll(
      By(Feature.document, document.id), By(Feature.dim, dim)
    ).headOption.getOrElse {
      this.create.document(document).dim(dim).value(value).save()
    }
  }
}

class Feature extends LongKeyedMapper[Feature] with IdPK {
  def getSingleton = Feature
  object dim extends MappedInt(this)
  object document extends MappedLongForeignKey(this, Document)
  object value extends MappedDouble(this)
}

