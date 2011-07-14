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
   
	
	 // arrays to pass the words and weights to json;
	 // this is going to be an "iregularly" shaped two-dimensional
	 // array with differentially sized rows, because different rows
	 // will contain the significant words for different topics, and
	 // the numbers of significant words for various topics are likely
	 // to be different from each other
	 
	 // make use of generic arrays that scala provides -- java
	 // wouldn't have permitted this
	 
	 /* 
	 
	  // oddly, this (the def below) didn't work -- why? The scala compiler generated an
	  // error message saying:  error: too many arguments for method 
	  // elementsOfATopic: (xs: List[T])(implicit evidence$1: ClassManifest[T])Array[T]
      // [INFO]      		 elementsOfATopic( (Word Int), significantTopicWords(whichTopic)) 
      
      // Don't know why this didn't work.

	 def elementsOfATopic[T: ClassManifest](xs: List[T]): Array[T] = {
	    val arr = new Array[T](xs.length)
	    for (i<- 0 until xs.length)
	     arr(i) = xs(i)
	    arr
	 }   
	 
	 */
	  
   def this(path: String, n: Int) = this(new File(path), n)
   
   def loadTopics() {   
     
     // This is not the recommended way to do it;
     // we should do it using "fold" instead
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
                   		      
                   		      
                   		      // (topicWord.getID().asInstanceOf[String], topicWord.getWeight()/ sum)
                   		      // (topicWord.getID().toString, topicWord.getWeight()/ sum)
                   		      //(Word.findOrAdd(topicWord.getID()), topicWord.getWeight()/ sum)
                   		      
                   		      // create the tuple
                   		      val word =  
                   		         ParallelTopicModel.read(file).getAlphabet.lookupObject(topicWord.getID()).asInstanceOf[String]
                   		      val probability = topicWord.getWeight()/ sum
                   		         /* print("word=")
								 print(word) 
								 print(" ")
								 print("probability=")
								 print(probability)
				                 println() */
                   		      (word,probability)
                   		        
                   	   })
                   	 
                    }  
     )
     
     
     println("000")
     var significantTopicWords = new Array[Int](topics.length)
     var topicNumber = 0 
     println("1")
     topics.foreach{ 
       topic => {
		 var probMass = 0.0
		 significantTopicWords(topicNumber) = 0
		
		 // 0.6 is a magic number -- change it later on
		 while (probMass <  0.05) {
			 probMass = probMass + ((topic.toList)(significantTopicWords(topicNumber)))._2
			 significantTopicWords(topicNumber) = significantTopicWords(topicNumber) + 1
		 }
		 significantTopicWords(topicNumber) = significantTopicWords(topicNumber) - 1	
	  }
	  topicNumber = topicNumber + 1
     }	
     println("2")
 	 
 	 // Each topic is a list (actually a hashSet) of tuples, each 
 	 // tuple being of the form 
 	 // (word:String probability:Double)
 	 
 	 var listOfAllTopics: List[List[WordWithProb]] = List()
 	 var whichTopic = -1
     topics.foreach { topicWords => 
             whichTopic = whichTopic + 1
             println("0") 
      	     val topic = Topic.create
     		 topic.save
     		 
     		 var whichWord = -1
     		 
     		 // Construct the array of tuples from the generic method defined earlier.
     		 // The format of the tuple gets passed to the implicit parameter which is
     		 // the class manifest in the method
     		 
     		 var elementsOfATopic: List[WordWithProb] = List()
     		 
 	         topicWords.foreach { 
				pair => {
				   
				   
				   whichWord = whichWord + 1 
				   if (whichWord <= significantTopicWords(whichTopic)) {
					 println("New pair")
					 if (whichWord <= significantTopicWords(topicNumber)) {
						 // Note: each "word" below is now actually 
						 // a tuple (the string and the normalized weight)
						 
						 // This then creates a problem. Word.findOrAdd expects a
						 // string, but it gets a tuple. How to resolve this?
						 // For the moment, I am going ahead and overloading the 
						 // findOrAdd function.
			   
						 val word = Word.findOrAdd(pair._1)
						 val probability = pair._2
						 
						 print("word=")
						 print(word) 
						 print(" ")
						 print("probability=")
						 print(probability)
						 println()
						 var wordWithProb = new WordWithProb(word.asInstanceOf[String],probability)
						 elementsOfATopic =  wordWithProb::elementsOfATopic
					   
						 // Note: "weight" is actually not a weight, but a (normalized) probability.
						 // That we call it "weight" is just an artifact of how this used to be 
						 // represented earlier.
					   
						 // val topicWord = TopicWord.create.topic(topic).word(word).weight(probability)  	           
						 // topicWord.save 
					 }		 	 
			       } 
			     } 
  	         }
  	         listOfAllTopics = elementsOfATopic::listOfAllTopics
  	         
     }
     listOfAllTopics
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
 
