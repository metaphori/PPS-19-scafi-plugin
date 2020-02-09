package it.unibo.scafi.plugin

import it.unibo.scafi.definition.AggregateFunction.{aggFun, args, block}
import it.unibo.scafi.definition.{F, L, T}
import it.unibo.scafi.plugin.TypeCheckComponent.aggregateTypeError
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PluginArgumentsTest extends PluginTest {
  val repSig = aggFun("rep", returns = L, args(block(L), block(T)))
  val compilerNoWraps = new ScafiCompilerPlatform(false, "wrap=disable")
  val compilerNoError = new ScafiCompilerPlatform(false, "error=disable")
  //Same test of AggregateWrapsTest, but with wrap=disable option.
  "Scafi plugin with wrap component disabled" should "no wraps lambda using aggregate" in {
    val (singleCode, singleReport) = compilerNoWraps.transform(writeInMain {
      """
        | foldhood{15}{(x,y) => {x;y}}{nbr(10)}
      """.stripMargin
    })
    singleReport.hasErrors shouldBe false
    singleCode.contains(
      """|aggregate({
         |      x;
         |      y
         |    }))""".stripMargin) shouldBe false
  }

  "Scafi plugin with no error" should "not raise an error if there is field value in rep" in {
    val report = compilerNoError.compile(writeInMain {
      """
        | rep{nbr{10}}{x => x}
    """.stripMargin
    })
    report.hasErrors shouldBe false
    report.hasWarnings shouldBe true
    report.errors.contains(aggregateTypeError(repSig, L, F)) shouldBe false
    report.warnings.contains(aggregateTypeError(repSig, L, F)) shouldBe true
  }
}
