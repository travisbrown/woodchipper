package edu.umd.mith.cc.util

import java.io._
import scala.io._
import java.util.TreeSet
import java.util.Iterator
import _root_.bootstrap.liftweb.Boot
import _root_.net.liftweb.mapper._
import scala.collection.JavaConverters._
import edu.umd.mith.cc.model._
import cc.mallet.topics.ParallelTopicModel
import cc.mallet.types.IDSorter

class MalletTopicReader(file: File, n: Int) {

   def convert(m: ParallelTopicModel) = {
     m.getSortedWords.map(_.asInstanceOf[TreeSet[IDSorter]].iterator.asScala.toArray).toList
   }
 
   def this(path: String, n: Int) = this(new File(path), n)
   
   def loadTopics() {   
     
     // This is not the recommended way to do it;
     // do it using "fold" instead
     def sumOfWeights(wordWeights: Array[Double]): Double = wordWeights.toList match {
         case hd :: tail => hd + sumOfWeights(tail.toArray)
          case Nil => 0
     }
    
     /* val topics = allTopicsWithWeights.map(topicWithWeights =>
             var sumOfWeights= 0
             topicWithWeights.foreach { topicWord =>
                  sumOfWeights+= topicWord.getWeight()
             }         
             topicWithWeights.map( 
                     // topicWord is actually a tuple, not just a word: 
                     // it is a string (the actual word) and also the associated
                     // normalized weight; these two elements together form a tuple.
                     topicWord => 
                        (topicWord.getID().asInstanceOf[String], topicWord.getWeight()/ sumOfWeights ))   
     )  */
   
     val allTopicsWithWeights = convert(ParallelTopicModel.read(file))
     
     // val topics = allTopicsWithWeights.getTopWords(n).map(
     
     // Earlier, topics used to be lists (actually treeSets) of just words only, and so it 
     // made sense to do it this way (as in the commented line above). However, now that 
     // topics are actually lists of tuples, we need to approach this differently. 
     // However, getTopWords being a Mallet function, we shouldn't change getTopWords 
     // directly, but instead put a wrapper around it (or write our own function).
     
     println("0000")
     
     val topics = allTopicsWithWeights.map(
                    topicWithWeights => {  
                       println("00")
                       var sum = sumOfWeights(topicWithWeights.map(x=>x.getWeight()))
                 	   topicWithWeights.map(topicWord => {  
                 	   
                       // topicWord is actually a tuple, not just a word: 
                       // it is a string (the actual word) and also the associated
                       // weight; these two elements together form a tuple.
                       	    				   
							 /*
						     // [SB 07/08/11 : We don't need this -- using a class here is overkill as merely
						     // a tuple (associating a word with a probability) ought to do quite well. 
						     // So, I'm commenting this class definition out.
                   		      WordWithProb(topicWord.getID().asInstanceOf[String], topicWord.getWeight()/ sum)
                   		      */
                   		      
                   		      println("0")
                   		      // (topicWord.getID().asInstanceOf[String], topicWord.getWeight()/ sum)
                   		      (topicWord.getID().toString, topicWord.getWeight()/ sum)
                   	   })
                    }  
     )   
     println("000")
     var topicNumber = 0 
     var significantTopicWords = new Array[Int](topics.length)
     println("1")
     topics.foreach{ 
       topic => {
		 var probMass = 0.0
		 significantTopicWords(topicNumber) = 0
		
		 // 0.6 is a magic number -- change it later on
		 while (probMass <  0.6) {
			 probMass = probMass + (topic(significantTopicWords(topicNumber)))._2
			 significantTopicWords(topicNumber) = significantTopicWords(topicNumber) + 1
		 }
		 significantTopicWords(topicNumber) = significantTopicWords(topicNumber) - 1	
	  }	 
     }	
     println("2")
 	 
 	 // Each topic is a list (actually a hashSet) of tuples, each 
 	 // tuple being of the form 
 	 // (word:String probability:Double)
 	 
 	 
     topics.foreach { topicWords =>   
        
      	     val topic = Topic.create
     		 topic.save
     		 
     		 var whichWord=0
 	         topicWords.foreach { 
 	           pair => {
				 if (whichWord <= significantTopicWords(topicNumber)) {
					 // Note: each "word" below is now actually 
					 // a tuple (the string and the normalized weight)
					 
					 // This then creates a problem. Word.findOrAdd expects a
					 // string, but it gets a tuple. How to resolve this?
					 // For the moment, I am going ahead and overloading the 
					 // findOrAdd function.
		   
				     val word = Word.findOrAdd(pair._1)
				     
				     //remove this line later -- this is just for debugging purposes
				     val dummy = pair._2
				     
				     print("word=")
				     print(word) 
				     print(" ")
				     print("probability=")
				     print(dummy)
				     println()
				   
				     // Note: "weight" is actually not a weight, but a (normalized) probability.
				     // That we call it "weight" is just an artifact of how this used to be 
				     // represented earlier.
				   
				     val topicWord = TopicWord.create.topic(topic).word(word).weight(pair._2)  	           
				   
				     topicWord.save
				     whichWord = whichWord+1
				 }
			    } 
  	         }  	        
     }
     println("3")
   }
 }
 
 object MalletTopicReader {
   def main(args: Array[String]) {
     val boot = new bootstrap.liftweb.Boot
     boot.boot
     
     println("Am I in main?")
     
     if (args.length > 0) {
       val reader = new MalletTopicReader(args(0), 0)
       println("-1")
       reader.loadTopics
       println("-2")
     } else {
       println("-3") 
       Topic.findAll.foreach { topic =>
         println("-4") 
         topic.words.foreach(println(_))
       }
     }
   }
 }
 
