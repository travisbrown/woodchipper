package edu.umd.mith.hathi.util

class DateCleaner {
  def parseYearField(value: String): Option[(Int, Option[Int])] = {
    val AbbrevYearRange = """.*(\d{4})\-(\d{2}).*""".r
    val YearRange = """.*(\d{4}).*(\d{4}).*""".r
    val Year = """.*(\d{4}).*""".r
    val Decade = """.*(\d{3})\-.*""".r
    val Century = """.*(\d{2})\-.*""".r
    value.replaceAll("""[\.\[\], ]""", "") match {
      case AbbrevYearRange(start, end) =>
        Some((start.toInt, Some((start.substring(0, 2) + end).toInt)))
      case YearRange(start, end) => Some((start.toInt, Some(end.toInt)))
      case Year(start) => Some((start.toInt, None))
      case Decade(known) => {
        val decade = known.toInt
        Some((decade * 10, Some(decade * 10 + 9)))
      }
      case Century(known) => {
        val century = known.toInt
        Some((century * 100, Some(century * 100 + 99)))
      }
      case RomanNumeral(start) => Some((start, None))
      case _ => None 
    }
  }
}

object RomanNumeral {
  private val numerals = List(("M", 1000), ("CM", 900), ("D", 500),
                              ("CD", 400), ("C",  100), ("XC", 90),
                              ("L",   50), ("XL",  40), ("X",  10),
                              ("IX",   9), ("V",    5), ("IV",  4),
                              ("I",    1))

  def unapply(s: String): Option[Int] = s.toUpperCase match {
    case "" => Some(0)
    case s: String => {
      numerals.filter {
        case (n, _) => s.startsWith(n)
      } match {
        case (n: String, i: Int) :: _ =>
          RomanNumeral.unapply(s.substring(n.length)).map(_ + i)
        case Nil => None
      }
    }
  }

  def main(args: Array[String]) {
    println(RomanNumeral.unapply(args(0)))
  }
}

