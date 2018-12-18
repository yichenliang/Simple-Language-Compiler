// Yichen Liang    p6   Nov 23   11:59pm
package cop5556fa18;

import cop5556fa18.PLPAST.AssignmentStatement;
import cop5556fa18.PLPAST.Block;
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
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPAST.VariableListDeclaration;
import cop5556fa18.PLPAST.WhileStatement;
import cop5556fa18.PLPTypes.Type;
import cop5556fa18.PLPAST.Declaration;
import cop5556fa18.RuntimeFunctions;
import cop5556fa18.PLPScanner.Kind;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class PLPCodeGen implements PLPASTVisitor, Opcodes {
	
	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	
	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	
	private int slot = 1;

	public PLPCodeGen(String sourceFileName, boolean dEVEL, boolean gRADE) {
		super();
		this.sourceFileName = sourceFileName;
		DEVEL = dEVEL;
		GRADE = gRADE;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO refactor and extend as necessary
				for (PLPASTNode node : block.declarationsAndStatements) {
					node.visit(this, null);
				}
				return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// cw = new ClassWriter(0); 
		// If the call to mv.visitMaxs(1, 1) crashes, it is sometimes helpful 
		// to temporarily run it without COMPUTE_FRAMES. You probably won't 
		// get a completely correct classfile, but you will be able to see the 
		// code that was generated.
		
		className = program.name;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();
		
		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		PLPCodeGenUtils.genLog(DEVEL, mv, "entering main");

		program.block.visit(this, arg);  //!!!!!

		// generates code to add string to log
		PLPCodeGenUtils.genLog(DEVEL, mv, "leaving main");
		
		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor, asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily change the parameter in the ClassWriter constructor
		// from COMPUTE_FRAMES to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();			
	}

	//VariableDeclaration -> Type IDENTIFIER ( e | Expression )
	@Override
	public Object visitVariableDeclaration(VariableDeclaration declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Label blockStart = new Label();
		Label blockEnd = new Label();
		declaration.slot = slot;
		if(declaration.expression != null) {
			declaration.expression.visit(this, arg);
			switch (declaration.expression.getType()) {
				case INTEGER:
					mv.visitVarInsn(ISTORE, declaration.slot);
					break;
				case BOOLEAN:
					mv.visitVarInsn(ISTORE, declaration.slot);
					break;
				case FLOAT:
					mv.visitVarInsn(FSTORE, declaration.slot);
					break;
				case CHAR:   
					mv.visitVarInsn(ISTORE, declaration.slot);
					break;
				case STRING: 
					mv.visitVarInsn(ASTORE, declaration.slot);
					break;
				default:
					break;
			}
		}
		slot++;
		declaration.startL = blockStart;
		declaration.endL = blockEnd;
		return null;
	}

	//VariableListDeclaration -> Type IDENTIFIER  IDENTIFIER +
	@Override
	public Object visitVariableListDeclaration(VariableListDeclaration declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		for(String n : declaration.names) {
			int i = declaration.names.indexOf(n);
			if(i != -1) {
				declaration.slotList.add(i, slot);	
			}
			slot++;
		}
		return null;
	}

	@Override
	public Object visitExpressionBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Label setTrue = new Label();
		Label endEB = new Label();
		
		if (expressionBinary.leftExpression.getType() == expressionBinary.rightExpression.getType()) {
			
			//integer	integer	 +,-,*,/,%,**, &, |	integer
			//integer	integer	 ==, !=, >,>=, <, <=	boolean
			if (expressionBinary.leftExpression.getType() == Type.INTEGER) {
				switch (expressionBinary.op) {
					case OP_PLUS:  //+ 
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitInsn(IADD);
						break;
					case OP_MINUS:  //-
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitInsn(ISUB);
						break;
					case OP_TIMES:  //*
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitInsn(IMUL);
						break;
					case OP_DIV:   // /	
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitInsn(IDIV);
						break;
					case OP_MOD:   // %
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitInsn(IREM);
						break;
					case OP_POWER: //**
						expressionBinary.leftExpression.visit(this, arg);
						mv.visitInsn(I2D);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitInsn(I2D);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
						mv.visitInsn(D2I);
						break;
					case OP_AND:   // &
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitInsn(IAND);
						break;
					case OP_OR:    // |
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitInsn(IOR);
						break;
					case OP_EQ:    // ==
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitJumpInsn(IF_ICMPEQ, setTrue);
						mv.visitLdcInsn(false);
						break;
					case OP_NEQ:   // !=
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitJumpInsn(IF_ICMPNE, setTrue);
						mv.visitLdcInsn(false);
						break;
					case OP_GT:    // >
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitJumpInsn(IF_ICMPGT, setTrue);
						mv.visitLdcInsn(false);
						break;
					case OP_GE:    // >=
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitJumpInsn(IF_ICMPGE, setTrue);
						mv.visitLdcInsn(false);
						break;
					case OP_LT:    // <
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitJumpInsn(IF_ICMPLT, setTrue);
						mv.visitLdcInsn(false);
						break;
					case OP_LE:    // <=
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitJumpInsn(IF_ICMPLE, setTrue);
						mv.visitLdcInsn(false);
						break;
					default:
						break;
				}
				mv.visitJumpInsn(GOTO, endEB);
				mv.visitLabel(setTrue);
				mv.visitLdcInsn(true);
				mv.visitLabel(endEB);
			} else if (expressionBinary.leftExpression.getType() == Type.FLOAT) {
				//float	float	+,-,*,/,**	float
				//float	float	==, !=, >,>=, <, <=	boolean
				switch (expressionBinary.op) {
					case OP_PLUS:  //+ 
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitInsn(FADD);
						break;
					case OP_MINUS:  //-
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitInsn(FSUB);
						break;
					case OP_TIMES:  //*
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitInsn(FMUL);
						break;
					case OP_DIV:   // /	
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitInsn(FDIV);
						break;
					case OP_POWER: //**
						expressionBinary.leftExpression.visit(this, arg);
						mv.visitInsn(F2D);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitInsn(F2D);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
						mv.visitInsn(D2F);
						break;
						//Label setTrue = new Label(); l3
						//Label endEB = new Label();   l4
					case OP_EQ:    // ==
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitInsn(FCMPL);
						//Label l3 = new Label(); setTrue
						mv.visitJumpInsn(IFNE, setTrue);
						mv.visitInsn(ICONST_1);
						//Label l4 = new Label(); endEB
						mv.visitJumpInsn(GOTO, endEB);
						mv.visitLabel(setTrue);
						mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.FLOAT, Opcodes.FLOAT}, 0, null);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(endEB);
						mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
						break;
					// CHANGED!!!
						//Label setTrue = new Label(); l3
						//Label endEB = new Label();   l4
					case OP_NEQ:   // !=   
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitInsn(FCMPL);
						//Label l3 = new Label();
						mv.visitJumpInsn(IFEQ, setTrue);
						mv.visitInsn(ICONST_1);
						//Label l4 = new Label();
						mv.visitJumpInsn(GOTO, endEB);
						mv.visitLabel(setTrue);
						mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.FLOAT, Opcodes.FLOAT}, 0, null);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(endEB);
						mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});	 
						break;
						//Label setTrue = new Label(); l3
						//Label endEB = new Label();   l4
					case OP_GT:    // >
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitInsn(FCMPL);
						//Label l3 = new Label();
						mv.visitJumpInsn(IFLE, setTrue);
						mv.visitInsn(ICONST_1);
						//Label l4 = new Label();
						mv.visitJumpInsn(GOTO, endEB);
						mv.visitLabel(setTrue);
						mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.FLOAT, Opcodes.FLOAT}, 0, null);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(endEB);
						mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
						break;
						//Label setTrue = new Label(); l3
						//Label endEB = new Label();   l4
					case OP_GE:    // >=
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitInsn(FCMPL);
						//Label l3 = new Label();
						mv.visitJumpInsn(IFLT, setTrue);
						mv.visitInsn(ICONST_1);
						//Label l4 = new Label();
						mv.visitJumpInsn(GOTO, endEB);
						mv.visitLabel(setTrue);
						mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.FLOAT, Opcodes.FLOAT}, 0, null);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(endEB);
						mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
						break;
						//Label setTrue = new Label(); l3
						//Label endEB = new Label();   l4
					case OP_LT:    // <
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitInsn(FCMPG);
						//Label l3 = new Label();
						mv.visitJumpInsn(IFGE, setTrue);
						mv.visitInsn(ICONST_1);
						//Label l4 = new Label();
						mv.visitJumpInsn(GOTO, endEB);
						mv.visitLabel(setTrue);
						mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.FLOAT, Opcodes.FLOAT}, 0, null);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(endEB);
						mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
						break;
						//Label setTrue = new Label(); l3
						//Label endEB = new Label();   l4
					case OP_LE:    // <=
						expressionBinary.leftExpression.visit(this, arg);
						expressionBinary.rightExpression.visit(this, arg);
						mv.visitInsn(FCMPG);
						//Label l3 = new Label();
						mv.visitJumpInsn(IFGT, setTrue);
						mv.visitInsn(ICONST_1);
						//Label l4 = new Label();
						mv.visitJumpInsn(GOTO, endEB);
						mv.visitLabel(setTrue);
						mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.FLOAT, Opcodes.FLOAT}, 0, null);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(endEB);
						mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
						break;	
					default:
						break;
				}
//				mv.visitJumpInsn(GOTO, endEB);
//				mv.visitLabel(setTrue);
//				mv.visitLdcInsn(true);
//				mv.visitLabel(endEB);
			} else if (expressionBinary.leftExpression.getType() == Type.BOOLEAN) {
				//boolean	boolean	&, |	boolean
				//boolean	boolean	==, !=, >,>=, <, <=	boolean
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				switch (expressionBinary.op) {
					case OP_AND:   // &
						mv.visitInsn(IAND);
						break;
					case OP_OR:    // |
						mv.visitInsn(IOR);
						break;
					case OP_EQ:    // ==
						mv.visitJumpInsn(IF_ICMPEQ, setTrue);
						mv.visitLdcInsn(false);
						break;
					case OP_NEQ:   // !=
						mv.visitJumpInsn(IF_ICMPNE, setTrue);
						mv.visitLdcInsn(false);
						break;
					case OP_GT:    // >
						mv.visitJumpInsn(IF_ICMPGT, setTrue);
						mv.visitLdcInsn(false);
						break;
					case OP_GE:    // >=
						mv.visitJumpInsn(IF_ICMPGE, setTrue);
						mv.visitLdcInsn(false);
						break;
					case OP_LT:    // <
						mv.visitJumpInsn(IF_ICMPLT, setTrue);
						mv.visitLdcInsn(false);
						break;
					case OP_LE:    // <=
						mv.visitJumpInsn(IF_ICMPLE, setTrue);
						mv.visitLdcInsn(false);
						break;
					default:
						break;
				}
				mv.visitJumpInsn(GOTO, endEB);
				mv.visitLabel(setTrue);
				mv.visitLdcInsn(true);
				mv.visitLabel(endEB);
			}
			else if (expressionBinary.leftExpression.getType() == Type.STRING) {
				mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
				mv.visitInsn(DUP);
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;", false);
				mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
			}
			
		} 
		else if(expressionBinary.leftExpression.getType() == Type.INTEGER 
				&& expressionBinary.rightExpression.getType() == Type.FLOAT) {
			//integer	float	+,-,*,/,**	float
			switch (expressionBinary.op) {
				case OP_PLUS:
					expressionBinary.leftExpression.visit(this, arg);
					mv.visitInsn(I2F);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(FADD);
					break;
				case OP_MINUS:
					expressionBinary.leftExpression.visit(this, arg);
					mv.visitInsn(I2F);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(FSUB);
					break;
				case OP_TIMES:
					expressionBinary.leftExpression.visit(this, arg);
					mv.visitInsn(I2F);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(FMUL);
					break;
				case OP_DIV:
					expressionBinary.leftExpression.visit(this, arg);
					mv.visitInsn(I2F);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(FDIV);
					break;	
				case OP_POWER:
					expressionBinary.leftExpression.visit(this, arg);
					mv.visitInsn(I2D);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(F2D);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
					mv.visitInsn(D2F);
					break;
				default:
					break;
			}
			
		}
		else if(expressionBinary.leftExpression.getType() == Type.FLOAT 
				&& expressionBinary.rightExpression.getType() == Type.INTEGER) {
			//float	integer	+,-,*,/,**	float
			switch (expressionBinary.op) {
				case OP_PLUS:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(I2F);
					mv.visitInsn(FADD);
					break;
				case OP_MINUS:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(I2F);
					mv.visitInsn(FSUB);
					break;
				case OP_TIMES:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(I2F);
					mv.visitInsn(FMUL);
					break;
				case OP_DIV:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(I2F);
					mv.visitInsn(FDIV);
					break;	
				case OP_POWER:
					expressionBinary.leftExpression.visit(this, arg);
					mv.visitInsn(F2D);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(I2D);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
					mv.visitInsn(D2F);
					break;
				default:
					break;	
			}
		}
		
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Label start = new Label();
		Label startTrue = new Label();
		Label startFalse = new Label();
		Label end = new Label();
		
		mv.visitLabel(start);
		expressionConditional.condition.visit(this, arg); //condition
		mv.visitJumpInsn(IFNE, startTrue); //IFNE True
		
		mv.visitLabel(startFalse);
		expressionConditional.falseExpression.visit(this, arg); //FALSE
		Label endFalse = new Label();
		mv.visitLabel(endFalse);
		mv.visitJumpInsn(GOTO, end); //GOTO END
		
		mv.visitLabel(startTrue);
		expressionConditional.trueExpression.visit(this, arg); //true
		Label endTrue = new Label();
		mv.visitLabel(endTrue);
		
		mv.visitLabel(end);
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
	}

	@Override
	public Object visitFunctionWithArg(FunctionWithArg FunctionWithArg, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//FunctionName -> sin | cos | atan | abs | log | int | float
		FunctionWithArg.expression.visit(this, arg);
		Kind k = FunctionWithArg.functionName;
		Type expT = FunctionWithArg.expression.getType();
		switch(k) {
			case KW_sin:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
						"sin", RuntimeFunctions.sinSig, false);
				break;
		    case KW_cos:
		    	mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
						"cos", RuntimeFunctions.cosSig, false);
				break;
		    case KW_atan:
		    	mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
						"atan", RuntimeFunctions.atanSig, false);
				break;
		    case KW_abs:
		    	if (expT == Type.INTEGER) {
		    		mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
							"absInt", RuntimeFunctions.absIntSig, false);
		    	}
		    	else if (expT == Type.FLOAT) {
		    		mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
							"abs", RuntimeFunctions.absSig, false);
		    	}
		    	break;
		    case KW_log:
		    	mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
						"log", RuntimeFunctions.logSig, false);
				break;
		    case KW_int:
		    	if (expT != Type.INTEGER) {
					mv.visitInsn(F2I);
				}
				break;
		    case KW_float:
				if (expT != Type.FLOAT) {
					mv.visitInsn(I2F);
				}
				break;
			default:
				break;
		}
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdentifier expressionIdent, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Declaration dec = expressionIdent.getDec();
		
		if (dec != null && dec instanceof VariableDeclaration) {
			VariableDeclaration decV = (VariableDeclaration) dec;
			
			if(PLPTypes.getType(decV.type) == Type.INTEGER || PLPTypes.getType(decV.type) == Type.BOOLEAN) {
				mv.visitVarInsn(ILOAD, decV.slot); // ILOAD: put it on the top of the stack
			}
			else if (PLPTypes.getType(decV.type) == Type.FLOAT) {
				mv.visitVarInsn(FLOAD, decV.slot);
			}
			else if (PLPTypes.getType(decV.type) == Type.CHAR) {
				mv.visitVarInsn(ILOAD, decV.slot);  //TODO ???????????????
			}
			else if (PLPTypes.getType(decV.type) == Type.STRING) {
				mv.visitVarInsn(ALOAD, decV.slot);
			}
			
		}
		
		if(dec != null && dec instanceof VariableListDeclaration) {
			VariableListDeclaration decList = (VariableListDeclaration) dec;
			int i = decList.names.indexOf(expressionIdent.name);
			int slotIndex = decList.slotList.get(i);
			
			if(PLPTypes.getType(decList.type) == Type.INTEGER || PLPTypes.getType(decList.type) == Type.BOOLEAN) {
				mv.visitVarInsn(ILOAD, slotIndex); // ILOAD: put it on the top of the stack
			}
			else if (PLPTypes.getType(decList.type) == Type.FLOAT) {
				mv.visitVarInsn(FLOAD, slotIndex);
			}
			else if (PLPTypes.getType(decList.type) == Type.CHAR) {
				mv.visitVarInsn(ILOAD, slotIndex);  //TODO ???????????????
			}
			else if (PLPTypes.getType(decList.type) == Type.STRING) {
				mv.visitVarInsn(ALOAD, slotIndex);
			}
		}
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionStringLiteral(ExpressionStringLiteral expressionStringLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitLdcInsn(expressionStringLiteral.text);
		return null;
	}

	@Override
	public Object visitExpressionCharLiteral(ExpressionCharLiteral expressionCharLiteral, Object arg) throws Exception {
		// TODO Auto-generated method stub
		mv.visitLdcInsn(expressionCharLiteral.text);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementAssign.expression.visit(this, arg);
		statementAssign.lhs.visit(this, arg);
		return null;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		//VariableDeclaration 
		Declaration dec = lhs.dec;
		
		if(dec != null && dec instanceof VariableDeclaration) {
			VariableDeclaration decV = (VariableDeclaration) dec;
			if(lhs.type == Type.BOOLEAN || lhs.type == Type.INTEGER) {
				mv.visitVarInsn(ISTORE, decV.slot);
			}
			else if(lhs.type == Type.FLOAT) {
				mv.visitVarInsn(FSTORE, decV.slot);
			}
			else if(lhs.type == Type.CHAR) {
				mv.visitVarInsn(ISTORE, decV.slot);
			}
			else if(lhs.type == Type.STRING) {
				mv.visitVarInsn(ASTORE, decV.slot);
			}
		}
		
		//VariableListDeclaration
		if(dec != null && dec instanceof VariableListDeclaration) {
			VariableListDeclaration decList = (VariableListDeclaration) dec;
			int i = decList.names.indexOf(lhs.identifier);
			int iSlot = decList.slotList.get(i);
			
			if(lhs.type == Type.BOOLEAN || lhs.type == Type.INTEGER) {
				mv.visitVarInsn(ISTORE, iSlot);
			}
			else if(lhs.type == Type.FLOAT) {
				mv.visitVarInsn(FSTORE, iSlot);
			}
			else if(lhs.type == Type.CHAR) {
				mv.visitVarInsn(ISTORE, iSlot);
			}
			else if(lhs.type == Type.STRING) {
				mv.visitVarInsn(ASTORE, iSlot);
			}
		}
		
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Label startCondition = new Label();
		Label startBody = new Label();
		Label endBody = new Label();
		
		mv.visitLabel(startCondition);
		ifStatement.condition.visit(this, arg); //condition
		Label endCondition = new Label();
		mv.visitLabel(endCondition);
		mv.visitJumpInsn(IFNE, startBody); //IFNE BODY
		mv.visitJumpInsn(GOTO, endBody); //GOTO Condition
		
		mv.visitLabel(startBody);
		ifStatement.block.visit(this, arg); //BODY
		mv.visitLabel(endBody);
		
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Label startCondition = new Label();
		Label startBody = new Label();
		
		mv.visitJumpInsn(GOTO, startCondition); //GOTO condition
		
		mv.visitLabel(startBody);
		whileStatement.b.visit(this, arg); //BODY
		Label endBody = new Label();
		mv.visitLabel(endBody);
		
		mv.visitLabel(startCondition);
		whileStatement.condition.visit(this, arg); //condition
		Label endCondition = new Label();
		mv.visitLabel(endCondition);
		mv.visitJumpInsn(IFNE, startBody); //IFNE BODY
		
		return null;
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg) throws Exception {
		/**
		 * TODO refactor and complete implementation.
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		printStatement.expression.visit(this, arg);
		Type type = printStatement.expression.getType();
		switch (type) {
			case INTEGER : {
				PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(I)V", false);
			}
			break;
			case BOOLEAN : {
				PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
				// TODO implement functionality
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				//printStatement.expression.visit(this, arg);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(Z)V", false);
				//throw new UnsupportedOperationException();
			}
			break; 
			case FLOAT : {
				PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
				// TODO implement functionality
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(F)V", false);
				//throw new UnsupportedOperationException();
			}
			break; 
			case CHAR : {
				PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
				// TODO implement functionality
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(C)V", false);
				//throw new UnsupportedOperationException();
			}
			break;
			case STRING : {
				PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
				// TODO implement functionality
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(Ljava/lang/String;)V", false);
				
				//throw new UnsupportedOperationException();
			}
			break;
			default:
			break;
		}
		return null;	
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		sleepStatement.time.visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionUnary.expression.visit(this, arg);
		Kind op = expressionUnary.op;
		//UnaryExpression -> + UnaryExpression | - UnaryExpression | ! UnaryExpression | Primary
		//! : integer or Boolean
		//+, - : integer or float. 

		switch(op) {
			case OP_PLUS:
				break;
			case OP_MINUS:
				if (expressionUnary.getType() == Type.INTEGER) {
					mv.visitLdcInsn(new Integer(-1));
					mv.visitInsn(IMUL);
				} else if(expressionUnary.getType() == Type.FLOAT) {
					mv.visitLdcInsn(new Float(-1.0));
					mv.visitInsn(FMUL);
				}
				break;
			case OP_EXCLAMATION:
				if (expressionUnary.getType() == Type.INTEGER) {
					mv.visitLdcInsn(new Integer(Integer.MAX_VALUE));
					mv.visitInsn(IXOR);
					mv.visitLdcInsn(new Integer(Integer.MIN_VALUE));
					mv.visitInsn(ISUB);
				} else if(expressionUnary.getType() == Type.BOOLEAN) {
					Label originalT = new Label();
					Label originalF = new Label();
					mv.visitLdcInsn(true);
					mv.visitJumpInsn(IF_ICMPEQ, originalT);
					mv.visitLdcInsn(true);
					mv.visitJumpInsn(GOTO, originalF);
					mv.visitLabel(originalT);
					mv.visitLdcInsn(false);
					mv.visitLabel(originalF);
				}
				break;
			default:
				break;			
		}
		return null;
	}

}
