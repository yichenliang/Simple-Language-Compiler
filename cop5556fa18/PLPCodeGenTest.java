// Yichen Liang    p6   Nov 23   11:59pm
package cop5556fa18;

import static org.junit.Assert.assertEquals;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPCodeGenUtils.DynamicClassLoader;

public class PLPCodeGenTest {	
	//determines whether show prints anything
		static boolean doPrint = true;
		
		static void show(Object s) {
			if (doPrint) {
				System.out.println(s);
			}
		}

		//determines whether a classfile is created
		static boolean doCreateFile = false;
		
		@Rule
		public ExpectedException thrown = ExpectedException.none();
		

		//values passed to CodeGenerator constructor to control grading and debugging output
		private boolean devel = true; //if true, print devel output
		private boolean grade = true; //if true, print grade output
		
		/**
		 * Generates bytecode for given input.
		 * Throws exceptions for Lexical, Syntax, and Type checking errors
		 * 
		 * @param input   String containing source code
		 * @return        Generated bytecode
		 * @throws Exception
		 */
		byte[] genCode(String input) throws Exception {
			
			//scan, parse, and type check
			PLPScanner scanner = new PLPScanner(input);
			show(input);
			scanner.scan();
			PLPParser parser = new PLPParser(scanner);
			Program program = parser.parse();
			PLPTypeChecker v = new PLPTypeChecker();
			program.visit(v, null);
//			show(program);  //It may be useful useful to show this here if code generation fails

			//generate code
			PLPCodeGen cv = new PLPCodeGen(null, devel, grade);
			byte[] bytecode = (byte[]) program.visit(cv, null);
			show(program); //doing it here shows the values filled in during code gen
			//display the generated bytecode
			show(PLPCodeGenUtils.bytecodeToString(bytecode));
			
			//write byte code to file 
			if (doCreateFile) {
				String name = ((Program) program).name;
				String classFileName = "bin/" + name + ".class";
				OutputStream output = new FileOutputStream(classFileName);
				output.write(bytecode);
				output.close();
				System.out.println("wrote classfile to " + classFileName);
			}
			
			//return generated classfile as byte array
			return bytecode;
		}
		
		/**
		 * Run main method in given class
		 * 
		 * @param className    
		 * @param bytecode    
		 * @param commandLineArgs  String array containing command line arguments, empty array if none
		 * @throws + 
		 * @throws Throwable 
		 */
		void runCode(String className, byte[] bytecode) throws Exception  {
			PLPRuntimeLog.initLog(); //initialize log used for grading.
			DynamicClassLoader loader = new DynamicClassLoader(Thread.currentThread().getContextClassLoader());
			Class<?> testClass = loader.define(className, bytecode);
			String[] commandLineArgs = {};
			@SuppressWarnings("rawtypes")
			Class[] argTypes = {commandLineArgs.getClass()};
			Method m = testClass.getMethod("main", argTypes );
			show("Output from " + m + ":");  //print name of method to be executed
			Object passedArgs[] = {commandLineArgs};//create array containing params, in this case a single array.
			//show(passedArgs);
			try {
			m.invoke(null, passedArgs);	
			}
			catch (Exception e) {
				Throwable cause = e.getCause();
				if (cause instanceof Exception) {
					Exception ec = (Exception) e.getCause();
					throw ec;
				}
				throw  e;
			}
		}
		
		/**
		 * Since we are not doing any optimization, the compiler will 
		 * still create a class with a main method and the JUnit test will
		 * execute it.  
		 * 
		 * The only thing it will do is append the "entering main" and "leaving main" messages to the log.
		 * 
		 * @throws Exception
		 */
		@Test
		public void emptyProg() throws Exception {
			String prog = "emptyProg";	
			String input = prog + "{}";
			byte[] bytecode = genCode(input);
			runCode(prog, bytecode);
			show("Log:\n "+PLPRuntimeLog.globalLog);
			assertEquals("entering main;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void integerLit() throws Exception {
			String prog = "intgegerLit";
			String input = prog + "{print 4;} ";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;4;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test3() throws Exception {
			String prog = "pro";
			String input = prog + "{int b, c; b=3; print b;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;3;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test4() throws Exception {
			String prog = "pro";
			String input = prog + "{print 1+2;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;3;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test5() throws Exception {
			String prog = "pro";
			String input = prog + "{int a, b, c; b = 3; c = 4;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test6() throws Exception {
			String prog = "pro";
			String input = prog + "{int a = 4;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test7() throws Exception {
			String prog = "pro";
			String input = prog + "{int a = 4 / 2; print a;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;2;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test8() throws Exception {
			String prog = "pro";
			String input = prog + "{int a = 3; if(a==3) { a = 5; print a;};}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;5;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test9() throws Exception {
			String prog = "pro";
			String input = prog + "{string a = \"it is a string\"; print a;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;it is a string;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test10() throws Exception {
			String prog = "pro";
			String input = prog + "{int a = 3; if(a==3) {sleep a;};}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test11() throws Exception {
			String prog = "pro";
			String input = prog + "{float a = 3.6 + 2; print a;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;5.6;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test12() throws Exception {
			String prog = "pro";
			String input = prog + "{boolean a = 3 >= 4; print a;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;false;leaving main;",PLPRuntimeLog.globalLog.toString());
		}

		@Test
		public void test13() throws Exception {
			String prog = "pro";
			String input = prog + "{float a = +3.5; boolean b = !true; print b;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;false;leaving main;",PLPRuntimeLog.globalLog.toString());
		}

		@Test
		public void test14() throws Exception {
			String prog = "pro";
			String input = prog + "{int a, b, c; a = 3; float d = float(a); if(a==3) {sleep a; b = int(3.33333);}; print d;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;3.0;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test15() throws Exception {
			String prog = "pro";
			String input = prog + "{string a; a = \"abc\"; string b = \"abc\"; print a; print b;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;abc;abc;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test16() throws Exception {
			String prog = "pro";
			String input = prog + "{int a = 3; float b = 3.0; float c = a + b; print c;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;6.0;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test17() throws Exception {
			String prog = "pro";
			String input = prog + "{float a; a = 3==3 ? float(3) : 3.6 + 2; print a;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;3.0;leaving main;",PLPRuntimeLog.globalLog.toString());
		}

		@Test
		public void test18() throws Exception {
			String prog = "pro";
			String input = prog + "{string a, b, c; b = \"abc\"; c = \"def\"; print b; print c;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;abc;def;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test19() throws Exception {
			String prog = "pro";
			String input = prog + "{char a = 'a'; print a;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;a;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test20() throws Exception {
			String prog = "pro";
			String input = prog + "{int a = 3; int b = 4; int c= a + b;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test21() throws Exception {
			String prog = "pro";
			String input = prog + "{string a; string b = \"abc\"; string c = \"def\"; a = b + c; print a;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;abcdef;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test22() throws Exception {
			String prog = "pro";
			String input = prog + "{int a = 3; int b = 3; float c = 0.0; if(a == b){float d = sin (c); print d;};}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;0.0;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test23() throws Exception {
			String prog = "pro";
			String input = prog + "{int a = 3; int b = -a; print b;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;-3;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test24() throws Exception {
			String prog = "pro";
			String input = prog + "{float a = 3.0; float b = -a; print b;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;-3.0;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test25() throws Exception {
			String prog = "pro";
			String input = prog + "{float a = 3.0; float b = -a; print b;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;-3.0;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test26() throws Exception {
			String prog = "pro";
			String input = prog + "{char a, b, c; c = 'a'; print c;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;a;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test27() throws Exception {
			String prog = "pro";
			String input = prog + "{int x; x = 0; while ( x <4 ) {char a; a = 'a'; print a; x = x + 1;}; print x;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;a;a;a;a;4;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test28() throws Exception {
			String prog = "pro";
			String input = prog + "{print 9 ** 4;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;6561;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test29() throws Exception {
			String prog = "pro";
			String input = prog + "{print 2.2 != 2.2;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;false;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		//TODO
		@Test
		public void test30() throws Exception {
			String prog = "pro";
			String input = prog + "{print 2.2==2.2; print 2.2 != 2.1; print 2.1>3.1; print 2.1<3.1; print 2.1>=2.1; print 2.1<=3.1;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;true;true;false;true;true;true;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test31() throws Exception {
			String prog = "pro";
			String input = prog + "{print true==true; print true != false; print true>false; print true<false; print true>=true; print true<=false;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;true;true;true;false;true;false;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test32() throws Exception {
			String prog = "pro";
			String input = prog + "{char a = 'b'; print a;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;b;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		@Test
		public void test33() throws Exception {
			String prog = "pro";
			String input = prog + "{string x; x = \"abcc\"; print x;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;abcc;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
		
		//print 2.2==2.2; print 2.2 != 2.1; print 2.1>3.1; print 2.1<3.1; print 2.1>=2.1; print 2.1<=3.1;
		//      true;           true;             false;         true;          true;           true;
		@Test
		public void test34() throws Exception {
			String prog = "pro";
			String input = prog + "{print 2.1>=2.2;}";	
			byte[] bytecode = genCode(input);		
			runCode(prog, bytecode);	
			show("Log:\n"+PLPRuntimeLog.globalLog);
			assertEquals("entering main;false;leaving main;",PLPRuntimeLog.globalLog.toString());
		}
	
}