package org.checkerframework.specimin;

import java.io.IOException;
import org.junit.Test;

/**
 * If a field is used by a target method and that field is initialized by another method, the method
 * that initialize the field should also be included in the final output.
 */
public class FieldInitializedByMethodCall {
  @Test
  public void runTest() throws IOException {
    SpeciminTestExecutor.runTestWithoutJarPaths(
        "fieldInitializedByMethodCall",
        new String[] {"com/example/Simple.java"},
        new String[] {"com.example.Simple#bar()"});
  }
}
