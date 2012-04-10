package edu.umd.mith.woodchipper.model

import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.common._

object Topic extends Topic with LongKeyedMetaMapper[Topic] {
  override def dbTableName = "topics"
}

class Topic extends LongKeyedMapper[Topic] with IdPK with OneToMany[Long, Topic] {
  def getSingleton = Topic
  object name extends MappedString(this, 512)
  object weight extends MappedDouble(this) {
    override def defaultValue = 0.0
  }
  object words extends MappedOneToMany(
    TopicWord, TopicWord.topic,
    OrderBy(TopicWord.weight, Descending),
    OrderBy(TopicWord.id, Ascending)
  )
}

object Word extends Word with LongKeyedMetaMapper[Word] {
  override def dbTableName = "words"
  override def dbIndexes = List(Index(IndexField(form)))

  def findOrAdd(form: String) =
    this.findAll(By(Word.form, form)).headOption.getOrElse {
      val word = this.create.form(form)
      word.save
      word
    }
}

class Word extends LongKeyedMapper[Word] with IdPK with OneToMany[Long, Word] {
  def getSingleton = Word
  object form extends MappedString(this, 128)
  object topics extends MappedOneToMany(
    TopicWord, TopicWord.word,
    OrderBy(TopicWord.weight, Descending)
  )
}

object TopicWord extends TopicWord with LongKeyedMetaMapper[TopicWord] {
  //override def dbIndexes = Index(IndexField(topic)) ::
  //                         Index(IndexField(word)) :: Nil
}

class TopicWord extends LongKeyedMapper[TopicWord] with IdPK {
  def getSingleton = TopicWord
  object topic extends MappedLongForeignKey(this, Topic)
  object word extends MappedLongForeignKey(this, Word)
  object weight extends MappedDouble(this) {
    override def defaultValue = 0.0
  }
}

