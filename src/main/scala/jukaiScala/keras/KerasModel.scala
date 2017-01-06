package jukaiScala.keras

/**
  * Created by kenta-yoshinaga on 2016/12/07.
  */

import ucar.nc2.NetcdfFile
import ucar.nc2.{Variable, Attribute, Group}

import org.json4s._
import org.json4s.DefaultFormats
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

class KerasModel(path:String) {

  private val model = NetcdfFile.open(path, null)

  private val rootGroup = model.getRootGroup

  def checkAndGetAttribute(name: String): Attribute = Option(rootGroup.findAttribute(name)) match {
    case Some(x) => x
    case None => throw new IllegalArgumentException("cannot get " + name + " attribute from input model file")
  }

  def checkAndGetGroup(name: String): Group = Option(rootGroup.findGroup(name)) match {
    case Some(x) => x
    case None => throw new IllegalArgumentException("cannot get " + name + " group from input model file")
  }

  private val kerasAttribute = checkAndGetAttribute("keras_version")
  private val modelAttribute = checkAndGetAttribute("model_config")

  private val weightGroups = checkAndGetGroup("model_weights")

  def parseConfigToList(config: String): List[Map[String, Any]] = {
    val jsonValue = parse(config)
    implicit val formats = DefaultFormats
    val jsonList = jsonValue.extract[Map[String, Any]]
    jsonList("config").asInstanceOf[List[Map[String, Any]]]
  }

  private val modelValues = parseConfigToList(modelAttribute.getValue(0).toString)

  def getConfigs(x: Map[String, Any]): Map[String, Any] = x("config").asInstanceOf[Map[ String, Any]]

  def constructNetwork(values: List[Map[String, Any]]):List[Functor] = values.map(
    x => {
      val configs = getConfigs(x)
      val functor = x("class_name").toString match{
        case "Activation" => {
          configs("activation").toString match{
            case "relu" => Relu
            case "softmax" => Softmax
            case "tanh" => Tanh
            case "sigmoid" => Sigmoid
          }
        }
        case "Convolution1D" => {
          Convolution1D(configs, weightGroups)
        }
        case "Dense" => {
          Dense(configs, weightGroups)
        }
        case "Embedding" => {
          Embedding(configs, weightGroups)
        }
        case "Flatten" => Flatten
      }
      functor
    }
  )

  val graph:List[Functor] = constructNetwork(modelValues)

}

object KerasModel{
  def apply(path:String): KerasModel = new KerasModel(path)
}
