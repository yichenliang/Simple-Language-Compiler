// Yichen Liang    p5   Nov 5 11:59pm

package cop5556fa18;

import java.util.Arrays;
import java.util.List;

import cop5556fa18.PLPAST.AssignmentStatement;
import cop5556fa18.PLPAST.Block;
import cop5556fa18.PLPAST.Declaration;
import cop5556fa18.PLPAST.ExpressionBinary;
import cop5556fa18.PLPAST.ExpressionBooleanLiteral;
import cop5556fa18.PLPAST.ExpressionCharLiteral;
import cop5556fa18.PLPAST.ExpressionConditional;
import cop5556fa18.PLPAST.ExpressionFloatLiteral;
import cop5556fa18.PLPAST.ExpressionIdentifier;
import cop5556fa18.PLPAST.ExpressionIntegerLiteral;
import cop5556fa18.PLPAST.ExpressionStringLiteral;
import cop5556fa18.PLPAST.ExpressionUnary;
import cop5556fa18.PLPAST.FunctionWithArg;
import cop5556fa18.PLPAST.IfStatement;
import cop5556fa18.PLPAST.LHS;
import cop5556fa18.PLPAST.PLPASTNode;
import cop5556fa18.PLPAST.PLPASTVisitor;
import cop5556fa18.PLPAST.PrintStatement;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPAST.SleepStatement;
import cop5556fa18.PLPAST.Statement;
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPAST.VariableListDeclaration;
import cop5556fa18.PLPAST.WhileStatement;
import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPTypes.Type;


public class PLPTypeChecker implements PLPASTVisitor {
	
	PLPTypeChecker() {
	}
	
	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	PLPSymbolTable symbolTable = new PLPSymbolTable();
	
	// Name is only used for naming the output file. 
	// Visit the child block to type check program.
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
			program.block.visit(this, arg);
			return null;
	}
		
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		symbolTable.enterScope();
		List<PLPASTNode> list = block.declarationsAndStatements;
		for (PLPASTNode n : list) {
			if (n instanceof Declaration ) {
				Declaration dec = (Declaration) n;
				dec.visit(this, arg);
			}
			else {
				Statement sta = (Statement) n;
				sta.visit(this, arg);	
			}
		}
		symbolTable.closeScope();
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	// VariableDeclaration -> Type IDENTIFIER ( e | Expression )
	
	public Object visitVariableDeclaration(VariableDeclaration declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		if (symbolTable.duplicate(declaration.name)) {
			String errorMessage = "Duplicate declaration";
			throw new SemanticException(declaration.firstToken, errorMessage);
		}
		
		if(declaration.expression == null) {
			symbolTable.addDec(declaration.name, declaration);
			return null;
		}
		
		Type expressionT = (Type) declaration.expression.visit(this, arg);
		
		if(expressionT == PLPTypes.getType(declaration.type)) {
			symbolTable.addDec(declaration.name, declaration);
			return null;
		}
	
		String message = "Incompatible declaration type.";
		throw new SemanticException(declaration.firstToken, message);
		//throw new UnsupportedOperationException();
	}

	//IDENTIFIERLIST -> Identifier (, Identifier)*	
	//VariableListDeclaration -> Type IDENTIFIER  IDENTIFIER +
	@Override
	public Object visitVariableListDeclaration(VariableListDeclaration declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		for(String n : declaration.names) {
			if(symbolTable.duplicate(n)) {
				String errorMessage = "Duplicate declaration";
				throw new SemanticException(declaration.firstToken, errorMessage);
			}
			symbolTable.addDec(n, declaration);
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionBooleanLiteral.setType(Type.BOOLEAN);
		return expressionBooleanLiteral.getType();
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type leftT = (Type) expressionBinary.leftExpression.visit(this, arg);
		Type rightT = (Type) expressionBinary.rightExpression.visit(this, arg);
		Kind op = expressionBinary.op;
		
		List<Kind> basicOpType = Arrays.asList(Kind.OP_PLUS, Kind.OP_MINUS, Kind.OP_TIMES, Kind.OP_DIV, Kind.OP_MOD,Kind.OP_POWER);
		List<Kind> basicOpTypeFloat = Arrays.asList(Kind.OP_PLUS, Kind.OP_MINUS, Kind.OP_TIMES, Kind.OP_DIV, Kind.OP_POWER);
		List<Kind> logicOpType = Arrays.asList(Kind.OP_AND, Kind.OP_OR);
		List<Kind> CompOpType = Arrays.asList(Kind.OP_EQ, Kind.OP_NEQ, Kind.OP_GT, Kind.OP_GE,Kind.OP_LT, Kind.OP_LE);
		
		//integer integer
		if (leftT == Type.INTEGER && rightT == Type.INTEGER) {  
			if(basicOpType.contains(op) || logicOpType.contains(op)) { // + - * / % **  | &
				expressionBinary.setType(Type.INTEGER);
				return expressionBinary.getType();
			}
			if(CompOpType.contains(op)) {
				expressionBinary.setType(Type.BOOLEAN);
				return expressionBinary.getType();
			}
		}
		
		//float float
		if (leftT == Type.FLOAT && rightT == Type.FLOAT) {
			if(basicOpTypeFloat.contains(op)) {
				expressionBinary.setType(Type.FLOAT);
				return expressionBinary.getType();
			}
			if(CompOpType.contains(op)) {
				expressionBinary.setType(Type.BOOLEAN);
				return expressionBinary.getType();
			}
		}
		
		//boolean boolean
		if (leftT == Type.BOOLEAN && rightT == Type.BOOLEAN) {
			if (logicOpType.contains(op) || CompOpType.contains(op)) {
				expressionBinary.setType(Type.BOOLEAN);
				return expressionBinary.getType();
			}
		}
		
		//string string
		if (leftT == Type.STRING && rightT == Type.STRING) {
			if (op == Kind.OP_PLUS) {
				expressionBinary.setType(Type.STRING);
				return expressionBinary.getType();
			}
		}
		
		// float integer
		if (leftT == Type.FLOAT && rightT == Type.INTEGER) {
			if (basicOpTypeFloat.contains(op)) {
				expressionBinary.setType(Type.FLOAT);
				return expressionBinary.getType();
			}
		}
		
		//integer float
		if (leftT == Type.INTEGER && rightT == Type.FLOAT) {
			if (basicOpTypeFloat.contains(op)) {
				expressionBinary.setType(Type.FLOAT);
				return expressionBinary.getType();
			}
		}
		
		String message = "Incompatible type for expressionBinary: " + expressionBinary.firstToken.getText();
		throw new SemanticException(expressionBinary.firstToken, message);
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type condition  = (Type) expressionConditional.condition.visit(this, arg);
		Type trueExpression  = (Type) expressionConditional.trueExpression.visit(this, arg);
		Type falseExpression  = (Type) expressionConditional.falseExpression.visit(this, arg);
		
		if (condition!= Type.BOOLEAN) {
			String message = "Condition type should be boolean.";
			throw new SemanticException(expressionConditional.firstToken, message);
		}
		
		if (trueExpression != falseExpression) {
			String message = "Expressions don't have the same type to compare.";
			throw new SemanticException(expressionConditional.firstToken, message);
		}
		
		expressionConditional.setType(trueExpression); 
		return expressionConditional.getType();
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionFloatLiteral.setType(Type.FLOAT);
		return expressionFloatLiteral.getType();
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitFunctionWithArg(FunctionWithArg FunctionWithArg, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type eT = (Type) FunctionWithArg.expression.visit(this, arg);
		Kind fN = FunctionWithArg.functionName;
		
		if (eT == Type.INTEGER) {
			if (fN == Kind.KW_abs) {
				FunctionWithArg.setType(Type.INTEGER);
				return FunctionWithArg.getType();
			}
			if (fN == Kind.KW_float) {
				FunctionWithArg.setType(Type.FLOAT);
				return FunctionWithArg.getType();
			}
			if (fN == Kind.KW_int) {
				FunctionWithArg.setType(Type.INTEGER);
				return FunctionWithArg.getType();
			}
		}
		
		if (eT == Type.FLOAT) {
			if (fN == Kind.KW_abs || fN == Kind.KW_sin || fN == Kind.KW_cos || fN == Kind.KW_atan || fN == Kind.KW_log) {
				FunctionWithArg.setType(Type.FLOAT);
				return FunctionWithArg.getType();
			}
			if (fN == Kind.KW_float) {
				FunctionWithArg.setType(Type.FLOAT);
				return FunctionWithArg.getType();
			}
			if (fN == Kind.KW_int) {
				FunctionWithArg.setType(Type.INTEGER);
				return FunctionWithArg.getType();
			}
		}
		
		String message = "Incompatible types for expressionFunctionAppWithExpressionArg";
		throw new SemanticException(FunctionWithArg.firstToken, message);
		//throw new UnsupportedOperationException();
	}

	//Primary -> IDENTIFIER 
	//ExpressionIdent 
	@Override
	public Object visitExpressionIdent(ExpressionIdentifier expressionIdent, Object arg) throws Exception {
		// TODO Auto-generated method stub
//		VariableDeclaration dec = (VariableDeclaration) symbolTable.lookup(expressionIdent.name);
//		VariableListDeclaration decList = (VariableListDeclaration) symbolTable.lookup(expressionIdent.name);
		
		Declaration dec =symbolTable.lookup(expressionIdent.name);
		if (dec != null && dec instanceof VariableDeclaration) {
			VariableDeclaration decV = (VariableDeclaration) dec;
			expressionIdent.setDec(decV);
			expressionIdent.setType(PLPTypes.getType(decV.type));
			return expressionIdent.getType();
		}
		
		if(dec != null && dec instanceof VariableListDeclaration) {
			VariableListDeclaration decList = (VariableListDeclaration) dec;
			expressionIdent.setDec(decList);
			expressionIdent.setType(PLPTypes.getType(decList.type));
			return expressionIdent.getType();
		}
		
		String message = "Missing declaration.";
		throw new SemanticException(expressionIdent.firstToken, message);
	//	throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionIntegerLiteral.setType(Type.INTEGER);
		return expressionIntegerLiteral.getType();
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionStringLiteral(ExpressionStringLiteral expressionStringLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionStringLiteral.setType(Type.STRING);
		return expressionStringLiteral.getType();
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionCharLiteral(ExpressionCharLiteral expressionCharLiteral, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionCharLiteral.setType(Type.CHAR);
		return expressionCharLiteral.getType();
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type LHS = (Type) statementAssign.lhs.visit(this, arg);
		Type e = (Type) statementAssign.expression.visit(this, arg);
		
		if (LHS != e) {
			String message = "LHS and expression should have same type.";
			throw new SemanticException(statementAssign.firstToken, message);
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type e = (Type) ifStatement.condition.visit(this, arg);
		if (e != Type.BOOLEAN) {
			String message = "Expression should has type boolean.";
			throw new SemanticException(ifStatement.firstToken, message);
		}
		ifStatement.block.visit(this, arg);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type e = (Type) whileStatement.condition.visit(this, arg);
		if (e != Type.BOOLEAN) {
			String message = "Expression should has type boolean.";
			throw new SemanticException(whileStatement.firstToken, message);
		}
		whileStatement.b.visit(this, arg);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type e = (Type) printStatement.expression.visit(this, arg);
		if(e == Type.INTEGER || e == Type.BOOLEAN || e == Type.FLOAT || e == Type.CHAR || e == Type.STRING) {
			return null;
		}
		String message = "Expression has invalid type!";
		throw new SemanticException(printStatement.firstToken, message);
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type e = (Type) sleepStatement.time.visit(this, arg);
		if (e != Type.INTEGER) {
			String message = "Expression should has type integer.";
			throw new SemanticException(sleepStatement.firstToken, message);
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type eT = (Type) expressionUnary.expression.visit(this, arg);
		Kind op = expressionUnary.op;
		if (eT == Type.INTEGER && op == Kind.OP_EXCLAMATION) {
			expressionUnary.setType(Type.INTEGER);
			return expressionUnary.getType();
		}
		
		if (eT == Type.BOOLEAN && op == Kind.OP_EXCLAMATION) {
			expressionUnary.setType(Type.BOOLEAN);
			return expressionUnary.getType();
		}
		
		if (eT == Type.INTEGER && op == Kind.OP_PLUS) {
			expressionUnary.setType(Type.INTEGER);
			return expressionUnary.getType();
		}
		
		if (eT == Type.INTEGER && op == Kind.OP_MINUS) {
			expressionUnary.setType(Type.INTEGER);
			return expressionUnary.getType();
		}
		
		if (eT == Type.FLOAT && op == Kind.OP_MINUS) {
			expressionUnary.setType(Type.FLOAT);
			return expressionUnary.getType();
		}
		
		if (eT == Type.FLOAT && op == Kind.OP_PLUS) {
			expressionUnary.setType(Type.FLOAT);
			return expressionUnary.getType();
		}
		
		String message = "Incompatible type for expressionUnary: " + expressionUnary.firstToken.getText();
		throw new SemanticException(expressionUnary.firstToken, message);
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		// TODO Auto-generated method stub
//		VariableDeclaration dec = (VariableDeclaration) symbolTable.lookup(lhs.identifier);
//		VariableListDeclaration decList = (VariableListDeclaration) symbolTable.lookup(lhs.identifier);
		Declaration dec = symbolTable.lookup(lhs.identifier);
		
		if (dec != null && dec instanceof VariableDeclaration ) {
			VariableDeclaration decV = (VariableDeclaration) dec;
			lhs.setDec(decV);
			lhs.setType(PLPTypes.getType(decV.type));
			return lhs.getType();
		}
		
		if (dec != null && dec instanceof VariableListDeclaration) {
			VariableListDeclaration decList = (VariableListDeclaration) dec;
			lhs.setDec(decList);
			lhs.setType(PLPTypes.getType(decList.type));
			return lhs.getType();
		}
		
		String message = "Missing declaration.";
		throw new SemanticException(lhs.firstToken, message);
	}

}
