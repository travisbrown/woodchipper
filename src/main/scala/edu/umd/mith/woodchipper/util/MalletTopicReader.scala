package edu.umd.mith.woodchipper.util

import java.io._
import scala.io._
import bootstrap.liftweb.Boot
import net.liftweb.mapper._

import edu.umd.mith.woodchipper.model._
import cc.mallet.topics.ParallelTopicModel

class MalletTopicReader(file: File, n: Int) {
  def this(path: String, n: Int) = this(new File(path), n)

  def loadTopics() {
    val model = ParallelTopicModel.read(file)
    val topics = model.getTopWords(n).map(_.map(_.asInstanceOf[String]))
    topics.foreach { topicWords =>
      val topic = Topic.create
      topic.save
      topicWords.foreach { form =>
        val word = Word.findOrAdd(form)
        val topicWord = TopicWord.create.topic(topic).word(word)
        topicWord.save
      }
    }
  }
}

object MalletTopicReader {
  def main(args: Array[String]) {
    val boot = new bootstrap.liftweb.Boot
    boot.boot

    if (args.length > 0) {
      val reader = new MalletTopicReader(args(0), 10)
      reader.loadTopics
    } else {
      Topic.findAll.foreach { topic =>
        topic.words.foreach(println(_))
      }
    }
  }
}

