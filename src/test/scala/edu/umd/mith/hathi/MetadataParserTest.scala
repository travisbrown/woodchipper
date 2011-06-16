package edu.umd.mith.hathi

import org.specs._
import java.io.File

class MetadataParserTest extends SpecificationWithJUnit {
  val metadataName = "non_google_pd_pdus.xml"
  val metadataFile = new File(this.getClass.getResource(metadataName).toURI)
  val parser = new MetadataParser(metadataFile)

  val records = parser.iterator.toList 

  "the test metadata file" should {
    "have 47 entries" in {
      records.size must_== 47
    }

    /*"start with the directory entry" in {
      entries(0)._1 must_== testName + "/"
    }

    "contain correctly named files in order" in {
      entries.drop(1).zipWithIndex.foreach { case ((name, _), i) =>
        name must_== "%s/%08d.txt".format(testName, i + 1)
      }
    }

    "have the correct page as its third entry" in {
      val lines = entries(2)._2.getLines.toIndexedSeq
      lines.size must_== 10
      lines(0) must_== ""
      lines(1) must_== " "
      lines(2) must_== " Columbia ©nitJem'tp "
      lines(3) must_== " "
      lines(4) must_== " THE LIBRARIES "
      lines(5) must_== " "
      lines(6) must_== " Bequest of "
      lines(7) must_== " Frederic Bancroft "
      lines(8) must_== " "
      lines(9) must_== " 1860-1945"
    }*/
  }
}

