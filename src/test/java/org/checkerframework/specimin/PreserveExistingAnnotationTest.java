package org.checkerframework.specimin;

import java.io.IOException;
import org.junit.Test;

/**
 * This test ensures that Specimin will not remove annotations whose class files exist in the input
 * codebase.
 */
public class PreserveExistingAnnotationTest {
  @Test
  public void runTest() throws IOException {
    SpeciminTestExecutor.runTestWithoutJarPaths(
        "preservexistingannotation",
        new String[] {"com/google/common/util/concurrent/FuturesGetChecked.java"},
        new String[] {
          "com.google.common.util.concurrent.FuturesGetChecked.GetCheckedTypeValidatorHolder#getBestValidator()"
        });
  }
}
