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
     
     /* def sumOfWeights(wordWeights: Array[Double]): Double = wordWeights.toList match {
         case hd :: tail => hd + sumOfWeights(tail.toArray)
          case Nil => 0
     } */
     
     def sumOfWeights(wordWeights: Array[Double]): Double = 
<<<<<<< HEAD
               wordWeights.sum
         
     val fileRead = ParallelTopicModel.read(file)
     val allTopicsWithWeights = convert(fileRead)
     
     println("0000")
     
     /* 
     val out = new java.io.FileWriter("out.txt");
     */
     
     
=======
               wordWeights.toList.sum
         
    
     val allTopicsWithWeights = convert(ParallelTopicModel.read(file))
     
     println("0000")
     
>>>>>>> travis/piechart
     val topics = allTopicsWithWeights.map(
                    topicWithWeights => {  
                       println("00")
                       var sum = sumOfWeights(topicWithWeights.map(x=>x.getWeight()))
<<<<<<< HEAD
                       print("Number of words in this topic is: ")
                       println(topicWithWeights.length)
                       
                       /* out.write("\r\n")
                       out.write("Number of words in this topic is: ")
                       out.write(topicWithWeights.length.asInstanceOf[String])
                       out.write("\r\n")
                       out.write("\r\n") 
                       */
                       
                 	   topicWithWeights.map(topicWord =>  { 
                 	   
                 	      /* out.write((fileRead.getAlphabet.lookupObject(topicWord.getID())).asInstanceOf[String]) 
                   		  out.write(" ")
                   		  out.write("\r\n")
                   		  */
                   		  
                          (fileRead.getAlphabet.lookupObject(topicWord.getID()).asInstanceOf[String],topicWord.getWeight()/ sum)
                       })
                    })  
                    
     /* out.close()
     */
=======
                 	   topicWithWeights.map(topicWord =>  { 
                   		  (ParallelTopicModel.read(file).getAlphabet.lookupObject(topicWord.getID()).asInstanceOf[String],topicWord.getWeight()/ sum)    
     })})     
                   	  
     
>>>>>>> travis/piechart
     
     println("000")
     var significantTopicWords = new Array[Int](topics.length)
     var topicNumber = 0 
     println("1")
     topics.foreach{ 
<<<<<<< HEAD
		  topic => {
			 var probMass = 0.0
			 significantTopicWords(topicNumber) = 0			
			 // 0.05 is a magic number -- change it later on
			 while (probMass <  0.05) {
				 probMass = probMass + ((topic.toList)(significantTopicWords(topicNumber)))._2
				 significantTopicWords(topicNumber) = significantTopicWords(topicNumber) + 1
			 }
			 significantTopicWords(topicNumber) = significantTopicWords(topicNumber) - 1	
		  }
		  print("topicNumber=")
		  print(topicNumber) 
		  print(" ")
		  print("significantTopicWords(topicNumber)")
		  print(significantTopicWords(topicNumber))
		  println()
		 
		  topicNumber = topicNumber + 1
=======
       topic => {
		 var probMass = 0.0
		 significantTopicWords(topicNumber) = 0
		
		 // 0.05 is a magic number -- change it later on
		 while (probMass <  0.05) {
			 probMass = probMass + ((topic.toList)(significantTopicWords(topicNumber)))._2
			 significantTopicWords(topicNumber) = significantTopicWords(topicNumber) + 1
		 }
		 significantTopicWords(topicNumber) = significantTopicWords(topicNumber) - 1	
	  }
	  topicNumber = topicNumber + 1
>>>>>>> travis/piechart
     }	
     println("2")
 	 
 	 // Each topic is a list (actually a hashSet) of tuples, each 
 	 // tuple being of the form 
 	 // (word:String probability:Double)
 	 
 	 var listOfAllTopics: List[List[(String, Double)]] = List()
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
     		 
     		 var elementsOfATopic: List[(String, Double)] = List()
     		 
 	         topicWords.foreach { 
				pair => {
<<<<<<< HEAD
				
				   /* println(pair)
				   */
				   
=======
>>>>>>> travis/piechart
				   whichWord = whichWord + 1 
				   if (whichWord < significantTopicWords(whichTopic)) {
				   
					     println("New pair")
						 val word = Word.findOrAdd(pair._1)
<<<<<<< HEAD
						 println("AAAAAAAAAA")
=======
>>>>>>> travis/piechart
						 val probability = pair._2
						 print("word=")
						 print(word) 
						 print(" ")
						 print("probability=")
						 print(probability)
						 println()
<<<<<<< HEAD
						 // elementsOfATopic =  ((word.getID()).asInstanceOf[String],probability)::elementsOfATopic
						 elementsOfATopic =  ((pair._1).asInstanceOf[String],probability)::elementsOfATopic
						 println("BBBBBBBBBB")
						 val topicWord = TopicWord.create.topic(topic).word(word).weight(probability)  	
						 println("CCCCCCCCCC")
=======
						 elementsOfATopic =  (word.asInstanceOf[String],probability)::elementsOfATopic
						 val topicWord = TopicWord.create.topic(topic).word(word).weight(probability)  	           
>>>>>>> travis/piechart
						 topicWord.save 
					 }		 	 
			       } 
			     } 
<<<<<<< HEAD
			     println(elementsOfATopic)
			     listOfAllTopics = elementsOfATopic::listOfAllTopics
  	         }
  	         print(listOfAllTopics)      
  	         listOfAllTopics      
     }
 }
=======
			     listOfAllTopics = elementsOfATopic::listOfAllTopics
  	         }
  	   listOfAllTopics      
     }
>>>>>>> travis/piechart
 
 object MalletTopicReader {
   def main(args: Array[String]) {
     val boot = new bootstrap.liftweb.Boot
     boot.boot
     
     if (args.length > 0) {
       val reader = new MalletTopicReader(args(0), 0)   
       reader.loadTopics  
     } else {     
       Topic.findAll.foreach { topic =>         
         topic.words.foreach(println(_))
       }
     }
   }
 }
<<<<<<< HEAD

=======
}
>>>>>>> travis/piechart
 
