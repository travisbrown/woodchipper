package edu.umd.mith.cc {
package model {

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import scala.collection.mutable

/*

// [SB 07/08/11 : We don't need this -- using a class here is overkill as merely
// a tuple (associating a word with a probability) ought to do quite well. 
// So, I'm commenting this class definition out.

// wordWithProb is actually a tuple. The reason that we are making it into a 
// named class is that, in the loadTopics method in the MalletTopicReader makes
// use of a method called findOrAdd, defined in Text.scala. The overloaded findOrAdd
// method ought to find (or add) an entire tuple at a time in/to a Collection of 
// tuples. It seems likely that, in order to do that, merely having an on-the-fly
// tuple would not be sufficient. 

object WordWithProb  {
  override def dbTableName = "wordWithProbs" // define the DB table name
}

class WordWithProb (w: String, pr: Double) extends LongKeyedMapper[WordWithProb] with IdPK {
    def getSingleton = WordWithProb // what's the "meta" server 
    var word = w
	private var _prob = pr
	
    // Getter
    def prob = _prob

    // Setter
    def prob_= (value:Double):Unit = _prob = value
	
}	

*/


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
  
   /*  
   // Overload the findOrAdd method;
   def findOrAdd(weight: Double): Collection = {
    val existing = this.findAll(By(Collection.weight, weight))
    if (existing.isEmpty) {
      val collection = this.create.weight(weight)
      collection.save
      collection
    } else {
      existing.head
    }
  }
  */
  
  /* Commenting this out as we're going to be using tuples rather than 
      an explicit findOrAdd class  -- SB 07/11/11
      
  // Overload the findOrAdd method; this is necessary because 
  // when we think of a topic, we're now no longer simply thinking 
  // of a topic as made up only of words, but now of words and their 
  // normalized probabilities in the topic, as well. 
  
  // Overloaded method for the case in which a tuple rather than a 
  // string is passed as parameter; the tuple consists of a string (a word)
  // and a double (the probability associated with that word in the topic.
  
  def findOrAdd(woWiPro: WordWithProb): Collection = {
  
    // I'm not completely sure what is happening in this line,
    // especially with regard to the "By"
    
    val existing = this.findAll(By(Collection.woWiPro, woWiPro))
    if (existing.isEmpty) {
      val collection = this.create.woWiPro(woWiPro)
      collection.save
      collection
    } else {
      existing.head
    }
  }
  */
  
  
  
}

class Collection extends LongKeyedMapper[Collection] with IdPK {
  def getSingleton = Collection // what's the "meta" server
  object name extends MappedString(this, 512)
  // object woWiPro extends WordWithProb
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
  override def dbTableName = "documents" // define the DB table name

  // define the order fields will appear in forms and output
  override def fieldOrder = List(text, uid, plain, html)

  override def dbIndexes = Index(IndexField(uid)) :: Nil
                           //Index(IndexField(plain)) :: Nil
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

  def features: Array[Double] = {
    Feature.findAll(By(Feature.document, this.id), OrderBy(Feature.dim, Ascending)).map(_.value.is).toArray
  }
}

object Feature extends Feature with LongKeyedMetaMapper[Feature] {
  override def dbTableName = "features" // define the DB table name

  def set(document: Document, dim: Int, value: Double): Boolean = {
    val existing = this.findAll(By(Feature.document, document.id), By(Feature.dim, dim))
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
