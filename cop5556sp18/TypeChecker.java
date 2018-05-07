package cop5556sp18;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;
import cop5556sp18.Types.Type;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.Expression;   // added by me
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

import static cop5556sp18.Types.Type.*;


public class TypeChecker implements ASTVisitor {


	TypeChecker() {
	}

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}
	
	SymbolTable symbolTable = new SymbolTable();
	
	
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
		for(ASTNode node: block.decsOrStatements){
			node.visit(this, arg);
		}
		symbolTable.leaveScope();
		return block;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
			if(declaration.width != null){
				declaration.width.visit(this, arg);	
			}
			if(declaration.height != null){
				declaration.height.visit(this, arg);
			}
	
		if(symbolTable.checkScope(declaration.name, declaration)){
			if(!(declaration.width == null || 
					(declaration.width.type.equals(INTEGER) && Types.getType(declaration.type).equals(IMAGE)))){
				//throw semantic error
				throw new SemanticException(declaration.firstToken, "Exception in visitDeclaration1");
			}
			if(!(declaration.height == null || (declaration.height.type.equals(INTEGER) && Types.getType(declaration.type).equals(IMAGE)))){
				//throw semantic error
				throw new SemanticException(declaration.firstToken, "Exception in visitDeclaration2");
				}
			if((declaration.width == null) != (declaration.height == null)){
				//throw semantic error
				throw new SemanticException(declaration.firstToken, "Exception in visitDeclaration3");
			}
			
			symbolTable.insert(declaration.name, declaration);
			return declaration;
			}
		
			
	throw new SemanticException(declaration.firstToken, "Exception in visitDeclaration");
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		statementWrite.sourceDec = symbolTable.lookup(statementWrite.sourceName);
		
		if(statementWrite.sourceDec != null){
			statementWrite.destDec = symbolTable.lookup(statementWrite.destName);
			if(statementWrite.destDec != null){
				if(Types.getType(statementWrite.sourceDec.type).equals(IMAGE) && Types.getType(statementWrite.destDec.type).equals(FILE)){
					return statementWrite;
				}
			}
		}
		
		throw new SemanticException(statementWrite.firstToken, "Exception in visitStatementWrite");
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		statementInput.e.visit(this, arg);
		
		statementInput.dec = symbolTable.lookup(statementInput.destName);
		if(statementInput.dec != null && statementInput.e.type.equals(INTEGER)){
			return statementInput;
		}
		throw new SemanticException(statementInput.firstToken, "Exception in visitStatementInput");
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		// TODO Auto-generated method stub
		pixelSelector.ex.visit(this, arg);
		pixelSelector.ey.visit(this, arg);
		if(pixelSelector.ex.type.equals(pixelSelector.ey.type)){
			if(pixelSelector.ex.type.equals(INTEGER) || pixelSelector.ex.type.equals(FLOAT) ){
				return pixelSelector;
			}
		}
		throw new SemanticException(pixelSelector.firstToken, "Exception in visitPixelSelector");
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionConditional.guard.visit(this, arg);
		expressionConditional.trueExpression.visit(this, arg);
		expressionConditional.falseExpression.visit(this, arg);
		if(expressionConditional.guard.type.equals(BOOLEAN) && expressionConditional.falseExpression.type.equals(expressionConditional.trueExpression.type)){
			expressionConditional.type = expressionConditional.trueExpression.type;
			return expressionConditional;
		}
		throw new SemanticException(expressionConditional.firstToken, "Exception in visitExpressionConditional");
	}
	
	public Type inferTypeForBinaryEx(Expression e0, Expression e1, Kind op,Token token) throws Exception{
		if(e0.type.equals(INTEGER) && e1.type.equals(INTEGER) && (op.equals(Kind.OP_PLUS) || op.equals(Kind.OP_AND)|| op.equals(Kind.OP_DIV)|| op.equals(Kind.OP_MOD)|| op.equals(Kind.OP_MINUS)|| op.equals(Kind.OP_OR)|| op.equals(Kind.OP_POWER)|| op.equals(Kind.OP_TIMES))){
			return INTEGER;
		}
		if(e0.type.equals(FLOAT) && e1.type.equals(FLOAT) &&(op.equals(Kind.OP_PLUS) || op.equals(Kind.OP_DIV)|| op.equals(Kind.OP_MINUS)||  op.equals(Kind.OP_POWER)|| op.equals(Kind.OP_TIMES))){
			return FLOAT;
		}
		if(e0.type.equals(FLOAT) && e1.type.equals(INTEGER) &&(op.equals(Kind.OP_PLUS) || op.equals(Kind.OP_DIV)|| op.equals(Kind.OP_MINUS)||  op.equals(Kind.OP_POWER)|| op.equals(Kind.OP_TIMES))){
			return FLOAT;
		}
		if(e0.type.equals(INTEGER) && e1.type.equals(FLOAT) &&(op.equals(Kind.OP_PLUS) || op.equals(Kind.OP_DIV)|| op.equals(Kind.OP_MINUS)||  op.equals(Kind.OP_POWER)|| op.equals(Kind.OP_TIMES))){
			return FLOAT;
		}
		if(e0.type.equals(BOOLEAN) && e1.type.equals(BOOLEAN) &&(op.equals(Kind.OP_AND) || op.equals(Kind.OP_OR))){
			return BOOLEAN;
		}
		if(e0.type.equals(INTEGER) && e1.type.equals(INTEGER) &&(op.equals(Kind.OP_AND) || op.equals(Kind.OP_OR))){
			return INTEGER;
		}
		if(e0.type.equals(INTEGER) && e1.type.equals(INTEGER) &&(op.equals(Kind.OP_EQ) || op.equals(Kind.OP_LT) || op.equals(Kind.OP_NEQ) || op.equals(Kind.OP_GE) || op.equals(Kind.OP_LE) || op.equals(Kind.OP_GT) )){
			return BOOLEAN;
			
		}
		if(e0.type.equals(FLOAT) && e1.type.equals(FLOAT) &&(op.equals(Kind.OP_EQ) || op.equals(Kind.OP_LT) || op.equals(Kind.OP_NEQ) || op.equals(Kind.OP_GE) || op.equals(Kind.OP_LE) || op.equals(Kind.OP_GT) )){
			return BOOLEAN;	
		}
		if(e0.type.equals(BOOLEAN) && e1.type.equals(BOOLEAN) &&(op.equals(Kind.OP_EQ) || op.equals(Kind.OP_LT) || op.equals(Kind.OP_NEQ) || op.equals(Kind.OP_GE) || op.equals(Kind.OP_LE) || op.equals(Kind.OP_GT) )){
			return BOOLEAN;
		}
		
		//System.out.println("$$$$$$$$$$$$$$$$$$$$$$");
		
		throw new SemanticException(token, "Exception in expressionBinary");
		
		

	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionBinary.leftExpression.visit(this, arg);
		expressionBinary.rightExpression.visit(this, arg);
		expressionBinary.type = inferTypeForBinaryEx(expressionBinary.leftExpression, expressionBinary.rightExpression, expressionBinary.op, expressionBinary.firstToken);
		
		//System.out.println("#######################");
		return expressionBinary;
		
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionUnary.expression.visit(this, arg);
		expressionUnary.type = expressionUnary.expression.type;
		return expressionUnary;
		
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionIntegerLiteral.type = Type.INTEGER;
		return expressionIntegerLiteral;
		
	}

	@Override
	public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionBooleanLiteral.type = Type.BOOLEAN;
		return expressionBooleanLiteral;
		
	}

	@Override
	public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionPredefinedName.type = Type.INTEGER;
		return expressionPredefinedName;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionFloatLiteral.type = Type.FLOAT;
		return expressionFloatLiteral;
		//throw new UnsupportedOperationException();
	}
	
	public Type inferredTypeFunctionApp(Kind function, Expression e, Token token) throws Exception{ // check thiss ************
		if(e.type.equals(INTEGER) && (function.equals(Kind.KW_alpha) || function.equals(Kind.KW_blue) || function.equals(Kind.KW_green) || function.equals(Kind.KW_red) || function.equals(Kind.KW_abs))){
			return INTEGER;
		}
		if(e.type.equals(FLOAT) && (function.equals(Kind.KW_sin) || function.equals(Kind.KW_cos) || function.equals(Kind.KW_atan) || function.equals(Kind.KW_log) || function.equals(Kind.KW_abs))){
			return FLOAT;
		}
		if(e.type.equals(IMAGE) && (function.equals(Kind.KW_width) || function.equals(Kind.KW_height))){
			return INTEGER;
		}
		if(e.type.equals(INTEGER) && (function.equals(Kind.KW_float))){
			return FLOAT;
		}
		if(e.type.equals(FLOAT) && (function.equals(Kind.KW_float))){
			return FLOAT;
		}
		if(e.type.equals(FLOAT) && (function.equals(Kind.KW_int))){
			return INTEGER;
		}
		if(e.type.equals(INTEGER) && (function.equals(Kind.KW_int))){
			return INTEGER; 
		}
		throw new SemanticException(token, "Exception in visitExpressionFunctionAppWithExpressionArg");
		
	}
	
	
	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg, Object arg)
			throws Exception {
		
		// TODO Auto-generated method stub
		expressionFunctionAppWithExpressionArg.e.visit(this, arg);
		expressionFunctionAppWithExpressionArg.type = inferredTypeFunctionApp(expressionFunctionAppWithExpressionArg.function, expressionFunctionAppWithExpressionArg.e, expressionFunctionAppWithExpressionArg.firstToken);
		//System.out.println(expressionFunctionAppWithExpressionArg.type);
		return expressionFunctionAppWithExpressionArg;
				
		
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionFunctionAppWithPixel.e0.visit(this, arg);
		expressionFunctionAppWithPixel.e1.visit(this, arg);
		if(expressionFunctionAppWithPixel.name.equals(Kind.KW_cart_x) || expressionFunctionAppWithPixel.name.equals(Kind.KW_cart_y)){
			if(expressionFunctionAppWithPixel.e0.type.equals(FLOAT) && expressionFunctionAppWithPixel.e1.type.equals(FLOAT)){
				expressionFunctionAppWithPixel.type = Type.INTEGER; //check thisss
				return expressionFunctionAppWithPixel;
			}
		}
		else if(expressionFunctionAppWithPixel.name.equals(Kind.KW_polar_a) || expressionFunctionAppWithPixel.name.equals(Kind.KW_polar_r)){
			if(expressionFunctionAppWithPixel.e0.type.equals(INTEGER) && expressionFunctionAppWithPixel.e1.type.equals(INTEGER)){
				expressionFunctionAppWithPixel.type = Type.FLOAT;
				return expressionFunctionAppWithPixel;
			}
		}
		throw new SemanticException(expressionFunctionAppWithPixel.firstToken, "Exception in VisitExpressionFunctionAppWithPixel");
	}

	@Override
	public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionPixelConstructor.alpha.visit(this, arg);
		expressionPixelConstructor.blue.visit(this, arg);
		expressionPixelConstructor.green.visit(this, arg);
		expressionPixelConstructor.red.visit(this, arg);
		if(expressionPixelConstructor.alpha.type.equals(INTEGER) && expressionPixelConstructor.blue.type.equals(INTEGER) && expressionPixelConstructor.green.type.equals(INTEGER) && expressionPixelConstructor.red.type.equals(INTEGER)){
			expressionPixelConstructor.type = Type.INTEGER;
			return expressionPixelConstructor;
		}
		throw new SemanticException(expressionPixelConstructor.firstToken, "Exception in visitExpressionPixelConstructor");
		
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		 
		statementAssign.lhs.visit(this, arg);
		statementAssign.e.visit(this, arg);
		
		if(statementAssign.lhs.type.equals(statementAssign.e.type)){
			//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$");
			return statementAssign;
		}
			throw new SemanticException(statementAssign.firstToken, "Exception in visitStatementAssign");

		
		// Doubt here
		//return statementAssign;
		//throw new SemanticException(statementAssign.firstToken, "Exception in visitStatementAssign");
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementShow.e.visit(this, arg);	
	if(statementShow.e.type.equals(INTEGER) || statementShow.e.type.equals(BOOLEAN) || statementShow.e.type.equals(FLOAT) || statementShow.e.type.equals(IMAGE)){
		return statementShow;
	}
	throw new SemanticException(statementShow.firstToken, "Exception in visitStatementShow");
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionPixel.pixelSelector.visit(this, arg);
		expressionPixel.dec = symbolTable.lookup(expressionPixel.name);
		if(expressionPixel.dec != null && expressionPixel.dec.type.equals(Kind.KW_image)){
			expressionPixel.type = Type.INTEGER;
			return expressionPixel;
		}
		throw new SemanticException(expressionPixel.firstToken, "Exception in visitExpressionPixel");
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionIdent.dec = symbolTable.lookup(expressionIdent.name);
		if(expressionIdent.dec != null){
			expressionIdent.type = Types.getType(expressionIdent.dec.type);
			return expressionIdent;
		}
		throw new SemanticException(expressionIdent.firstToken, "Exception in visitExpressionIdent");
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
		// TODO Auto-generated method stub
		lhsSample.pixelSelector.visit(this, arg);
		lhsSample.dec = symbolTable.lookup(lhsSample.name);
		if(lhsSample.dec != null && Types.getType(lhsSample.dec.type).equals(IMAGE)){
			lhsSample.type = Type.INTEGER;
			return lhsSample;
		}
		throw new SemanticException(lhsSample.firstToken, "Exception in visitLHSSample");
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {
		// TODO Auto-generated method stub
		lhsPixel.pixelSelector.visit(this, arg);
		lhsPixel.dec = symbolTable.lookup(lhsPixel.name);
		if(lhsPixel.dec != null && Types.getType(lhsPixel.dec.type).equals(IMAGE)){
			lhsPixel.type = Type.INTEGER;
			return lhsPixel;
		}

		throw new SemanticException(lhsPixel.firstToken, "Exception in visitLHSPixel");
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
		// TODO Auto-generated method stub
		lhsIdent.dec = symbolTable.lookup(lhsIdent.name);
		if(lhsIdent.dec != null){
			lhsIdent.type = Types.getType(lhsIdent.dec.type);
			return lhsIdent;
		}
		throw new SemanticException(lhsIdent.firstToken, "Exception in visitLHSIdent");
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementIf.b.visit(this, arg);
		statementIf.guard.visit(this, arg);
		
		if(statementIf.guard.type.equals(BOOLEAN)){
			return statementIf;
		}
	
		throw new SemanticException(statementIf.firstToken, "Exception in visitStatementIf");
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementWhile.b.visit(this, arg);
		statementWhile.guard.visit(this, arg);
		if(statementWhile.guard.type.equals(BOOLEAN)){
			return statementWhile;
		}
		
		throw new SemanticException(statementWhile.firstToken, "Exception in visitStatementWhile");
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementSleep.duration.visit(this, arg);
		if(statementSleep.duration.type.equals(INTEGER)){
			return statementSleep;
		}
		throw new SemanticException(statementSleep.firstToken, "Exception in visitStatementSleep");
	}


}
