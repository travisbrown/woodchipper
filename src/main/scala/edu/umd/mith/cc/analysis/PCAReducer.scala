package edu.umd.mith.cc.analysis

import cern.colt.matrix.DoubleMatrix2D
import cern.colt.matrix.DoubleMatrix1D
import cern.colt.matrix.ObjectMatrix2D
import cern.colt.matrix.impl.DenseObjectMatrix2D
import cern.colt.matrix.impl.DenseDoubleMatrix2D
import cern.colt.matrix.impl.DenseDoubleMatrix1D
import cern.colt.matrix.doublealgo.Statistic
import cern.colt.matrix.linalg.EigenvalueDecomposition
import cern.colt.matrix.linalg.Algebra
import cern.jet.math.Functions

class TestFile(path: String) {
  val (names, data)  = scala.io.Source.fromFile(path).getLines.toIterable.map { line =>
    val fields = line.split("""\s""")
    (fields(0), fields.slice(1, fields.size).map(_.toDouble))
  }.unzip

  def matrix: Array[Array[Double]] = data.toArray
  def labels: List[String] = names.toList
}

class PCAReduction(
  val data: Array[Array[Double]]
) extends Reduction {
}

class PCAReducer extends Reducer[PCAReduction] {
  private val algebra = new Algebra

  def reduce(data: Array[Array[Double]]): PCAReduction = {
    val matrix = new DenseDoubleMatrix2D(data)

    /* First we compute the empirical mean. */
    val cols = matrix.columns
    val rows = matrix.rows
    val colMeans = new Array[Double](cols)
    for (i <- 0 until cols) {
      colMeans(i) = matrix.viewColumn(i).zSum / rows
    }

    for (i <- 0 until cols) {
      for (j <- 0 until rows) {
        matrix.set(j, i, matrix.get(j, i) - colMeans(i))
      }
    }

    /* Next the eigenvalue decomposition of the covariance matrix. */
    val cov = Statistic.covariance(matrix)
    val evd = new EigenvalueDecomposition(cov)

    /* Finally we take the projection of the points onto the new basis. */
    val projection: DoubleMatrix2D = this.algebra.mult(matrix, evd.getV).viewColumnFlip
    //val projection: DoubleMatrix2D = this.algebra.mult(matrix, evd.getV).assign(Functions.abs).viewColumnFlip

    new PCAReduction(projection.toArray)
  }
}

object PCAReducer {
  def main(args: Array[String]) {
    val file = new TestFile(args(0))
    val pc = new PCAReducer
    val reduced = pc.reduce(file.matrix)
    reduced.data.foreach {
      row => println(row.mkString(" "))
    }
  }
}

