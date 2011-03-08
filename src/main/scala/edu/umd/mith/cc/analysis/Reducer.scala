package edu.umd.mith.cc.analysis

trait Reduction {
  def data: Array[Array[Double]]
  def dims: Int = this.data(0).length
}

trait Reducer[B <: Reduction] {
  def reduce(data: Array[Array[Double]], dims: Int): B
  def reduce(data: Array[Array[Double]]): B = this.reduce(data, Int.MaxValue)
}

