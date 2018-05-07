 /**
 * JUunit tests for the Parser for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Spring 2018.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Spring 2018 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2018
 */

package cop5556sp18;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Parser;
import cop5556sp18.Scanner;
import cop5556sp18.Parser.SyntaxException;
import cop5556sp18.Scanner.LexicalException;

public class SimpleParserTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}


	//creates and returns a parser for the given input.
	private Parser makeParser(String input) throws LexicalException {
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		Parser parser = new Parser(scanner);
		return parser;
	}
	
	

	/**
	 * Simple test case with an empty program.  This throws an exception 
	 * because it lacks an identifier and a block. The test case passes because
	 * it expects an exception
	 *  
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = "";  //The input is the empty string.  
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	/**
	 * Smallest legal program.
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testSmallest() throws LexicalException, SyntaxException {
		String input = "b{}";  
		Parser parser = makeParser(input);
		parser.parse();
	}	
	
	
	//This test should pass in your complete parser.  It will fail in the starter code.
	//Of course, you would want a better error message. 
	@Test
	public void testDec0() throws LexicalException, SyntaxException {
		String input = "b{int c;}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	

@Test
public void testSamples() throws LexicalException, SyntaxException {
    String input = "abc{show 23;}";
    Parser parser = makeParser(input);
    parser.parse();
}

@Test
public void testSample2() throws LexicalException, SyntaxException {
    String input = "demo1{image h;input h from @0;show h; sleep(4000); image g[width(h),height(h)];int x;x:=0;while(x<width(g)){int y;y:=0;while(y<height(g)){g[x,y]:=h[y,x];y:=y+1;};x:=x+1;};show g;sleep(4000);}";
    Parser parser = makeParser(input);
    parser.parse();
}

@Test
public void makeRedImage() throws LexicalException, SyntaxException {
    String input = "makeRedImage{image im[256,256];int x;int y;x:=0;y:=0;while(x<width(im)) {y:=0;while(y<height(im)) {im[x,y]:=<<255,255,0,0>>;y:=y+1;};x:=x+1;};show im;}";
    Parser parser = makeParser(input);
    parser.parse();
}

@Test
public void testPolarR2() throws LexicalException, SyntaxException {
    String input = "PolarR2{image im[1024,1024];int x;x:=0;while(x<width(im)) {int y;y:=0;while(y<height(im)) {float p;p:=polar_r[x,y];int r;r:=int(p)%Z;im[x,y]:=<<Z,0,0,r>>;y:=y+1;};x:=x+1;};show im;}";
    Parser parser = makeParser(input);
    parser.parse();
}
@Test
public void testSamples3() throws LexicalException, SyntaxException {
    String input = "samples{image bird; input bird from @0;show bird;sleep(4000);image bird2[width(bird),height(bird)];int x;x:=0;while(x<width(bird2)) {int y;y:=0;while(y<height(bird2)) {blue(bird2[x,y]):=red(bird[x,y]);green(bird2[x,y]):=blue(bird[x,y]);red(bird2[x,y]):=green(bird[x,y]);alpha(bird2[x,y]):=Z;y:=y+1;};x:=x+1;};show bird2;sleep(4000);}";
    Parser parser = makeParser(input);
    parser.parse();	

}

@Test
public void testSamples4() throws LexicalException, SyntaxException {
    String input = "example{if(((2+3)*(5*3))==0) {};}";
    Parser parser = makeParser(input);
    parser.parse();
}

// neww *************************

@Test
public void testDec1() throws LexicalException, SyntaxException {
	String input = "abc{float c; boolean re; image abc; filename FILE2; image newImage [320,87];}";
	Parser parser = makeParser(input);
	parser.parse();
}




@Test
public void testStatementIfSmallest() throws LexicalException, SyntaxException {
	String input = "back{if (!true) {show aefjbc;} ;}";
	Parser parser = makeParser(input);
	parser.parse();
}

@Test
public void testStatementWhileSmallest() throws LexicalException, SyntaxException {
	String input = "b{while (make) {sleep 109;} ;}";
	Parser parser = makeParser(input);
	parser.parse();
}



@Test
public void testStatementAssignLHS2Smallest() throws LexicalException, SyntaxException {
	String input = "b{m := (abc[10,10]);}";
	Parser parser = makeParser(input);
	parser.parse();
}

@Test
public void testOrExpresssion0Smallest() throws LexicalException, SyntaxException {
	String input = "b{ A := true | abcdes | 123456;}";
	Parser parser = makeParser(input);
	parser.parse();
}



@Test
public void testAndExpresssionSmallest() throws LexicalException, SyntaxException {
	String input = "b{ s := 1+4 != 2-6 == 6.2 & true & !false ;}";
	Parser parser = makeParser(input);
	parser.parse();
}


@Test
public void testRelExpresssionSmallest() throws LexicalException, SyntaxException {
	String input = "b{ s := a+b < 5-2 > 65 >= 5**2 <= 6%2 ;}";
	Parser parser = makeParser(input);
	parser.parse();
}



}
	

