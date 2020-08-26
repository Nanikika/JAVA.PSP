import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;
import java.util.regex.*;

// Clase utilizada para almacenar y calcular de manera mas sencilla los lineamientos puestos para el analizis
// Siendo estos: (clases, metodos, comentarios, variables, etc)
class Lineamientos {
  Map<Integer, HashMap<String, Integer>> lines = new HashMap<Integer, HashMap<String, Integer>>();
  Integer total = 0;
  Pattern pattern;

  public void setLine(Integer line, Integer quantity) {
    HashMap<String, Integer> newLine = new HashMap<String, Integer>();
    newLine.put("number", line);
    newLine.put("quantity", 1);
    lines.put(line, newLine);
  }

  public void setPattern(String patternString) {
    pattern = Pattern.compile(patternString);
  }

  public Boolean match(String line, Integer lineNumber) {
    Matcher m = this.pattern.matcher(line);
    Boolean match = m.find();
    if (match) {
      this.setLine(lineNumber, 1);
      this.total += 1;
    }
    return match;
  }

  public static void main(String[] args) {
  }
}

public class ReadFile {
  public static List<String> readFileInList(String fileName) {
    List<String> lines = Collections.emptyList();
    try {
      lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return lines;
  }

  public static void matchAttributes(Lineamientos attributes, String line, Integer lineNumber) {
    Matcher m = attributes.pattern.matcher(line);
    if (m.find()) {
      Integer quantity = line.split(",").length;
      attributes.setLine(lineNumber, quantity);
      attributes.total += quantity;
    }
  }

  public static void main(String[] args) {
    // Archivo a leer y analizar lineas de codigo (LOC)
    List<String> l = readFileInList("test.java");

    Lineamientos classes = new Lineamientos();
    Lineamientos methods = new Lineamientos();
    Lineamientos attributes = new Lineamientos();
    Lineamientos totalDeadLines = new Lineamientos();
    Lineamientos totalCommentLines = new Lineamientos();
    Integer totalLines = 0;
    Integer totalExecutableLines = 0;

    classes.setPattern(
        "\\s*(public|private)\\s+class\\s+(\\w+)\\s+((extends\\s+\\w+)|(implements\\s+\\w+( ,\\w+)*))?\\s*\\{");
    methods.setPattern(
        "\\s*(public|protected|private|static|\\s) +[\\w\\<\\>\\[\\],\\s]+\\s+(\\w+) *\\([^\\)]*\\) *(\\{?|[^;])");
    attributes.setPattern("\\((.*)(,?(.*))*\\)");
    totalCommentLines.setPattern("\\s*(//)");

    Iterator<String> itr = l.iterator();
    Boolean mainState = false;
    Integer blockNest = 0;
    while (itr.hasNext()) {
      String next = itr.next();
      totalLines += 1;
      try {
        if (next.length() <= 0) {
          totalDeadLines.setLine(totalLines, 0);
          totalDeadLines.total += 1;
        }
        Boolean commentMatch = totalCommentLines.match(next, totalLines);

        if (!commentMatch) {
          classes.match(next, totalLines);
          Boolean methodMatch = methods.match(next, totalLines);
          matchAttributes(attributes, next, totalLines);

          if (mainState) {
            totalExecutableLines += 1;
          }

          if (methodMatch && next.contains("main")) {
            mainState = true;
          }

          if (mainState && (!next.contains("main") && next.contains("{"))) {
            blockNest += 1;
          }
          if (mainState && next.contains("}")) {
            blockNest -= 1;
            totalExecutableLines -= 1;
          }

          if (blockNest < 0) {
            blockNest = 0;
            mainState = false;
          }
        }
      } catch (IllegalStateException e) {
        System.out.println(e);
      }
    }
    System.out.println("Total Classes " + classes.total);
    System.out.println("Total Methods " + methods.total);
    System.out.println("Total Attributes " + attributes.total);
    System.out.println("Total Comment Lines " + totalCommentLines.total);
    System.out.println("Total Deadlines " + totalDeadLines.total);
    System.out.println("Total Executable Lines " + totalExecutableLines);
    System.out.println("Total Lines " + totalLines);
  }
}
