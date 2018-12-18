// Yichen Liang    p4   Oct 22   11:59pm

package cop5556fa18;

import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPScanner;

import java.util.ArrayList;
import java.util.List;

//import cop5556fa18.PLPAST.AssignmentStatement;
//import cop5556fa18.PLPAST.Block;
//import cop5556fa18.PLPAST.Declaration;
//import cop5556fa18.PLPAST.Expression;
//import cop5556fa18.PLPAST.ExpressionBinary;
//import cop5556fa18.PLPAST.ExpressionConditional;
//import cop5556fa18.PLPAST.ExpressionFloatLiteral;
//import cop5556fa18.PLPAST.ExpressionIdentifier;
//import cop5556fa18.PLPAST.ExpressionIntegerLiteral;
//import cop5556fa18.PLPAST.ExpressionStringLiteral;
//import cop5556fa18.PLPAST.ExpressionBooleanLiteral;
//import cop5556fa18.PLPAST.ExpressionCharLiteral;
//import cop5556fa18.PLPAST.ExpressionUnary;
//import cop5556fa18.PLPAST.FunctionWithArg;
//import cop5556fa18.PLPAST.IfStatement;
//import cop5556fa18.PLPAST.PLPASTNode;
//import cop5556fa18.PLPAST.PrintStatement;
//import cop5556fa18.PLPAST.Program;
//import cop5556fa18.PLPAST.SleepStatement;
//import cop5556fa18.PLPAST.Statement;
//import cop5556fa18.PLPAST.VariableDeclaration;
//import cop5556fa18.PLPAST.VariableListDeclaration;
//import cop5556fa18.PLPAST.WhileStatement;
import cop5556fa18.PLPAST.*;

public class PLPParser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}
	
	PLPScanner scanner;
	Token t;

	PLPParser(PLPScanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}
	

	public Program parse() throws SyntaxException {
		Program p = program();
		//match(Kind.EOF);
		matchEOF();
		return p;
	}
	
	
	//Program -> Identifier Block
	//Program -> IDENTIFIER Block 
	public Program program() throws SyntaxException {
		Token firstToken = t;
		String name = match(Kind.IDENTIFIER).getText();
		Block block = block();
		Program p = new Program(firstToken, name, block);
		return p;
	}
	
	
	Kind[] firstDec = { Kind.KW_int, Kind.KW_boolean, Kind.KW_float, Kind.KW_char, Kind.KW_string /* Complete this */};  
	Kind[] firstStatement = { Kind.KW_if, Kind.IDENTIFIER, Kind.KW_sleep, Kind.KW_print, Kind.KW_while/* Complete this */};

	// Block ->  { (  (Declaration | Statement) ; )* }
	// Block -> ( Declaration | Statement )* 
	public Block block() throws SyntaxException {
		Token firstToken = t;
		List<PLPASTNode> list = new ArrayList<>();
		match(Kind.LBRACE);
		while (checkKind(firstDec) | checkKind(firstStatement)) {
	     if (checkKind(firstDec)) {
			Declaration d = declaration();
			list.add(d);
		} else if (checkKind(firstStatement)) {
			Statement s = statement();
			list.add(s);
		}
			match(Kind.SEMI);
		}
		match(Kind.RBRACE);
        return new Block(firstToken, list);
	}
	
	// Declaration -> Type Identifier ( = Expression | e ) | Type IDENTIFIERLIST
	// IDENTIFIERLIST -> Identifier (, Identifier)*
	
	// Declaration -> Type Identifier ( = Expression | e ) | Type Identifier (, Identifier)*
	
	// P4: Declaration -> VariableDeclaration | VariableListDeclaration
	
	// VariableDeclaration -> Type IDENTIFIER ( e | Expression )
	// VariableListDeclaration -> Type IDENTIFIER  IDENTIFIER +
	public Declaration declaration() throws SyntaxException {
		Token firstToken = t;
		Kind type = t.kind;
		String name;
		String n;
		List<String> names = new ArrayList<>();;
		Expression expression;
		type();
		name = match(Kind.IDENTIFIER).getText();
		names.add(name);
		if(checkKind(Kind.OP_ASSIGN)) {
			match(Kind.OP_ASSIGN);
			expression = expression();
			return new VariableDeclaration(firstToken,type,name,expression);
		}
		else if (checkKind(Kind.COMMA)) {
			while(checkKind(Kind.COMMA)) {
				match(Kind.COMMA);
				n = match(Kind.IDENTIFIER).getText();
				names.add(n);
			}
			return new VariableListDeclaration(firstToken, type, names);
		}
		else if (checkKind(Kind.SEMI)) {
			expression = null;
			return new VariableDeclaration(firstToken,type,name,expression);
		}
		else {
			error("Invalid Declaration");
		}
		throw new UnsupportedOperationException();
	}
	
	//Type -> int | float | boolean | char | string     
	public void type() throws SyntaxException {
		switch(t.kind) {
			case KW_int: {
				match(Kind.KW_int);
			}
			break;
			case KW_float: {
				match(Kind.KW_float);
			}
			break;
			case KW_boolean: {
				match(Kind.KW_boolean);
			}
			break;
			case KW_char: {
				match(Kind.KW_char);
			}
			break;
			case KW_string: {
				match(Kind.KW_string);
			}
			break;
			default:
			// error message
				error("Invalid Type");
		}
		return;
	}
	
    //Expression -> OrExpression ? Expression : Expression | OrExpression
	//ExpressionConditional -> Expression Expression Expression 
    public Expression expression() throws SyntaxException {
		Token firstToken = t;
    	Expression condition = orExpression();
		if(checkKind(Kind.OP_QUESTION)) {
			match(Kind.OP_QUESTION);
			Expression trueExpression = expression();
			match(Kind.OP_COLON);
			Expression falseExpression = expression();
			return new ExpressionConditional(firstToken, condition, trueExpression, falseExpression);
		}
		else {
		  return condition;
		 }
	}
	
    //OrExpression -> AndExpression ( | AndExpression )*
    // ExpressionBinary -> Expression op Expression 
    public Expression orExpression() throws SyntaxException {
    	Token firstToken = t;
    	Expression el = andExpression(); 
    	Kind op = null;
    	while(checkKind(Kind.OP_OR)) {
    		op = match(Kind.OP_OR).kind;
    		Expression er = andExpression();  
    		el = new ExpressionBinary(firstToken, el, op, er); 
    	}
    	return el; 
	}
    
    // AndExpression -> EqExpression ( & EqExpression )*
    // ExpressionBinary -> Expression op Expression 
    public Expression andExpression() throws SyntaxException {
    	Token firstToken = t;
    	Kind op = null;
    	Expression el = eqExpression();
    	while(checkKind(Kind.OP_AND)) {
    		op = match(Kind.OP_AND).kind;
    		Expression er = eqExpression();
    		el = new ExpressionBinary(firstToken, el, op, er); 
    	}
    	return el;
	}
    
    //EqExpression -> RelExpression ( ( == | != ) RelExpression )*
    // ExpressionBinary -> Expression op Expression
    public Expression eqExpression() throws SyntaxException {
    	Token firstToken = t;
    	Expression el = relExpression();
    	Kind op = null;
    	while(checkKind(Kind.OP_EQ) | checkKind(Kind.OP_NEQ)) {
    		if(checkKind(Kind.OP_EQ)) {
    			op = match(Kind.OP_EQ).kind;
    		}
    		else {
    			op = match(Kind.OP_NEQ).kind;
    		}
    		Expression er = relExpression();
    		el = new ExpressionBinary(firstToken, el, op, er); 
    	}
    	return el;
    }
    
    // RelExpression -> AddExpression ( ( < | > | <= | >= ) AddExpression )*
    // ExpressionBinary -> Expression op Expression 
    public Expression relExpression() throws SyntaxException {
    	Token firstToken = t;
    	Expression el = addExpression();
    	Kind op = null;
    	while(checkKind(Kind.OP_GE) | checkKind(Kind.OP_LE) | checkKind(Kind.OP_GT) | checkKind(Kind.OP_LT)) {
    		switch(t.kind) {
    			case OP_GE: {
    				op = match(Kind.OP_GE).kind;
    			}
    			break;
    			case OP_LE: {
    				op = match(Kind.OP_LE).kind;
    			}
    			break;
    			case OP_GT: {
    				op = match(Kind.OP_GT).kind;
    			}
    			break;
    			case OP_LT: {
    				op = match(Kind.OP_LT).kind;
    			}
    			break;
    			default:
    				// error message
    				error("Invalid RelExpression");
    		}
    		Expression er = addExpression();
    		el = new ExpressionBinary(firstToken, el, op, er);
    	}
    	return el; 
    }
    
    //AddExpression -> MultExpression ( ( + | - ) MultExpression )*
    // ExpressionBinary -> Expression op Expression 
    public Expression addExpression() throws SyntaxException {
    	Token firstToken = t;
    	Expression el = multExpression();
    	Kind op = null;
    	while(checkKind(Kind.OP_PLUS) | checkKind(Kind.OP_MINUS)) {
    		if(checkKind(Kind.OP_PLUS)) {
    			op = match(Kind.OP_PLUS).kind;
    		}
    		else if(checkKind(Kind.OP_MINUS)) {
    			op = match(Kind.OP_MINUS).kind;
    		}
    		Expression er = multExpression();
    		el = new ExpressionBinary(firstToken, el, op, er);
    	}
    	return el;
    }
    
    // MultExpression -> PowerExpression ( ( * | / | % ) PowerExpression )*
    // ExpressionBinary -> Expression op Expression 
    public Expression multExpression() throws SyntaxException {
    	Token firstToken = t;
    	Expression el = powerExpression();
    	Kind op = null;
    	while(checkKind(Kind.OP_TIMES) | checkKind(Kind.OP_DIV) | checkKind(Kind.OP_MOD)) {
    		if(checkKind(Kind.OP_TIMES)) {
    			op = match(Kind.OP_TIMES).kind;
    		}
    		else if(checkKind(Kind.OP_DIV)) {
    			op = match(Kind.OP_DIV).kind;
    		}
    		else if(checkKind(Kind.OP_MOD)) {
    			op = match(Kind.OP_MOD).kind;
    		}
    		Expression er = powerExpression();
    		el = new ExpressionBinary(firstToken, el, op, er);
    	}
    	return el;
    }
    
    //PowerExpression -> UnaryExpression ( ** PowerExpression |  e )
    // ExpressionBinary -> Expression op Expression 
    public Expression powerExpression() throws SyntaxException {
    	Token firstToken = t;
    	Expression el = unaryExpression();
    	Kind op = null;
    	if(checkKind(Kind.OP_POWER)) {
    		op = match(Kind.OP_POWER).kind;
    		Expression er = powerExpression();
    		el = new ExpressionBinary(firstToken, el, op, er);
    	}
    	return el;
    }
    
    //UnaryExpression -> + UnaryExpression | - UnaryExpression | ! UnaryExpression | Primary
    // ExpressionUnary -> Op Expression 
    public Expression unaryExpression() throws SyntaxException {
    	Token firstToken = t;
    	Kind op = null;
    	switch(t.kind) {
    		case OP_PLUS:{
    			op = match(Kind.OP_PLUS).kind;
    			Expression e = unaryExpression();
    			return new ExpressionUnary(firstToken, op, e);
    		}
    		//break;
    		case OP_MINUS:{
    			op = match(Kind.OP_MINUS).kind;
    			Expression e = unaryExpression();
    			return new ExpressionUnary(firstToken, op, e);
    		}
    		//break;
    		case OP_EXCLAMATION:{
    			op = match(Kind.OP_EXCLAMATION).kind;
    			Expression e = unaryExpression();
    			return new ExpressionUnary(firstToken, op, e);
    		}
    		//break;
    		default:
    			Expression e = primary(); 
    			return e;
    	}
    }
    
    // Primary -> INTEGER_LITERAL | BOOLEAN_LITERAL | FLOAT_LITERAL | CHAR_LITERAL | STRING_LITERAL | ( Expression ) | IDENTIFIER | Function
    public Expression primary() throws SyntaxException {
    	Token firstToken = t;
    	switch(t.kind) {
    	    //Primary -> INTEGER_LITERAL
    	    //ExpressionIntegerLiteral 
    		case INTEGER_LITERAL:{
    		   Token val =  match(Kind.INTEGER_LITERAL);
    		   int value = val.intVal();
    		   return new ExpressionIntegerLiteral(firstToken, value);
    		}
    		//break;
    		
    		//Primary -> BOOLEAN_LITERAL 
    		//ExpressionBooleanLiteral 
    		case BOOLEAN_LITERAL:{
    			Token val = match(Kind.BOOLEAN_LITERAL);
    			boolean value = val.booleanVal();
    			return new ExpressionBooleanLiteral(firstToken, value);
    		}
    		//break;
    		
    		//Primary -> FLOAT_LITERAL 
    		//ExpressionFloatLiteral 
    		case FLOAT_LITERAL:{
    			Token val = match(Kind.FLOAT_LITERAL);
    			float value = val.floatVal();
    			return new ExpressionFloatLiteral(firstToken, value);
    		}
    		//break;
    		
    		//Primary -> CHAR_LITERAL
    		//ExpressionCharLiteral
    		case CHAR_LITERAL:{
    			Token val = match(Kind.CHAR_LITERAL);
    			char text = val.charVal();
    			return new ExpressionCharLiteral(firstToken, text);
    		}
    		//break;
    		
    		//Primary -> STRING_LITERAL
    		//ExpressionStringLiteral
    		case STRING_LITERAL:{
    			Token val = match(Kind.STRING_LITERAL);
    			String text = val.stringVal();
    			return new ExpressionStringLiteral(firstToken, text);
    		}
    		//break;
    		
    		//Primary -> IDENTIFIER 
    		//ExpressionIdent 
    		case IDENTIFIER:{
    			Token val = match(Kind.IDENTIFIER);
    			String name = val.getText();
    			return new ExpressionIdentifier(firstToken, name);
    		}
    		//break;
    		
    		//Primary -> ( Expression )  
    		case LPAREN:{
    			match(Kind.LPAREN);
    			Expression e = expression();
    			match(Kind.RPAREN);
    			return e;
    		}
    		//break;
    		
    		//Primary ->  Function 
    		default:
    			Expression e = function();	
    			return e;
    	}
    }
    
    //Function -> FunctionName ( Expression )
    // FunctionWithArg -> FunctionName Expression 
    public Expression function() throws SyntaxException {
    	Token firstToken = t;
    	Kind functionName = functionName();
    	match(Kind.LPAREN);
    	Expression e = expression();
    	match(Kind.RPAREN);	
    	return new FunctionWithArg(firstToken, functionName, e);
    }
    
    //FunctionName -> sin | cos | atan | abs | log | int | float
    public Kind functionName() throws SyntaxException {
    	Kind name = null;
    	switch(t.kind) {	
    		case KW_sin:{
    			name = match(Kind.KW_sin).kind;
    			//return name;
    		}
    		break;
    		case KW_cos:{
    			name = match(Kind.KW_cos).kind;
    			//return name;
    		}
    		break;
    		case KW_atan:{
    			name = match(Kind.KW_atan).kind;
    			//return name;
    		}
    		break;
    		case KW_log:{
    			name = match(Kind.KW_log).kind;
    			//return name;
    		}
    		break;
    		case KW_abs:{
    			name = match(Kind.KW_abs).kind;
    			//return name;
    		}
    		break;
    		case KW_int:{
    			name = match(Kind.KW_int).kind;
    			//return name;
    		}
    		break;
    		case KW_float:{
    			name = match(Kind.KW_float).kind;
    			//return name;
    		}
    		break;
    		default:
    			error("Invalid FunctionName");
    	}
    	return name;
    }
    
	//Kind[] firstStatement = { Kind.KW_if, Kind.IDENTIFIER, Kind.KW_sleep, Kind.KW_print, Kind.KW_while};
    public Statement statement() throws SyntaxException {
    	Token firstToken = t;
    	switch(t.kind) {
    		// IfStatement -> if ( Expression ) Block
    	    // IfStatement -> Expression Block 
    		case KW_if:{
    			match(Kind.KW_if);
    			match(Kind.LPAREN);
    			Expression condition = expression();
    			match(Kind.RPAREN);
    			Block block = block();
    			return new IfStatement(firstToken, condition, block);
    		}
    		//break;
    		
    		//AssignmentStatement -> Identifier = Expression
    		//AssignmentStatement -> IDENTIFIER Expression 
    		// TODO one change in P5 (from identifier to )
    		// AssignmentStatement -> LHS Expression
    		case IDENTIFIER:{
    			String identifier;
    			identifier = match(Kind.IDENTIFIER).getText();
    			match(Kind.OP_ASSIGN);
    			LHS lhs;
    			lhs = new LHS(firstToken, identifier);
    			Expression expression = expression();
    			//return new AssignmentStatement(firstToken, identifier, expression);
    			//AssignmentStatement(Token firstToken, LHS lhs, Expression expression)
    			return new AssignmentStatement(firstToken, lhs, expression);
    		}
    		//break;
    		
    		//SleepStatement -> sleep Expression
    		//SleepStatement -> Expression
    		case KW_sleep:{
    			match(Kind.KW_sleep);
    			Expression time = expression();
    			return new SleepStatement(firstToken, time);
    		}
    		//break;
    		
    		//PrintStatement -> print Expression
    		//PrintStatement -> Expression
    		case KW_print:{
    			match(Kind.KW_print);
    			Expression expression = expression();
    			return new PrintStatement(firstToken, expression);
    		}
    		//break;
    		
    		// WhileStatement -> while ( Expression ) Block
    		// WhileStatement -> Expression Block
    		case KW_while:{
    			match(Kind.KW_while);
    			match(Kind.LPAREN);
    			Expression condition = expression();
    			match(Kind.RPAREN);
    			Block b = block();
    			return new WhileStatement(firstToken, condition, b);
    		}
    		//break;
    		default:  // some error case
    			error("Invalid Statement!");
    	}
		throw new UnsupportedOperationException();
	}
	

	protected boolean checkKind(Kind kind) {
		return t.kind == kind;       //Token t;
	}

	protected boolean checkKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}
	
	private Token matchEOF() throws SyntaxException {
		if (checkKind(Kind.EOF)) {
			return t;
		}
		throw new SyntaxException(t,"Syntax Error"); //TODO  give a better error message!
	}
	
	/**
	 * @param kind
	 * @return 
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {   // TODO !!! need to fix
		Token tmp = t;
		if (checkKind(kind)) {    
			t = scanner.nextToken();
			return tmp;
		}
		//TODO  give a better error message!
		String m = (t.line() + 1) + ":" + (t.posInLine() + 1) + "Syntax Error";
		throw new SyntaxException(t,m);
	}
	
	
	private void error(String errorMessage) throws SyntaxException {
		String m = (t.line() + 1) + ":" + (t.posInLine() + 1) + errorMessage;
		throw new SyntaxException(t, m);
	}

}
