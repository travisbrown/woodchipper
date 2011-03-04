package edu.umd.mith.cc.analysis

trait Reduction {
  def data: Array[Array[Double]]
}

trait Reducer[B <: Reduction] {
  def reduce(data: Array[Array[Double]]): B
}

