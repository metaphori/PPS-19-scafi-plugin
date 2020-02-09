package it.unibo.scafi.plugin

import scala.tools.nsc.ast.TreeDSL
import scala.tools.nsc.transform.Transform

class TransformComponent(val c : ComponentContext) extends AbstractComponent(c, TransformComponent)
  with Transform
  with TreeDSL {
  import global._ //needed to avoid global.type for each compiler type

  override protected def newTransformer(unit: CompilationUnit): Transformer = {
    global.inform(phaseName)
    AggregateProgramTransformer
  }

  private object AggregateProgramTransformer extends Transformer {
    override def transform(tree: global.Tree): global.Tree = {
      extractAggregateMain(tree) match {
        case None => super.transform(tree)
        case Some(_) => WrapFunction.transform(tree)
      }
    }
  }

  private object WrapFunction extends Transformer {
    override def transform(tree: global.Tree): global.Tree = {
      tree match {
        case q"(..$args) => aggregate($body)" => super.transform(tree)
        case q"(..$args) => { ..$body }" => q"(..$args) => aggregate{ ..$body }"
        case _ => super.transform(tree)
      }
    }
  }

  override def processOption(name : String, value : String): Unit = (name, value) match {
    case ("wrap", "disable") => this.disable()
    case _ => super.processOption(name, value) //TODO really important, is a good way? it is better a template method?
  }
}
object TransformComponent extends ComponentDescriptor  {
  override def name: String = "scafi-transform"

  override val runsAfter: List[String] = List("parser")

  override val runsBefore: List[String] = List("namer")

  def apply()(implicit c : ComponentContext) : TransformComponent = new TransformComponent(c)
}
