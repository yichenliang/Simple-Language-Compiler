// Yichen Liang    p2   Sep 19 11:59pm
/**
 * JUunit tests for the Scanner
 */

package cop5556fa18;

import static cop5556fa18.PLPScanner.Kind.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa18.PLPScanner.LexicalException;
import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPScanner;



public class PLPScannerTest {
	
	//set Junit to be able to catch exceptions
		@Rule
		public ExpectedException thrown = ExpectedException.none();

		
		//To make it easy to print objects and turn this output on and off
		static boolean doPrint = true;
		private void show(Object input) {
			if (doPrint) {
				System.out.println(input.toString());
			}
		}

		/**
		 *Retrieves the next token and checks that it is an EOF token. 
		 *Also checks that this was the last token.
		 *
		 * @param scanner
		 * @return the Token that was retrieved
		 */
		
		Token checkNextIsEOF(PLPScanner scanner) {
			PLPScanner.Token token = scanner.nextToken();
			assertEquals(PLPScanner.Kind.EOF, token.kind);
			assertFalse(scanner.hasTokens());
			return token;
		}


		/**
		 * Retrieves the next token and checks that its kind, position, length, line, and position in line
		 * match the given parameters.
		 * 
		 * @param scanner
		 * @param kind
		 * @param pos
		 * @param length
		 * @param line
		 * @param pos_in_line
		 * @return  the Token that was retrieved
		 */
		Token checkNext(PLPScanner scanner, PLPScanner.Kind kind, int pos, int length, int line, int pos_in_line) {
			Token t = scanner.nextToken();
			assertEquals(kind, t.kind);
			assertEquals(pos, t.pos);
			assertEquals(length, t.length);
			assertEquals(line, t.line());
			assertEquals(pos_in_line, t.posInLine());
			return t;
		}

		/**
		 * Retrieves the next token and checks that its kind and length match the given
		 * parameters.  The position, line, and position in line are ignored.
		 * 
		 * @param scanner
		 * @param kind
		 * @param length
		 * @return  the Token that was retrieved
		 */
		Token checkNext(PLPScanner scanner, PLPScanner.Kind kind, int length) {
			Token t = scanner.nextToken();
			assertEquals(kind, t.kind);
			assertEquals(length, t.length);
			return t;
		}
		


		/**
		 * Simple test case with an empty program.  The only Token will be the EOF Token.
		 *   
		 * @throws LexicalException
		 */
		@Test
		public void testEmpty() throws LexicalException {
			String input = "";  //The input is the empty string.  This is legal
			show(input);        //Display the input 
			PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
			checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
		}

		
		/**
		 * This example shows how to test that your scanner is behaving when the
		 * input is illegal.  In this case, we are giving it an illegal character '~' in position 2
		 * 
		 * The example shows catching the exception that is thrown by the scanner,
		 * looking at it, and checking its contents before rethrowing it.  If caught
		 * but not rethrown, then JUnit won't get the exception and the test will fail.  
		 * 
		 * The test will work without putting the try-catch block around 
		 * new Scanner(input).scan(); but then you won't be able to check 
		 * or display the thrown exception.
		 * 
		 * @throws LexicalException
		 */
		@Test
		public void failIllegalChar() throws LexicalException {
			//String input = ";;~";
			String input = "012AD$_012ad";
			show(input);
			thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
			try {
				new PLPScanner(input).scan();
			} catch (LexicalException e) {  //Catch the exception
				show(e);                    //Display it
				//assertEquals(2,e.getPos()); //Check that it occurred in the expected position
				throw e;                    //Rethrow exception so JUnit will see it
			}
		}
		
//		/**
//		 * Using the two previous functions as a template, you can implement other JUnit test cases.
//		 * /
		
		
		// my own test case 
	    @Test
	    public void testinput01() throws LexicalException{
	    	String input = "boolean a;";
	    	//String input = "abc\n12";
	    	PLPScanner scanner = new PLPScanner(input).scan();
	    	PLPScanner.Token token0 = scanner.nextToken();
	        assertEquals(KW_boolean, token0.kind);
	        assertEquals(0, token0.pos);
	        PLPScanner.Token token1 = scanner.nextToken();
	        assertEquals(IDENTIFIER, token1.kind);
	        assertEquals(8, token1.pos);
	    }
	    
	    @Test
	    public void testinput02() throws LexicalException{
	    	String input = "t = ((4-2)*5.6)/3+2;";
	    	PLPScanner scanner = new PLPScanner(input).scan();
	    	PLPScanner.Token token0 = scanner.nextToken();  // _identifier
	        assertEquals(IDENTIFIER, token0.kind);
	        assertEquals(0, token0.pos);
	        PLPScanner.Token token1 = scanner.nextToken();  // =
	        assertEquals(OP_ASSIGN, token1.kind);
	        assertEquals(2, token1.pos);
	        PLPScanner.Token token2 = scanner.nextToken();  // (
	        assertEquals(LPAREN, token2.kind);
	        assertEquals(4, token2.pos);
	        PLPScanner.Token token3 = scanner.nextToken();  // (
	        assertEquals(LPAREN, token3.kind);
	        assertEquals(5, token3.pos);
	        PLPScanner.Token token4 = scanner.nextToken();  // 4 
	        assertEquals(INTEGER_LITERAL, token4.kind);
	        assertEquals(6, token4.pos);
	        PLPScanner.Token token6 = scanner.nextToken();  // -
	        assertEquals(OP_MINUS, token6.kind);
	        assertEquals(7, token6.pos);
	        
	        
	    }
	    
	    @Test
	    public void testComment() throws LexicalException{
	    	String input = "%{ it is comment %} _identifier = 3.3 %%}";
	    	PLPScanner scanner = new PLPScanner(input).scan();
	    	PLPScanner.Token token0 = scanner.nextToken();  // _identifier
	        assertEquals(IDENTIFIER, token0.kind);
	        assertEquals(20, token0.pos);
	        PLPScanner.Token token1 = scanner.nextToken();  // =
	        assertEquals(OP_ASSIGN, token1.kind);
	        assertEquals(32, token1.pos);
	        PLPScanner.Token token2 = scanner.nextToken();  // 3.3
	        assertEquals(FLOAT_LITERAL, token2.kind);
	        assertEquals(34, token2.pos);
	        PLPScanner.Token token3 = scanner.nextToken();  // %
	        assertEquals(OP_MOD, token3.kind);
	        assertEquals(38, token3.pos);
	        PLPScanner.Token token4 = scanner.nextToken();  // %
	        assertEquals(OP_MOD, token4.kind);
	        assertEquals(39, token4.pos);
	        PLPScanner.Token token5 = scanner.nextToken();  // }
	        assertEquals(RBRACE, token5.kind);
	        assertEquals(40, token5.pos);
	    }
	    
	    @Test
	    public void failInString() throws LexicalException {
			//String input = "e = \"Hi, World! ;";
			String input = "12343454636745543441344353456766745456756879800453.123"; 
			//String input = "%{ in comment {% still in comment %}%}";
			show(input);
			thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
			try {
				new PLPScanner(input).scan();
			} catch (LexicalException e) {  //Catch the exception
				show(e);                    //Display it
				//assertEquals(17,e.getPos()); //Check that it occurred in the expected position
				throw e;                    //Rethrow exception so JUnit will see it
			}
		}
	    
	    @Test
	    public void failInIndentifier() throws LexicalException {
//			String input = "__";
			String input = "12343454636745543441344353456766745456756879800453.123";
			show(input);
			thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
			try {
				new PLPScanner(input).scan();
			} catch (LexicalException e) {  //Catch the exception
				show(e);                    //Display it
				//assertEquals(5,e.getPos()); //Check that it occurred in the expected position
				throw e;                    //Rethrow exception so JUnit will see it
			}
		}
	    
	    @Test
		public void showEachToken() throws LexicalException {
//			String input = "if ( a==100 ){\r\n" + 
//					"  print (\"Value of a is 100\" );\r\n" + 
//					"  print( a );\r\n" + 
//					"}";  //The input is the empty string.  This is legal
	    	String input = "0123";
			show(input);        //Display the input 
			PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
		}
	    
//	    @Test
//	    public void testinput_additionalSymbol() throws LexicalException{
//	    	String input = "while?sin:cos|&atan!=_absabs123";
//	    	PLPScanner scanner = new PLPScanner(input).scan();
//	    	PLPScanner.Token token0 = scanner.nextToken();  // while
//	        assertEquals(KW_while, token0.kind);
//	        assertEquals(0, token0.pos);
//	        PLPScanner.Token token1 = scanner.nextToken();  // ?
//	        assertEquals(OP_QUESTION, token1.kind);
//	        assertEquals(5, token1.pos);
//	        PLPScanner.Token token2 = scanner.nextToken();  // sin
//	        assertEquals(KW_sin, token2.kind);
//	        assertEquals(6, token2.pos);
//	        PLPScanner.Token token3 = scanner.nextToken();  // :
//	        assertEquals(OP_COLON, token3.kind);
//	        assertEquals(9, token3.pos);
//	        PLPScanner.Token token4 = scanner.nextToken();  // cos 
//	        assertEquals(KW_cos, token4.kind);
//	        assertEquals(10, token4.pos);
//	        PLPScanner.Token token6 = scanner.nextToken();  // |
//	        assertEquals(OP_OR, token6.kind);
//	        assertEquals(13, token6.pos);
//	        PLPScanner.Token token7 = scanner.nextToken();  // &
//	        assertEquals(OP_AND, token7.kind);
//	        assertEquals(14, token7.pos);
//	        PLPScanner.Token token8 = scanner.nextToken();  // atan
//	        assertEquals(KW_atan, token8.kind);
//	        assertEquals(15, token8.pos);
//	        PLPScanner.Token token9 = scanner.nextToken();  // !=
//	        assertEquals(OP_NEQ, token9.kind);
//	        assertEquals(19, token9.pos);
//	        PLPScanner.Token token10 = scanner.nextToken();  // _abs
//	        assertEquals(IDENTIFIER, token10.kind);
//	        assertEquals(21, token10.pos);
//	        PLPScanner.Token token11 = scanner.nextToken();  // abs
//	        assertEquals(KW_abs, token11.kind);
//	        assertEquals(25, token11.pos);
//	        PLPScanner.Token token12 = scanner.nextToken();  // 123
//	        assertEquals(INTEGER_LITERAL, token12.kind);
//	        assertEquals(31, token12.pos);
//	    }
	    
//	    @Test
//		public void showEachToken_newSymbol() throws LexicalException {
//			//String input = "abswhile?sin:cos|&atan!=_absabs123";  //The input is the empty string.  This is legal
//			String input = "012AD$_012ad"; 
//			show(input);        //Display the input 
//			PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
//			show(scanner);   //Display the Scanner
//		}
		
	    @Test
		public void showEachToken1() throws LexicalException {
//			String input = "if ( a==100 ){\r\n" + 
//					"  print (\"Value of a is 100\" );\r\n" + 
//					"  print( a );\r\n" + 
//					"}";  //The input is the empty string.  This is legal
	    	String input = "%{ this is a comment %} %} %} print(var);";
			show(input);        //Display the input 
			PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
		}
	    
	    @Test
		public void showEachToken2() throws LexicalException {
	    	String input = "string s = \"test\";";
	    	
			show(input);        //Display the input 
			PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
		}
	    
	    
}
