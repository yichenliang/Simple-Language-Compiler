// Yichen Liang    p4   Oct 22   11:59pm
package cop5556fa18;

import static cop5556fa18.PLPScanner.Kind.OP_PLUS;
import static org.junit.Assert.assertEquals;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa18.PLPParser.SyntaxException;
import cop5556fa18.PLPScanner;
import cop5556fa18.PLPScanner.LexicalException;
import cop5556fa18.PLPAST.Block;
import cop5556fa18.PLPAST.Declaration;
import cop5556fa18.PLPAST.Expression;
import cop5556fa18.PLPAST.ExpressionBinary;
import cop5556fa18.PLPAST.ExpressionCharLiteral;
import cop5556fa18.PLPAST.ExpressionIdentifier;
import cop5556fa18.PLPAST.ExpressionIntegerLiteral;
import cop5556fa18.PLPAST.ExpressionStringLiteral;
import cop5556fa18.PLPAST.IfStatement;
import cop5556fa18.PLPAST.PLPASTNode;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPAST.VariableListDeclaration;
import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPAST.Statement;

public class PLPParserTest {
	
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
		private PLPParser makeParser(String input) throws LexicalException {
			show(input);        //Display the input 
			PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
			PLPParser parser = new PLPParser(scanner);
			return parser;
		}
		
		/**
		 * Test case with an empty program.  This throws an exception 
		 * because it lacks an identifier and a block
		 *   
		 * @throws LexicalException
		 * @throws SyntaxException 
		 */
		@Test
		public void testEmpty() throws LexicalException, SyntaxException {
			String input = "";  //The input is the empty string.  
			thrown.expect(SyntaxException.class);
			PLPParser parser = makeParser(input);
			@SuppressWarnings("unused")
			Program p = parser.parse();
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
			PLPParser parser = makeParser(input);
			Program p = parser.parse();
			show(p);
			assertEquals("b", p.name);
			assertEquals(0, p.block.declarationsAndStatements.size());
		}	
		
		
		/**
		 * Utility method to check if an element of a block at an index is a declaration with a given type and name.
		 * 
		 * @param block
		 * @param index
		 * @param type
		 * @param name
		 * @return
		 */
		Declaration checkDec(Block block, int index, Kind type, String name) {
			PLPASTNode node = block.declarationsAndStatements(index);
			assertEquals(VariableDeclaration.class, node.getClass());
			VariableDeclaration dec = (VariableDeclaration) node;
			assertEquals(type, dec.type);
			assertEquals(name, dec.name);
			return dec;
		}	
		
		@Test
		public void testDec0() throws LexicalException, SyntaxException {
			String input = "b{int i; char c;}";
			PLPParser parser = makeParser(input);
			Program p = parser.parse();
			show(p);	
			checkDec(p.block, 0, Kind.KW_int, "i");
			checkDec(p.block, 1, Kind.KW_char, "c");
		}
		
		
		/** 
		 * Test a specific grammar element by calling a corresponding parser method rather than parse.
		 * This requires that the methods are visible (not private). 
		 * 
		 * @throws LexicalException
		 * @throws SyntaxException
		 */
		
		@Test
		public void testExpression() throws LexicalException, SyntaxException {
			String input = "x + 2";
			PLPParser parser = makeParser(input);
			Expression e = parser.expression();  //call expression here instead of parse
			show(e);	
			assertEquals(ExpressionBinary.class, e.getClass());
			ExpressionBinary b = (ExpressionBinary)e;
			assertEquals(ExpressionIdentifier.class, b.leftExpression.getClass());//
			ExpressionIdentifier left = (ExpressionIdentifier)b.leftExpression;
			assertEquals("x", left.name);
			assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
			ExpressionIntegerLiteral right = (ExpressionIntegerLiteral)b.rightExpression;
			assertEquals(2, right.value);
			assertEquals(OP_PLUS, b.op);
		}
		
		@Test
		public void testDeclaration() throws LexicalException, SyntaxException {
			String input = "int b, x, y;";
			PLPParser parser = makeParser(input);
			Declaration d = parser.declaration();  //call declaration here instead of parse
			show(d);	
			assertEquals(VariableListDeclaration.class, d.getClass());
			VariableListDeclaration b = (VariableListDeclaration)d;
			assertEquals("b",b.names.get(0));
			assertEquals("x",b.names.get(1));
			assertEquals("y",b.names.get(2));	
		}
		@Test
		public void testStatement() throws LexicalException, SyntaxException {
			String input = "if ( a==100 ) { print (\"Value of a is 100\");  }";
			PLPParser parser = makeParser(input);
			Statement s = parser.statement();  //call statement here instead of parse
			show(s);	
			assertEquals(IfStatement.class, s.getClass());
			IfStatement b = (IfStatement)s;
			assertEquals(ExpressionBinary.class, b.condition.getClass());
			assertEquals(Block.class, b.block.getClass());
		}
		
		@Test
		public void testDeclaration1() throws LexicalException, SyntaxException {
			String input = "string e = \"Hello, World!\";";
			PLPParser parser = makeParser(input);
			Declaration d = parser.declaration();  //call expression here instead of parse
			show(d);	
			assertEquals(VariableDeclaration.class, d.getClass());
			VariableDeclaration b = (VariableDeclaration)d;
			assertEquals(ExpressionStringLiteral.class, b.expression.getClass());
		}
		
		@Test
		public void testDeclaration_StringLiteral() throws LexicalException, SyntaxException {
			String input = "string s = \"test\"";
			PLPParser parser = makeParser(input);
			Declaration d = parser.declaration();  
			show(d);	
			assertEquals(VariableDeclaration.class, d.getClass());
			VariableDeclaration b = (VariableDeclaration)d;
			assertEquals(ExpressionStringLiteral.class, b.expression.getClass());
			ExpressionStringLiteral s = (ExpressionStringLiteral)b.expression;
			assertEquals(s.text, "test");
			
		}
		
		@Test
		public void testDeclaration_CharLiteral() throws LexicalException, SyntaxException {
			String input = "char x = 'a'";
			PLPParser parser = makeParser(input);
			Declaration d = parser.declaration();  
			show(d);	
			assertEquals(VariableDeclaration.class, d.getClass());
			VariableDeclaration b = (VariableDeclaration)d;
			assertEquals(ExpressionCharLiteral.class, b.expression.getClass());
			ExpressionCharLiteral s = (ExpressionCharLiteral)b.expression;
			assertEquals(s.text, 'a');
			
		}
		
}
