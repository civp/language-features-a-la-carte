package syntactic.whitelist

import org.junit.Test
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import PredefFeatures.allDefinedFeatures

class PredefFeaturesTests {

  @Test
  def allDefinedFeaturesTest(): Unit = {
    assertEquals(23, allDefinedFeatures.size)
    assertTrue(allDefinedFeatures.contains(PredefFeatures.Defs))
    assertTrue(allDefinedFeatures.contains(PredefFeatures.LiteralsAndExpressions))
    assertTrue(allDefinedFeatures.contains(PredefFeatures.Inlines))

    // check that AdvancedOopAddition and BasicOopAddition are not in the list since they are private
    assertFalse(allDefinedFeatures.exists(_.getClass.getName.contains("Addition")))
  }

}
