package edu.umd.mith.woodchipper.analysis

import cern.colt.matrix.impl.DenseDoubleMatrix2D
import cern.colt.matrix.doublealgo.Statistic
import cern.colt.matrix.linalg.EigenvalueDecomposition
import cern.colt.matrix.linalg.Algebra
//import cern.jet.math.Functions

class PCAReduction(
  val data: IndexedSeq[IndexedSeq[Double]],
  val variance: IndexedSeq[Double],
  val loadings: IndexedSeq[IndexedSeq[Double]]
) extends Reduction {
}

class PCAReducer extends Reducer[PCAReduction] {
  def reduce(data: IndexedSeq[IndexedSeq[Double]], dims: Int): PCAReduction = {
    val dists = data.map { i =>
      data.map { j =>
        i.zip(j).map { case (x, y) => x * math.log(x / y) }.sum
      }.toArray
    }.toArray

    val pos = Array.fill(dims, data.size)(0.)
    new mdsj.StressMinimization(dists, pos)

    new PCAReduction(pos.transpose.map(_.toIndexedSeq), IndexedSeq.fill(dims)(0.0), IndexedSeq.fill(dims, data(0).length)(0.0))
  }
}

class RealPCAReducer extends Reducer[PCAReduction] {
  private val algebra = new Algebra

  def reduce(data: IndexedSeq[IndexedSeq[Double]], dims: Int): PCAReduction = {
    val matrix = new DenseDoubleMatrix2D(data.map(_.toArray).toArray)

    /* First we compute the empirical mean. */
    val cols = matrix.columns
    val rows = matrix.rows

    for (i <- 0 until cols) {
      val means = matrix.viewColumn(i).zSum / rows
      for (j <- 0 until rows) {
        matrix.set(j, i, matrix.get(j, i) - means)
      }
    }

    /* Next the eigenvalue decomposition of the covariance matrix. */
    val cov = Statistic.covariance(matrix)
    val evd = new EigenvalueDecomposition(cov)

    val v = evd.getV.viewColumnFlip

    /* Finally we take the projection of the points onto the new basis. */
    val projection = this.algebra.mult(matrix, v)
    //val projection: DoubleMatrix2D = this.algebra.mult(matrix, evd.getV).assign(Functions.abs).viewColumnFlip

    val colsSelected = math.min(dims, projection.columns)
    val dataView = projection.viewPart(0, 0, projection.rows, colsSelected)

    val evs = evd.getRealEigenvalues.viewFlip
    val evt = evs.zSum

    val varianceView = evs.viewPart(0, colsSelected)
    val loadingsView = v.viewDice.viewPart(0, 0, colsSelected, v.rows)

    new PCAReduction(dataView.toArray.map(genericWrapArray), varianceView.toArray.map(_ / evt), loadingsView.toArray.map(genericWrapArray))
  }
}

