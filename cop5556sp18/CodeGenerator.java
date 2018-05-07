/**
 * Starter code for CodeGenerator.java used n the class project in COP5556 Programming Language Principles 
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



import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556sp18.Types.Type;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionBooleanLiteral;
import cop5556sp18.AST.ExpressionConditional;
import cop5556sp18.AST.ExpressionFloatLiteral;
import cop5556sp18.AST.ExpressionFunctionAppWithExpressionArg;
import cop5556sp18.AST.ExpressionFunctionAppWithPixel;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.ExpressionPixel;
import cop5556sp18.AST.ExpressionPixelConstructor;
import cop5556sp18.AST.ExpressionPredefinedName;
import cop5556sp18.AST.ExpressionUnary;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;

import cop5556sp18.CodeGenUtils;
import cop5556sp18.Scanner.Kind;

public class CodeGenerator implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */

	static final int Z = 255;

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int slotNumber = 1;
	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	final int defaultWidth;
	final int defaultHeight;
	// final boolean itf = false;
	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 * @param defaultWidth
	 *            default width of images
	 * @param defaultHeight
	 *            default height of images
	 */
	public CodeGenerator(boolean DEVEL, boolean GRADE, String sourceFileName,
			int defaultWidth, int defaultHeight) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		for (ASTNode node : block.decsOrStatements) {
			node.visit(this, null);
		}
		return null;
	}

	@Override
	public Object visitBooleanLiteral(
			ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
		declaration.slotNumber = slotNumber++;
		//cw.visitField(0, declaration.name, Types.getJVMType(Types.getType(declaration.type)), null, declaration.slotNumber);
		
		//mv.visitFieldInsn(GETSTATIC, className, declaration.name, Types.getJVMType(Types.getType(declaration.type)));
		
		if(declaration.type.equals(Kind.KW_image) && (declaration.height != null && declaration.width != null)){
			declaration.width.visit(this, arg);
			declaration.height.visit(this, arg);
			
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeImage", RuntimeImageSupport.makeImageSig, false);
			mv.visitVarInsn(ASTORE, declaration.slotNumber);
		}
		else if(declaration.type.equals(Kind.KW_image) && (declaration.height == null && declaration.width == null)){
			mv.visitLdcInsn(defaultWidth);
			mv.visitLdcInsn(defaultHeight);
			
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeImage", RuntimeImageSupport.makeImageSig, false);
			mv.visitVarInsn(ASTORE, declaration.slotNumber);
		}
//		else if(declaration.type.equals(Kind.KW_image)){
//			mv.visitVarInsn(ASTORE, declaration.slotNumber);	
//		}
		
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		// TODO labels have to be created - AJ	
		
		Type e0 = expressionBinary.leftExpression.type;
		Type e1 = expressionBinary.rightExpression.type;
		Kind op	= expressionBinary.op;
		
//		if(op.equals(Kind.OP_PLUS) || op.equals(Kind.OP_MINUS) || op.equals(Kind.OP_DIV) || op.equals(Kind.OP_TIMES)
//				|| op.equals(Kind.OP_MOD) || op.equals(Kind.OP_LE) || op.equals(Kind.OP_LT) || op.equals(Kind.OP_GT)
//				|| op.equals(Kind.OP_GE) || op.equals(Kind.OP_EQ) || op.equals(Kind.OP_NEQ)){
//			expressionBinary.leftExpression.visit(this, arg);
//			expressionBinary.rightExpression.visit(this, arg);
//		}
		
		if(op.equals(Kind.OP_PLUS)){
			
			if(e0.equals(Type.INTEGER) && e1.equals(Type.INTEGER)){
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(IADD);
			}
			else if(e0.equals(Type.FLOAT) && e1.equals(Type.FLOAT)){
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FADD);
			}
			else if(e0.equals(Type.INTEGER) && e1.equals(Type.FLOAT)){
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(I2F);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FADD);
			}
			else if(e0.equals(Type.FLOAT) && e1.equals(Type.INTEGER)){
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2F);
				mv.visitInsn(FADD);
			}
		}
		else if(op.equals(Kind.OP_MINUS)){
			
			if(e0.equals(Type.INTEGER) && e1.equals(Type.INTEGER)){
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(ISUB);
			}else if(e0.equals(Type.FLOAT) && e1.equals(Type.FLOAT)){
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FSUB);
			}
			else if(e0.equals(Type.INTEGER) && e1.equals(Type.FLOAT)){
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(I2F);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FSUB);
			}
			else if(e0.equals(Type.FLOAT) && e1.equals(Type.INTEGER)){
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2F);
				mv.visitInsn(FSUB);
			}
		}
		else if(op.equals(Kind.OP_TIMES)){
			
			if(e0.equals(Type.INTEGER) && e1.equals(Type.INTEGER)){
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(IMUL);
			}else if(e0.equals(Type.FLOAT) && e1.equals(Type.FLOAT)){
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FMUL);
			}
			else if(e0.equals(Type.INTEGER) && e1.equals(Type.FLOAT)){
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(I2F);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FMUL);
			}
			else if(e0.equals(Type.FLOAT) && e1.equals(Type.INTEGER)){
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2F);
				mv.visitInsn(FMUL);
			}
		}
		else if(op.equals(Kind.OP_DIV)){
			
			if(e0.equals(Type.INTEGER) && e1.equals(Type.INTEGER)){
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(IDIV);
			}else if(e0.equals(Type.FLOAT) && e1.equals(Type.FLOAT)){
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FDIV);
			}
			else if(e0.equals(Type.INTEGER) && e1.equals(Type.FLOAT)){
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(I2F);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FDIV);
			}
			else if(e0.equals(Type.FLOAT) && e1.equals(Type.INTEGER)){
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2F);
				mv.visitInsn(FDIV);
			}
		}
		else if(op.equals(Kind.OP_MOD)){
			
			if(e0.equals(Type.INTEGER) && e1.equals(Type.INTEGER)){
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(IREM);
			}else if(e0.equals(Type.FLOAT) && e1.equals(Type.FLOAT)){
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FREM);
			}
			else if(e0.equals(Type.INTEGER) && e1.equals(Type.FLOAT)){
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(I2F);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FREM);
			}
			else if(e0.equals(Type.FLOAT) && e1.equals(Type.INTEGER)){
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2F);
				mv.visitInsn(FREM);
			}
		}
		else if(op.equals(Kind.OP_AND)){
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			
			if(e0.equals(Type.INTEGER) && e1.equals(Type.INTEGER)){
				mv.visitInsn(IAND);
			}else if(e0.equals(Type.BOOLEAN) && e1.equals(Type.BOOLEAN)){
				mv.visitInsn(IAND);
			}
			{
				//mv.visitMethodInsn(INVOKESTATIC, );
			}
		}
		else if(op.equals(Kind.OP_OR)){
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			if(e0.equals(Type.INTEGER) && e1.equals(Type.INTEGER)){
				mv.visitInsn(IOR);
			}else if(e0.equals(Type.BOOLEAN) && e1.equals(Type.BOOLEAN)){
				mv.visitInsn(IOR);
			}
		}
		else if(op.equals(Kind.OP_POWER)){
			if(e0.equals(Type.INTEGER) && e1.equals(Type.INTEGER)){
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(I2D);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2D);
				mv.visitMethodInsn(INVOKESTATIC,"java/lang/Math","pow", "(DD)D", false);
				mv.visitInsn(D2I);
			}else if(e0.equals(Type.FLOAT) && e1.equals(Type.FLOAT)){
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(F2D);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC,"java/lang/Math","pow", "(DD)D", false);
				mv.visitInsn(D2F);
			}
			else if(e0.equals(Type.INTEGER) && e1.equals(Type.FLOAT)){
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(I2D);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC,"java/lang/Math","pow", "(DD)D", false);
				mv.visitInsn(D2F);
			}
			else if(e0.equals(Type.FLOAT) && e1.equals(Type.INTEGER)){
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(F2D);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2D);
				mv.visitMethodInsn(INVOKESTATIC,"java/lang/Math","pow", "(DD)D", false);
				mv.visitInsn(D2F);
			}
		}
		else if(op.equals(Kind.OP_EQ)){
			Label start = new Label();
			Label end = new Label();
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			if(e0.equals(Type.INTEGER) && e1.equals(Type.INTEGER)){
				mv.visitJumpInsn(IF_ICMPEQ, start);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, end);
				mv.visitLabel(start);
				mv.visitLdcInsn(1);
				mv.visitLabel(end);
			}
			else if(e0.equals(Type.FLOAT) && e1.equals(Type.FLOAT)){
				mv.visitJumpInsn(FCMPG, start);
				
				mv.visitJumpInsn(GOTO, end);
				mv.visitLabel(start);
				mv.visitLdcInsn(1);
				//mv.visitInsn(IXOR); HOw to remove top element
				mv.visitLabel(end);
				}
		}
		
		else if(op.equals(Kind.OP_NEQ)){
			Label start = new Label();
			Label end = new Label();
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			if(e0.equals(Type.INTEGER) && e1.equals(Type.INTEGER)){
				mv.visitJumpInsn(IF_ICMPEQ, start);
				mv.visitLdcInsn(1);
				mv.visitJumpInsn(GOTO, end);
				mv.visitLabel(start);
				mv.visitLdcInsn(0);
				mv.visitLabel(end);
			}
			else if(e0.equals(Type.FLOAT) && e1.equals(Type.FLOAT)){
				mv.visitInsn(FCMPG);
				}
			else if(e0.equals(Type.BOOLEAN) && e1.equals(Type.BOOLEAN)){
				mv.visitJumpInsn(IF_ICMPEQ, start);
				mv.visitLdcInsn(1);
				mv.visitJumpInsn(GOTO, end);
				mv.visitLabel(start);
				mv.visitLdcInsn(0);
				mv.visitLabel(end);
			}
		}
		
		else if(op.equals(Kind.OP_GE)){
			Label start = new Label();
			Label end = new Label();
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			if(e0.equals(Type.INTEGER) && e1.equals(Type.INTEGER)){
				mv.visitJumpInsn(IF_ICMPGE, start);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, end);
				mv.visitLabel(start);
				mv.visitLdcInsn(1);
				mv.visitLabel(end);
			}
			else if(e0.equals(Type.FLOAT) && e1.equals(Type.FLOAT)){
				mv.visitInsn(FCMPG);
				mv.visitJumpInsn(IFGE, start);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, end);
				mv.visitLabel(start);
				mv.visitLdcInsn(1);
				mv.visitLabel(end);
			}
			else if(e0.equals(Type.BOOLEAN) && e1.equals(Type.BOOLEAN)){
				mv.visitJumpInsn(IF_ICMPGE, start);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, end);
				mv.visitLabel(start);
				mv.visitLdcInsn(1);
				mv.visitLabel(end);
			}

			
		}
		else if(op.equals(Kind.OP_LE)){
			Label start = new Label();
			Label end = new Label();
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			if(e0.equals(Type.INTEGER) && e1.equals(Type.INTEGER)){
				mv.visitJumpInsn(IF_ICMPLE, start);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, end);
				mv.visitLabel(start);
				mv.visitLdcInsn(1);
				mv.visitLabel(end);
			}
			else if(e0.equals(Type.FLOAT) && e1.equals(Type.FLOAT)){
				mv.visitInsn(FCMPG);
				mv.visitJumpInsn(IFLE, start);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, end);
				mv.visitLabel(start);
				mv.visitLdcInsn(1);
				mv.visitLabel(end);
			}
			else if(e0.equals(Type.INTEGER) && e1.equals(Type.INTEGER)){
				mv.visitJumpInsn(IF_ICMPLE, start);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, end);
				mv.visitLabel(start);
				mv.visitLdcInsn(1);
				mv.visitLabel(end);
			}
		}
		
		else if(op.equals(Kind.OP_GT)){
			Label start = new Label();
			Label end = new Label();
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			if(e0.equals(Type.INTEGER) && e1.equals(Type.INTEGER)){
				mv.visitJumpInsn(IF_ICMPGT, start);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, end);
				mv.visitLabel(start);
				mv.visitLdcInsn(1);
				mv.visitLabel(end);
			}
			else if(e0.equals(Type.FLOAT) && e1.equals(Type.FLOAT)){
				mv.visitInsn(FCMPG);
				mv.visitJumpInsn(IFGT, start);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, end);
				mv.visitLabel(start);
				mv.visitLdcInsn(1);
				mv.visitLabel(end);
			}
			else if(e0.equals(Type.BOOLEAN) && e1.equals(Type.BOOLEAN)){
				mv.visitJumpInsn(IF_ICMPGT, start);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, end);
				mv.visitLabel(start);
				mv.visitLdcInsn(1);
				mv.visitLabel(end);
			}
		}
		
		else if(op.equals(Kind.OP_LT)){
			Label start = new Label();
			Label end = new Label();
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			if(e0.equals(Type.INTEGER) && e1.equals(Type.INTEGER)){
				mv.visitJumpInsn(IF_ICMPLT, start);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, end);
				mv.visitLabel(start);
				mv.visitLdcInsn(1);
				mv.visitLabel(end);
			}
			else if(e0.equals(Type.FLOAT) && e1.equals(Type.FLOAT)){
				mv.visitInsn(FCMPG);
				mv.visitJumpInsn(IFLT, start);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, end);
				mv.visitLabel(start);
				mv.visitLdcInsn(1);
				mv.visitLabel(end);
			}
			else if(e0.equals(Type.BOOLEAN) && e1.equals(Type.BOOLEAN)){
				mv.visitJumpInsn(IF_ICMPLT, start);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, end);
				mv.visitLabel(start);
				mv.visitLdcInsn(1);
				mv.visitLabel(end);
			}
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionConditional(
			ExpressionConditional expressionConditional, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Label start = new Label();
		Label end = new Label();
		expressionConditional.guard.visit(this, arg);
		mv.visitJumpInsn(IFEQ, start);
		expressionConditional.trueExpression.visit(this, arg);
		mv.visitJumpInsn(GOTO, end);
		mv.visitLabel(start);
		expressionConditional.falseExpression.visit(this, arg);
		mv.visitLabel(end);
		
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionFloatLiteral(
			ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionFunctionAppWithExpressionArg.e.visit(this, arg);
		Kind nameKind = expressionFunctionAppWithExpressionArg.function;
		if(nameKind.equals(Kind.KW_sin)){
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC,"java/lang/Math","sin", "(D)D", false); // TODO check this
			mv.visitInsn(D2F);
		}
		else if(nameKind.equals(Kind.KW_cos)){
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC,"java/lang/Math","cos", "(D)D", false); // TODO check this
			mv.visitInsn(D2F);
		}	
		else if(nameKind.equals(Kind.KW_atan)){
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC,"java/lang/Math","atan", "(D)D", false); // TODO check this
			mv.visitInsn(D2F);
		}
		else if(nameKind.equals(Kind.KW_log)){
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC,"java/lang/Math","log", "(D)D", false); // TODO check this
			mv.visitInsn(D2F);
		}
		
		else if(nameKind.equals(Kind.KW_abs)){
			if(expressionFunctionAppWithExpressionArg.e.type.equals(Type.INTEGER)){
				mv.visitMethodInsn(INVOKESTATIC,"java/lang/Math","abs", "(I)I", false);
			}
			else if(expressionFunctionAppWithExpressionArg.e.type.equals(Type.FLOAT)){
				mv.visitMethodInsn(INVOKESTATIC,"java/lang/Math","abs", "(F)F", false);
			}
		}
		else if(nameKind.equals(Kind.KW_int)){
			if(expressionFunctionAppWithExpressionArg.e.type.equals(Type.FLOAT)){
				mv.visitInsn(F2I);
			}
		}
		else if(nameKind.equals(Kind.KW_float)){
			if(expressionFunctionAppWithExpressionArg.e.type.equals(Type.INTEGER)){
				mv.visitInsn(I2F);
			}
		}
		
		else if(nameKind.equals(Kind.KW_red)){
			mv.visitMethodInsn(INVOKESTATIC,RuntimePixelOps.className,"getRed", RuntimePixelOps.getRedSig, false);

		}
		else if(nameKind.equals(Kind.KW_green)){
			mv.visitMethodInsn(INVOKESTATIC,RuntimePixelOps.className,"getGreen", RuntimePixelOps.getGreenSig, false);

		}
		else if(nameKind.equals(Kind.KW_blue)){
			mv.visitMethodInsn(INVOKESTATIC,RuntimePixelOps.className,"getBlue", RuntimePixelOps.getBlueSig, false);

		}
		else if(nameKind.equals(Kind.KW_alpha)){
			mv.visitMethodInsn(INVOKESTATIC,RuntimePixelOps.className,"getAlpha", RuntimePixelOps.getAlphaSig, false);

		}
		else if(nameKind.equals(Kind.KW_width)){
			mv.visitMethodInsn(INVOKESTATIC,RuntimeImageSupport.className,"getWidth", RuntimeImageSupport.getWidthSig, false);

		}
		else if(nameKind.equals(Kind.KW_height)){
			mv.visitMethodInsn(INVOKESTATIC,RuntimeImageSupport.className,"getHeight", RuntimeImageSupport.getHeightSig, false);

		}
		
		
		
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(
			ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Kind nameKind = expressionFunctionAppWithPixel.name;
		if(nameKind.equals(Kind.KW_cart_x)){
			expressionFunctionAppWithPixel.e0.visit(this, arg);
			mv.visitInsn(F2D);
			expressionFunctionAppWithPixel.e1.visit(this, arg);
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC,"java/lang/Math","cos", "(D)D", false); // TODO check this
			//mv.visitInsn(D2F);
			mv.visitInsn(DMUL);
			mv.visitInsn(D2I);
		}
		else if(nameKind.equals(Kind.KW_cart_y)){
			expressionFunctionAppWithPixel.e0.visit(this, arg);
			mv.visitInsn(F2D);
			expressionFunctionAppWithPixel.e1.visit(this, arg);
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC,"java/lang/Math","sin", "(D)D", false); 
			//mv.visitInsn(D2F);
			mv.visitInsn(DMUL);
			mv.visitInsn(D2I);
		}
		
		else if(nameKind.equals(Kind.KW_polar_a)){
			
			expressionFunctionAppWithPixel.e1.visit(this, arg);
			mv.visitInsn(I2D);
			expressionFunctionAppWithPixel.e0.visit(this, arg); // SWAP or this??
			mv.visitInsn(I2D);
			mv.visitMethodInsn(INVOKESTATIC,"java/lang/Math","atan2", "(DD)D", false); 
			mv.visitInsn(D2F);
			
		}
		
else if(nameKind.equals(Kind.KW_polar_r)){
			
			expressionFunctionAppWithPixel.e0.visit(this, arg);
			mv.visitInsn(I2D);
			expressionFunctionAppWithPixel.e1.visit(this, arg); // SWAP or this??
			mv.visitInsn(I2D);
			mv.visitMethodInsn(INVOKESTATIC,"java/lang/Math","hypot", "(DD)D", false); 
			mv.visitInsn(D2F);
			//mv.visitInsn(D2I);
		}
		
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		if(expressionIdent.dec.type.equals(Kind.KW_int) ||expressionIdent.dec.type.equals(Kind.KW_boolean)){
			mv.visitVarInsn(ILOAD, expressionIdent.dec.slotNumber);
		}
		else if(expressionIdent.dec.type.equals(Kind.KW_float) ){
			mv.visitVarInsn(FLOAD, expressionIdent.dec.slotNumber);
		}
		else if(expressionIdent.dec.type.equals(Kind.KW_filename) ){
			mv.visitVarInsn(ALOAD, expressionIdent.dec.slotNumber);
		}
		else{
			mv.visitVarInsn(ALOAD, expressionIdent.dec.slotNumber);
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionIntegerLiteral(
			ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		// This one is all done!
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		mv.visitVarInsn(ALOAD, expressionPixel.dec.slotNumber);
		expressionPixel.pixelSelector.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getPixel", RuntimeImageSupport.getPixelSig, false);
		
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionPixelConstructor(
			ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
			expressionPixelConstructor.alpha.visit(this, arg);
			//mv.visitMethodInsn(INVOKESTATIC,RuntimePixelOps.className,"getAlpha", RuntimePixelOps.getAlphaSig, false);
			expressionPixelConstructor.red.visit(this, arg);
			//mv.visitMethodInsn(INVOKESTATIC,RuntimePixelOps.className,"getRed", RuntimePixelOps.getRedSig, false);
			expressionPixelConstructor.green.visit(this, arg);
			//mv.visitMethodInsn(INVOKESTATIC,RuntimePixelOps.className,"getGreen", RuntimePixelOps.getGreenSig, false);
			expressionPixelConstructor.blue.visit(this, arg);
			//mv.visitMethodInsn(INVOKESTATIC,RuntimePixelOps.className,"getBlue", RuntimePixelOps.getBlueSig, false);

			mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "makePixel",RuntimePixelOps.makePixelSig, false);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionPredefinedName(
			ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		if(expressionPredefinedName.name.equals(Kind.KW_Z)){
			mv.visitLdcInsn(Z);
		}
		else if(expressionPredefinedName.name.equals(Kind.KW_default_width)){
			//mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getWidth", RuntimeImageSupport.getWidthSig, false);;
			mv.visitLdcInsn(defaultWidth);
		}
		
		else if(expressionPredefinedName.name.equals(Kind.KW_default_height)){
			//mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getHeight", RuntimeImageSupport.getHeightSig, false);;
			mv.visitLdcInsn(defaultHeight);
		}
		
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionUnary.expression.visit(this, arg);
		
		if(expressionUnary.op.equals(Kind.OP_EXCLAMATION)){
			if(expressionUnary.expression.type.equals(Type.BOOLEAN)){
				mv.visitLdcInsn(true);
				mv.visitInsn(IXOR);
			}
			else if(expressionUnary.expression.type.equals(Type.INTEGER)){
				//mv.visitLdcInsn(Integer.MAX_VALUE);
				mv.visitLdcInsn(-1);
				mv.visitInsn(IXOR);
			}
		}
		else if(expressionUnary.op.equals(Kind.OP_MINUS)){
			if(expressionUnary.expression.type.equals(Type.INTEGER)){
				mv.visitInsn(INEG);
			}
			else if(expressionUnary.expression.type.equals(Type.FLOAT)){
				mv.visitInsn(FNEG);
			}
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
		if(lhsIdent.dec.type.equals(Kind.KW_image)){
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "deepCopy", RuntimeImageSupport.deepCopySig, false);
			mv.visitVarInsn(ASTORE, lhsIdent.dec.slotNumber);
		}
		else if (lhsIdent.dec.type.equals(Kind.KW_int) || lhsIdent.dec.type.equals(Kind.KW_boolean)){
			mv.visitVarInsn(ISTORE, lhsIdent.dec.slotNumber);
		}
		else if (lhsIdent.dec.type.equals(Kind.KW_float)){
			mv.visitVarInsn(FSTORE, lhsIdent.dec.slotNumber);
		}
		else if (lhsIdent.dec.type.equals(Kind.KW_filename)){
			mv.visitVarInsn(ASTORE, lhsIdent.dec.slotNumber);
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitVarInsn(ALOAD, lhsPixel.dec.slotNumber);
		lhsPixel.pixelSelector.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "setPixel", RuntimeImageSupport.setPixelSig, false);
		
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitVarInsn(ALOAD, lhsSample.dec.slotNumber);
		lhsSample.pixelSelector.visit(this, arg);
	
		if(lhsSample.color.equals(Kind.KW_red)){
			mv.visitLdcInsn(RuntimePixelOps.RED);
		}
		else if(lhsSample.color.equals(Kind.KW_green)){
			mv.visitLdcInsn(RuntimePixelOps.GREEN);
		}
		else if(lhsSample.color.equals(Kind.KW_blue)){
			mv.visitLdcInsn(RuntimePixelOps.BLUE);
		}
		else if(lhsSample.color.equals(Kind.KW_alpha)){
			mv.visitLdcInsn(RuntimePixelOps.ALPHA);
		}
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "updatePixelColor", RuntimeImageSupport.updatePixelColorSig, false);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		if(pixelSelector.ex.type.equals(Type.FLOAT)){
			pixelSelector.ex.visit(this, arg);
			mv.visitInsn(F2I);
			pixelSelector.ey.visit(this, arg);
			mv.visitInsn(F2I);
		}else{
		pixelSelector.ex.visit(this, arg);
		pixelSelector.ey.visit(this, arg);
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// cw = new ClassWriter(0); //If the call to mv.visitMaxs(1, 1) crashes,
		// it is
		// sometime helpful to
		// temporarily run it without COMPUTE_FRAMES. You probably
		// won't get a completely correct classfile, but
		// you will be able to see the code that was
		// generated.
		className = program.progName;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null,
				"java/lang/Object", null);
		cw.visitSource(sourceFileName, null);

		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();

		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		CodeGenUtils.genLog(DEVEL, mv, "entering main");

		program.block.visit(this, arg);

		// generates code to add string to log
		CodeGenUtils.genLog(DEVEL, mv, "leaving main");

		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart,
				mainEnd, 0);
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor,
		// asm will calculate this itself and the parameters are ignored.
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

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementAssign.e.visit(this, arg);
		//CodeGenUtils.genLogTOS(GRADE, mv, statementAssign.e.type);
		statementAssign.lhs.visit(this, arg);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		statementIf.guard.visit(this, arg);
		Label if_false = new Label();
		mv.visitJumpInsn(IFEQ, if_false);
		Label if_true = new Label();
		mv.visitLabel(if_true);
		statementIf.b.visit(this, arg);
		mv.visitLabel(if_false);
		//System.out.println("leaving if statement");
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitVarInsn(ALOAD, 0);
		statementInput.e.visit(this, arg);
		mv.visitInsn(AALOAD);
		//CodeGenUtils.genLogTOS(GRADE, mv, statementInput.e.type);
		
		if(statementInput.dec.type.equals(Kind.KW_int)){
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt","(Ljava/lang/String;)I", false);
			mv.visitVarInsn(ISTORE, statementInput.dec.slotNumber);
		}
		else if(statementInput.dec.type.equals(Kind.KW_float)){
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "parseFloat","(Ljava/lang/String;)F", false);
			mv.visitVarInsn(FSTORE, statementInput.dec.slotNumber);
		}
		else if(statementInput.dec.type.equals(Kind.KW_boolean)){
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean","(Ljava/lang/String;)Z", false);
			mv.visitVarInsn(ISTORE, statementInput.dec.slotNumber);
		}
		else if(statementInput.dec.type.equals(Kind.KW_filename)){
			//mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean","(Ljava/lang/String;)Z", false);
			mv.visitVarInsn(ASTORE, statementInput.dec.slotNumber);
		}
		else if(statementInput.dec.type.equals(Kind.KW_image)){
			if(statementInput.dec.width != null && statementInput.dec.height != null){
				statementInput.dec.width.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(I)Ljava/lang/Integer;", false);
				//mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(Ljava/lang/String;)I", false);
				statementInput.dec.height.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(I)Ljava/lang/Integer;", false);
				//mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(Ljava/lang/String;)I", false);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "readImage", RuntimeImageSupport.readImageSig, false);
				//mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "resize", "(Ljava/awt/image/BufferedImage;II)Ljava/awt/image/BufferedImage;", false);
				 mv.visitVarInsn(ASTORE, statementInput.dec.slotNumber);
			}
			else if(statementInput.dec.width == null && statementInput.dec.height == null){
				mv.visitInsn(ACONST_NULL);
				//mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(I)Ljava/lang/Integer;", false);
				mv.visitInsn(ACONST_NULL);
				//mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(I)Ljava/lang/Integer;", false);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "readImage", RuntimeImageSupport.readImageSig, false);
				mv.visitVarInsn(ASTORE, statementInput.dec.slotNumber);
			}
			
		}
		//else if()
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg)
			throws Exception {
		/**
		 * TODO refactor and complete implementation.
		 * 
		 * For integers, booleans, and floats, generate code to print to
		 * console. For images, generate code to display in a frame.
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		
		/*
		 * 
		 * 
		 */
		
		
		
		statementShow.e.visit(this, arg);
		Type type = statementShow.e.type;
		switch (type) {
			case INTEGER : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(I)V", false);
			}
			break;
			
			case BOOLEAN : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				// TODO implement functionality
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(Z)V", false);
				
				//throw new UnsupportedOperationException();
			}
			break; //commented out because currently unreachable. You will need
			// it.
			case FLOAT : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				// TODO implement functionality
				
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(F)V", false);
				
				//throw new UnsupportedOperationException();
			}
			 break; 
			 //commented out because currently unreachable. You will need
			// it.
			case FILE : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				// TODO implement functionality
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(Ljava/lang/String)V", false);
				
				//throw new UnsupportedOperationException();
			}
			break;
			
			case IMAGE: {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeFrame", RuntimeImageSupport.makeFrameSig, false);
				mv.visitInsn(POP);
			}
			break;
			default: {
				// error or return null??
				//return null;
			}
			
		}
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		statementSleep.duration.visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Label while_start = new Label();
		Label while_end = new Label();
		
		mv.visitJumpInsn(GOTO, while_start);
		mv.visitLabel(while_end);
		statementWhile.b.visit(this, arg);
		mv.visitLabel(while_start);
		statementWhile.guard.visit(this, arg);
		mv.visitJumpInsn(IFNE, while_end);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
		mv.visitVarInsn(ALOAD, statementWrite.sourceDec.slotNumber);
		mv.visitVarInsn(ALOAD, statementWrite.destDec.slotNumber);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "write", RuntimeImageSupport.writeSig,false);
		//mv.visitVarInsn(ASTORE, statementWrite.destDec.slotNumber);
		return null;
		//throw new UnsupportedOperationException();
	}

}
