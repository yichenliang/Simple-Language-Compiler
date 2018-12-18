// Yichen Liang    p5   Nov 5 11:59pm

package cop5556fa18;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa18.PLPScanner;
import cop5556fa18.PLPTypeChecker.SemanticException;
import cop5556fa18.PLPAST.PLPASTVisitor;
import cop5556fa18.PLPAST.Program;

public class PLPTypeCheckerTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Prints objects in a way that is easy to turn on and off
	 */
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Scan, parse, and type check an input string
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		// instantiate a Scanner and scan input
		PLPScanner scanner = new PLPScanner(input).scan();
		show(scanner);
		// instantiate a Parser and parse input to obtain and AST
		Program ast = new PLPParser(scanner).parse();
		show(ast);
		// instantiate a TypeChecker and visit the ast to perform type checking and
		// decorate the AST.
		PLPASTVisitor v = new PLPTypeChecker();
		ast.visit(v, null);
	}
		
	@Test
	public void emptyProg() throws Exception {
		String input = "emptyProg{}";
		typeCheck(input);
	}

	@Test
	public void expression1() throws Exception {
		String input = "prog {print 1+2;}";
		typeCheck(input);
	}

	@Test
	public void expression2_fail() throws Exception {
		String input = "prog { print true+4; }"; //should throw an error due to incompatible types in binary expression
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void test1() throws Exception {
		String input = "prog {int a, b, c; b = 3; c = 4;}";
		typeCheck(input);
	}
	
	@Test
	public void test2() throws Exception {
		String input = "prog {int a = 3;}";
		typeCheck(input);
	}

	@Test
	public void test3() throws Exception {
		String input = "prog {char a = 'a';}";
		typeCheck(input);
	}
	
	@Test
	public void test4() throws Exception {
		String input = "prog {string a = \"it is a string\";}";
		typeCheck(input);
	}
	
	@Test
	public void test5() throws Exception {
		String input = "prog {int a; a = 3;}";
		typeCheck(input);
	}
	
	@Test
	public void test6() throws Exception {
		String input = "prog {char a; a = true;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void test7_ifStatement() throws Exception {
		String input = "prog {int a = 3; if(a==3) {sleep a;};}";
	    typeCheck(input);
	}
	
	@Test
	public void test8_ifStatement() throws Exception {
		String input = "prog {int a = 3; if(a==3) { a = 5;};}";
	    typeCheck(input);
	}
    
	@Test
	public void test9() throws Exception {
		String input = "prog {float a = 3.6 + 2;}";
	    typeCheck(input);
	}
	
	@Test
	public void test10() throws Exception {
		String input = "prog {float a; a = 3==3 ? float(3) : 3.6 + 2;}";
	    typeCheck(input);
	}
	
	@Test
	public void test11() throws Exception {
		String input = "prog {float a; a = true & false ? abs(3.1) : atan(1.1);}";
	    typeCheck(input);
	}
	
	@Test
	public void test12() throws Exception {
		String input = "prog {boolean a = 3 >= 4;}";
	    typeCheck(input);
	}
	
	@Test
	public void test13() throws Exception {
		String input = "prog {string a; string b = \"abs\"; string c = \"cds\"; a = b + c;}";
	     typeCheck(input);
	}
	
	@Test
	public void test14() throws Exception {
		String input = "prog {string a; string b = 'c'; string c = \"cds\"; a = b + c;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void test15() throws Exception {
		String input = "prog {float a = +3.5; boolean b = !true;}";
	     typeCheck(input);
	}
	
	@Test
	public void test16() throws Exception {
		String input = "prog {int a, b, c; a = 3; float d = float(a); if(a==3) {sleep a; b = int(3.33333);};}";
	     typeCheck(input);
	}
	
	@Test
	public void test17() throws Exception {
		String input = "prog {float decimal; int integer; decimal = integer % decimal;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}

	
}
