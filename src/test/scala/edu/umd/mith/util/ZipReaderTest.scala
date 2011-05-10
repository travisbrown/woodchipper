package edu.umd.mith.util

import org.specs._
import java.io.File

class ZipReaderTest extends SpecificationWithJUnit {
  val testName = "ark+=13960=t00z79g41"
  val testFile = new File(this.getClass.getResource(testName + ".zip").toURI)
  val reader = new ZipReader(testFile)
  val entries = reader.iterator.toIndexedSeq

  "the test zip file" should {
    "have 341 entries" in {
      entries.size must_== 341
    }

    "start with the directory entry" in {
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
    }
  }

  doLast {
    reader.close()
  }
}

