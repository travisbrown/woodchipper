package edu.umd.mith.hathi

import org.specs2.mutable._
import org.specs2.specification.Scope
import java.io.File

class MetadataParserTest extends SpecificationWithJUnit {
  "the test metadata file" should {
    "have 47 entries" in new Record {
      this.records.size must_== 47
    }
  }
}

trait Record extends Scope {
  val records = new MetadataParser(
    new File(this.getClass.getResource("non_google_pd_pdus.xml").toURI)
  ).iterator.toIndexedSeq
}

