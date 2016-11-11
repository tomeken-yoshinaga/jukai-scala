package jukaiScala.merlin

import org.scalatest._
import breeze.linalg.DenseMatrix
import breeze.numerics.abs
import jukaiScala.hdflib.H5Util

/**
  * Created by ubuntu on 10/18/16.
  */
class ConvSpec extends FlatSpec with Matchers{

  "Convolution.convert" should "match gold matrix of model file" in {
    val embFile = "./target/test-classes/data/tokenizer_test_embedding.h5"
    val cvFile = "./target/test-classes/data/tokenizer_test_convolution.h5"
    val cvConvertFile = "./target/test-classes/data/tokenizer_test_convolution_converted.h5"

    val embModel = H5Util.loadData(embFile).child.head
    val cvModel = H5Util.loadData(cvFile).child.head
    val goldData = H5Util.loadData(cvConvertFile).child.head.child(1)

    val embedding = Embedding(embModel)
    val conv = Conv(cvModel)

    val inputData = DenseMatrix(Array(0.0,1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0).map(x=>x.toFloat))

    val convertedData = conv.convert(embedding.convert(inputData))

    val goldMatrix = DenseMatrix.zeros[Float](10,4)
    for (y <- 0 until 10)
      for (x <- 0 until 4)
        goldMatrix(y, x) = goldData(x, y).asInstanceOf[Float]

    val diff = abs(convertedData - goldMatrix).forall(x => x < 1e-5)

    diff should be (true)
  }
}
