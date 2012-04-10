package edu.umd.mith.woodchipper.analysis

trait Reduction {
  def data: IndexedSeq[IndexedSeq[Double]]
  def dims: Int = this.data.headOption.map(_.length).getOrElse(0)
}

trait Reducer[B <: Reduction] {
  def reduce(data: IndexedSeq[IndexedSeq[Double]], dims: Int): B
  def reduce(data: IndexedSeq[IndexedSeq[Double]]): B = this.reduce(data, Int.MaxValue)
}

