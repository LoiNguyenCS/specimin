package org.checkerframework.specimin;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/** This class is the main runner for Specimin. Use its main() method to start Specimin. */
public class SpeciminRunner {

  /**
   * The main entry point for Specimin.
   *
   * @param args the arguments to Specimin
   */
  public static void main(String... args) throws IOException {
    OptionParser optionParser = new OptionParser();

    // This option is the root of the source directory of the target files. It is used
    // for symbol resolution from source code and to organize the output directory.
    OptionSpec<String> rootOption = optionParser.accepts("root").withRequiredArg();

    // This option is the relative paths to the target file(s) - the .java file(s) containing
    // target method(s) - from the root.
    OptionSpec<String> targetFilesOption = optionParser.accepts("targetFile").withRequiredArg();

    // This option is the target methods, specified in the format
    // class.fully.qualified.Name#methodName(Param1Type, Param2Type, ...)
    OptionSpec<String> targetMethodsOption = optionParser.accepts("targetMethod").withRequiredArg();

    // The directory in which to output the results.
    OptionSpec<String> outputDirectoryOption =
        optionParser.accepts("outputDirectory").withRequiredArg();

    OptionSet options = optionParser.parse(args);

    String root = options.valueOf(rootOption);
    List<String> targetFiles = options.valuesOf(targetFilesOption);

    // Set up the parser's symbol solver, so that we can resolve definitions.
    TypeSolver typeSolver =
        new CombinedTypeSolver(
            new ReflectionTypeSolver(), new JavaParserTypeSolver(new File(root)));
    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
    StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

    // Keys are paths to files, values are parsed ASTs
    Map<String, CompilationUnit> parsedTargetFiles = new HashMap<>();
    for (String targetFile : targetFiles) {
      parsedTargetFiles.put(targetFile, parseJavaFile(root, targetFile));
    }

    UnsolvedSymbolVisitor addMissingClass = new UnsolvedSymbolVisitor(root);
    /**
     * The set of path of files that have been created by addMissingClass. We will delete all those
     * files in the end.
     */
    Set<Path> createdClass = new HashSet<>();
    while (addMissingClass.gettingException()) {
      addMissingClass.setExceptionToFalse();
      for (CompilationUnit cu : parsedTargetFiles.values()) {
        addMissingClass.setImportStatement(cu.getImports());
        cu.accept(addMissingClass, null);
      }
      addMissingClass.updateSyntheticSourceCode();
      createdClass.addAll(addMissingClass.getCreatedClass());
      // since the root directory is updated, we need to update the SymbolSolver
      TypeSolver newTypeSolver =
          new CombinedTypeSolver(
              new ReflectionTypeSolver(), new JavaParserTypeSolver(new File(root)));
      JavaSymbolSolver newSymbolSolver = new JavaSymbolSolver(newTypeSolver);
      StaticJavaParser.getConfiguration().setSymbolResolver(newSymbolSolver);
      parsedTargetFiles = new HashMap<>();
      for (String targetFile : targetFiles) {
        parsedTargetFiles.put(targetFile, parseJavaFile(root, targetFile));
      }
    }
    List<String> targetMethodNames = options.valuesOf(targetMethodsOption);

    // Use a two-phase approach: the first phase finds the target(s) and records
    // what specifications they use, and the second phase takes that information
    // and removes all non-used code.

    TargetMethodFinderVisitor finder = new TargetMethodFinderVisitor(targetMethodNames);

    for (CompilationUnit cu : parsedTargetFiles.values()) {
      cu.accept(finder, null);
    }

    List<String> unfoundMethods = finder.getUnfoundMethods();
    if (!unfoundMethods.isEmpty()) {
      throw new RuntimeException(
          "Specimin could not locate the following target methods in the target files: "
              + String.join(", ", unfoundMethods));
    }

    // add all files related to the targeted methods to the parsedTargetFile
    for (String classFullName : finder.getUsedClass()) {
      String directoryOfFile = classFullName.replace(".", "/") + ".java";
      parsedTargetFiles.put(directoryOfFile, parseJavaFile(root, directoryOfFile));
    }
    MethodPrunerVisitor methodPruner =
        new MethodPrunerVisitor(finder.getTargetMethods(), finder.getUsedMethods());

    for (CompilationUnit cu : parsedTargetFiles.values()) {
      // This must happen before any modifications to each compilation unit, or
      // the printer won't know about them. (It registers an observer.)
      LexicalPreservingPrinter.setup(cu);
      cu.accept(methodPruner, null);
    }

    String outputDirectory = options.valueOf(outputDirectoryOption);

    for (Entry<String, CompilationUnit> target : parsedTargetFiles.entrySet()) {
      // If a compilation output's entire body has been removed, do not output it.
      if (isEmptyCompilationUnit(target.getValue())) {
        continue;
      }

      Path targetOutputPath = Path.of(outputDirectory, target.getKey());
      // Create any parts of the directory structure that don't already exist.
      Path dirContainingOutputFile = targetOutputPath.getParent();
      // This null test is very defensive and might not be required? I think getParent can
      // only return null if its input was a single element path, which targetOutputPath
      // should not be unless the user made an error.
      if (dirContainingOutputFile != null) {
        Files.createDirectories(dirContainingOutputFile);
      }
      try {
        Files.write(
            targetOutputPath,
            LexicalPreservingPrinter.print(target.getValue()).getBytes(StandardCharsets.UTF_8));
      } catch (IOException e) {
        System.out.println("failed to write output file " + targetOutputPath);
        System.out.println("with error: " + e);
      }
    }
    // delete all the temporary files created by UnsolvedSymbolVisitor
    deleteFiles(createdClass);
  }

  /**
   * Checks whether the given compilation unit contains nothing. Should conservatively return false
   * by default if unsure.
   *
   * @param cu any compilation unit
   * @return true iff this compilation unit is totally empty and can therefore be safely removed
   *     entirely
   */
  private static boolean isEmptyCompilationUnit(CompilationUnit cu) {
    for (Node child : cu.getChildNodes()) {
      if (child instanceof PackageDeclaration) {
        // Package declarations don't count for the purposes of
        // deciding whether to entirely remove a compilation unit.
        continue;
      } else if (child instanceof ClassOrInterfaceDeclaration) {
        ClassOrInterfaceDeclaration cdecl =
            ((ClassOrInterfaceDeclaration) child).asClassOrInterfaceDeclaration();
        if (!cdecl.getMembers().isEmpty()) {
          return false;
        }
      } else {
        return false;
      }
    }
    return true;
  }

  /**
   * Use JavaParser to parse a single Java files.
   *
   * @param root the absolute path to the root of the source tree
   * @param path the path of the file to be parsed, relative to the root
   * @return the compilation unit representing the code in the file at the path, or exit with an
   *     error
   */
  private static CompilationUnit parseJavaFile(String root, String path) throws IOException {
    return StaticJavaParser.parse(Path.of(root, path));
  }

  /**
   * This method delete all files from a set of Paths. If a file is the only file in its parent
   * directory, this method will recursively delete the parent directories until it meets a
   * non-empty directory.
   *
   * @param fileList the set of Paths of files to be deleted
   */
  private static void deleteFiles(Set<Path> fileList) {
    for (Path filePath : fileList) {
      try {
        Files.delete(filePath);
        File parentDir = filePath.toFile().getParentFile();
        if (parentDir != null && parentDir.exists() && parentDir.isDirectory()) {
          String[] fileContained = parentDir.list();
          if (fileContained != null && fileContained.length == 0) {
            // Recursive call to delete the parent directory
            deleteFileFamily(parentDir);
          }
        }
      } catch (Exception e) {
      }
    }
  }

  /**
   * Given a directory, this method will delete that directory and recursively delete the parent
   * directories until it meets a non-empty directory. Be careful when making any changes to this
   * method. This method is used to delete the temporary directory created by UnsolvedSymbolVisitor.
   *
   * @param fileDir the directory of the file to be deleted
   */
  private static void deleteFileFamily(File fileDir) {
    fileDir.delete();
    File parentDir = fileDir.getParentFile();
    if (parentDir != null && parentDir.exists() && parentDir.isDirectory()) {
      String[] fileContained = parentDir.list();
      // Be cautious when making any changes to this line, you might actually delete important
      // directories in the project.
      if (fileContained != null && fileContained.length == 0) {
        deleteFileFamily(parentDir);
      }
    }
  }
}
