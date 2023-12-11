package org.checkerframework.specimin;

import java.util.List;

/**
 * An UnsolvedMethod instance is a representation of a method that can not be solved by
 * SymbolSolver. The reason is that the class file of that method is not in the root directory.
 */
public class UnsolvedMethod {
  /** The name of the method */
  private final String name;

  /**
   * The return type of the method. At the moment, we set the return type the same as the class
   * where the method belongs to.
   */
  private String returnType;

  /**
   * The list of parameters of the method. (Right now we won't touch it until the new variant of
   * SymbolSolver is available)
   */
  private List<String> parameterList;

  /** This field is set to true if this method is a static method */
  private boolean isStatic = false;

  /**
   * Create an instance of UnsolvedMethod
   *
   * @param name the name of the method
   * @param returnType the return type of the method
   * @param parameterList the list of parameters for this method
   */
  public UnsolvedMethod(String name, String returnType, List<String> parameterList) {
    this.name = name;
    this.returnType = returnType;
    this.parameterList = parameterList;
  }

  /**
   * Set the value of returnType. This method is used when javac tells us that UnsolvedSymbolVisitor
   * get the return types wrong.
   *
   * @param returnType the return type to bet set for this method
   */
  public void setReturnType(String returnType) {
    this.returnType = returnType;
  }

  /**
   * Get the return type of this method
   *
   * @return the value of returnType
   */
  public String getReturnType() {
    return returnType;
  }

  /**
   * Get the name of this method
   *
   * @return the name of this method
   */
  public String getName() {
    return name;
  }

  /**
   * Get the list of parameters for this method
   *
   * @return the value of parameterList
   */
  public List<String> getParameterList() {
    return parameterList;
  }

  /** Set isStatic to true */
  public void setStatic() {
    isStatic = true;
  }

  /**
   * Return the content of the method. Note that the body of the method is stubbed out.
   *
   * @return the content of the method with the body stubbed out
   */
  @Override
  public String toString() {
    String arguments = "";
    for (int i = 0; i < parameterList.size(); i++) {
      String parameter = parameterList.get(i);
      String parameterName = "parameter" + i;
      arguments = arguments + parameter + " " + parameterName;
      if (i < parameterList.size() - 1) {
        arguments = arguments + ", ";
      }
    }
    String returnTypeInString = "";
    if (!returnType.equals("")) {
      returnTypeInString = returnType + " ";
    }
    String staticField = "";
    if (isStatic) {
      staticField = "static ";
    }
    return "\n    public "
        + staticField
        + returnTypeInString
        + name
        + "("
        + arguments
        + ") {\n        throw new Error();\n    }\n";
  }

  /**
   * This method checks if the current instance of UnsolvedMethod is equal to another instance of
   * UnsolvedMethod.
   *
   * @param other the other instance of UnsolvedMethod
   * @return true if two instances are equal
   */
  public boolean equalTo(UnsolvedMethod other) {
    return isStatic == other.isStatic
        && other.getName().equals(this.getName())
        && other.getReturnType().equals(this.getReturnType())
        && other.getParameterList().equals(this.getParameterList());
  }
}
