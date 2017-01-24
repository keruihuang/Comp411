import junit.framework.*;

import java.io.*;

public class Assign1Test extends TestCase {

  public Assign1Test (String name) {
    super(name);
  }
  
  protected void checkString(String name, String answer, String program) {
    Parser p = new Parser(new StringReader(program));
    assertEquals(name, answer, p.parse().toString());
  }
  
  
  protected void checkFile(String name, 
    String answerFilename,
    String programFilename) {
    try {
      File answerFile = new File(answerFilename);
      InputStream fin = new BufferedInputStream(new FileInputStream(answerFile));
      
      int size = (int) answerFile.length();
      byte[] data = new byte[size];
      fin.read(data,0,size);
      String answer = new String(data);
      
      
      Parser p = new Parser(programFilename);
      assertEquals(name, answer, p.parse().toString());      
    } catch (IOException e) {
      fail("Critical error: IOException caught while reading input file");
      e.printStackTrace();
    }
    
  }

  

  public void testAdd() {
    try {
      String output = "(2 + 3)";
      String input = "2+3";
      checkString("add", output, input );

    } catch (Exception e) {
      fail("add threw " + e);
    }
  } //end of func
  

  public void testPrim  () {
    try {
      String output = "first";
      String input = "first";
      checkString("prim  ", output, input );

    } catch (Exception e) {
      fail("prim   threw " + e);
    }
  } //end of func
  

  public void testParseException() {
    try {
      String output = "doh!";
      String input = "map a, to 3";
      checkString("parseException", output, input );

      fail("parseException did not throw ParseException exception");
    } catch (ParseException e) {   
         //e.printStackTrace();

    } catch (Exception e) {
      fail("parseException threw " + e);
    }
  } //end of func
  

  public void testLet() {
    try {
      String output = "let a := 3; in (a + a)";
      String input = "let a:=3; in a + a";
      checkString("let", output, input );

    } catch (Exception e) {
      fail("let threw " + e);
    }
  } //end of func
  

  public void testMap() {
    try {
//      String output = "map f to (map x to f(x(x)))(map x to f(x(x)))";
//      String input = "map f to (map x to f( x( x ) ) ) (map x to f(x(x)))";
      String output = "map a to 3";
      String input = "map a to 3";
      checkString("map", output, input );

    } catch (Exception e) {
      fail("map threw " + e);
    }
  } //end of func

  public void testIf() {
    try {
      String output = "if true then 3 else 2";
      String input = "if true then 3 else 2";
      checkString("if", output, input);
    } catch (Exception e) {
      fail("if threw" + e);
    }
  }

  public void testSimpleGood05() {
    try {
    String output = "(1 + (2 * 3))";
    String input = "1 + 2 * 3";
    checkString("add", output, input);
    } catch (Exception e) {
      fail ("good05 threw " + e);
    }
  }

  public void testSimpleGood06() {
    try {
      String output = "((1 + 2) * 3)";
      String input = "(1 + 2) * 3";
      checkString("add", output, input);
    } catch (Exception e) {
      fail ("good06 threw " + e);
    }
  }

  public void testMediumGood07() {
    try {
      String output = "map  to (x + (y - z))";
      String input = "map to x + y - z";
      checkString("map", output, input);
    } catch (Exception e) {
      fail ("good07 threw " + e);
    }
  }

    public void testConstant() {
    try {
      String output = "true";
      String input = "true";
      checkString("constant", output, input);
    } catch (Exception e) {
      fail ("constant threw " + e);
    }
  }  
}