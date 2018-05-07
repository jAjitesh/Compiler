package cop5556sp18;
/* *
 * Initial code for SimpleParser for the class project in COP5556 Programming Language Principles 
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


import cop5556sp18.Scanner.Token;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.Expression;
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
import cop5556sp18.AST.LHS;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.Statement;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;
import cop5556sp18.Scanner.Kind;
import static cop5556sp18.Scanner.Kind.*;

import java.util.ArrayList;



public class Parser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}



	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

//****************************************
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}

	/*
	 * Program ::= Identifier Block
	 */
	public Program program() throws SyntaxException {
		Token a = t; 
		Token name = match(IDENTIFIER);
		
		Block b = block();
		return new Program(a,name,b);
	}
	
	/*
	 * Block ::=  { (  (Declaration | Statement) ; )* }
	 */
	
	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstStatement = {KW_input, KW_write, IDENTIFIER, KW_red, KW_blue, KW_green, KW_alpha, KW_while, KW_if, KW_show, KW_sleep};


	public Block block() throws SyntaxException{
		Token a = t;
		ArrayList<ASTNode> list = new ArrayList<ASTNode>();
		match(LBRACE);
		while (isKind(firstDec)|isKind(firstStatement)) {
	     if (isKind(firstDec)) {
			Declaration d = declaration();
			list.add(d);
		} else if (isKind(firstStatement)) {
			Statement s = statement();
			list.add(s);
		}
			match(SEMI);
		}
		match(RBRACE);
		return new Block(a,list);
	}
	
public Declaration declaration() throws SyntaxException {
		
		Token a = t;
		
		if (isKind(firstDec)) {
						
			if (isKind(KW_image)) {
				Token type = consume();
				Token name = match(IDENTIFIER);
				Expression width = null;
				Expression height = null;
				
				if (isKind(LSQUARE)) {
					match(LSQUARE);
					width = expression();
					match(COMMA);
					height = expression();
					match(RSQUARE);
				}	
				return new Declaration(a,type,name,width,height);
			}
			else {
				Token type = consume();
				Token name = match(IDENTIFIER);
				Expression width = null;
				Expression height = null;
				return new Declaration(a,type,name,width,height);
			}					
		}
		else {
			throw new SyntaxException(a,"Expected declaration token but encountered "+t);
		}
}
	
	Kind[] firstExpression = {OP_PLUS, OP_MINUS, OP_EXCLAMATION,INTEGER_LITERAL, BOOLEAN_LITERAL, FLOAT_LITERAL, IDENTIFIER, LPAREN, KW_sin, KW_cos, KW_atan,KW_abs,KW_log,KW_cart_x/* cart_x*/, KW_cart_y/* cart_y */, 
			KW_polar_a/* polar_a*/, KW_polar_r, KW_int/* int */, KW_float /* float */, KW_width /* width */, KW_height,KW_red, KW_blue, KW_green, KW_alpha, KW_Z, KW_default_width/* default_width */, KW_default_height,LPIXEL};
	
	public Expression expression() throws SyntaxException {
		Token a = t;
		if(isKind(firstExpression)){
			Expression or = orExpression();
			if(isKind(OP_QUESTION)){
				match(OP_QUESTION);
				Expression trueExp = expression();
				match(OP_COLON);
				Expression falseExp = expression();
				or = new ExpressionConditional(a,or,trueExp,falseExp);
			}
			return or;
		}
		else{
			throw new SyntaxException(t,"Expected Expression token but found "+ t);
		}
	}
	
	public Expression orExpression() throws SyntaxException {
		Token a = t;
		if(isKind(firstExpression)){
			Expression and = andExpression();
			while(isKind(OP_OR)){
				Token op = match(OP_OR);
				Expression and1 = andExpression();
				and = new ExpressionBinary(a, and, op, and1);
			}
			return and;
		}
		else{
			throw new SyntaxException(t,"Expected Expression token but found "+ t);
		}
	}
	
	public Expression andExpression() throws SyntaxException {
		Token a = t;
		if(isKind(firstExpression)){
			Expression eq = eqExpression();
			while(isKind(OP_AND)){
				Token op = match(OP_AND);
				Expression eq1 = eqExpression();
				eq = new ExpressionBinary(a, eq, op, eq1);
			}
			return eq;
		}
		else{
			throw new SyntaxException(t,"Expected Expression token but found "+ t);
		}
	}
	
	public Expression eqExpression() throws SyntaxException {
		Token a = t;
		if(isKind(firstExpression)){
			
			Expression rel = relExpression();
			Token op = null;
			while(isKind(OP_EQ) || isKind(OP_NEQ)){
				if(isKind(OP_EQ)){
					op = match(OP_EQ);	
				}else{
					op = match(OP_NEQ);
				}
				Expression rel1 = relExpression();
				rel = new ExpressionBinary(a, rel, op, rel1);
			}
			return rel;
		}
		else{
			throw new SyntaxException(t,"Expected Expression token but found "+ t);
		}
	}
	
	public Expression relExpression() throws SyntaxException {
		Token a = t;
		if(isKind(firstExpression)){
			//System.out.println("in eq");
			Token op = null;
			Expression add = addExpression();
			while(isKind(OP_LT) || isKind(OP_GT) || isKind(OP_GE) || isKind(OP_LE)){
				if(isKind(OP_LT)){
					op = match(OP_LT);	
				}
				else if(isKind(OP_GT)){
					op = match(OP_GT);	
				}
				else if(isKind(OP_GE)){
					op = match(OP_GE);	
				}
				else{
					op = match(OP_LE);
				}
				Expression add1 = addExpression();
				add = new ExpressionBinary(a, add, op, add1);
			}
			return add;
		}
		else{
			throw new SyntaxException(t,"Expected Expression token but found "+ t);
		}
	}
	
	public Expression addExpression() throws SyntaxException {
		Token a = t;
		Token op = null;
		if(isKind(firstExpression)){
			Expression mult = multExpression();
			while(isKind(OP_PLUS) || isKind(OP_MINUS)){
				if(isKind(OP_PLUS)){
					op = match(OP_PLUS);	
				}
				else{
					op = match(OP_MINUS);
				}
				Expression mult1 = multExpression();
				mult = new ExpressionBinary(a, mult, op, mult1);
			}
			return mult;
		}
		else{
			throw new SyntaxException(t,"Expected Expression token but found "+ t);
		}
	}
	
	public Expression multExpression() throws SyntaxException {
		Token a = t;
		Token op = null;
		if(isKind(firstExpression)){
			Expression pow = powerExpression();
			while(isKind(OP_TIMES) || isKind(OP_DIV) || isKind(OP_MOD)){
				if(isKind(OP_TIMES)){
					op = match(OP_TIMES);	
				}
				else if(isKind(OP_DIV)){
					op = match(OP_DIV);
				}
				else{
					op = match(OP_MOD);
				}
				Expression pow1 = powerExpression();
				pow = new ExpressionBinary(a, pow, op, pow1);
			}
			return pow;
		}
		else{
			throw new SyntaxException(t,"Expected Expression token but found "+ t);
		}
	}
	//******************** not LL1 check here when any error?
	public Expression powerExpression() throws SyntaxException {
		Token a = t;
		Token op = null;
		if(isKind(firstExpression)){
			Expression unary = unaryExpression();
			if(isKind(OP_POWER)){
				op = match(OP_POWER);
				Expression add = powerExpression();
				unary = new ExpressionBinary(a, unary, op, add);
			}
			return unary;
		}
		else{
			throw new SyntaxException(t,"Expected Expression token but found "+ t);
		}
	}
	
	public Expression unaryExpression() throws SyntaxException {
		Token a = t;
		Token op = null;
		if(isKind(firstExpression)){
			if(isKind(OP_PLUS)){
				op = match(OP_PLUS);
				Expression unary = unaryExpression();
				return new ExpressionUnary(a, op, unary);
			}else if(isKind(OP_MINUS)){
				op = match(OP_MINUS);
				Expression unary = unaryExpression();
				return new ExpressionUnary(a, op, unary);
			}else{
				return unaryExpressionNotPlusMinus();
			}
		}
		else{
			throw new SyntaxException(t,"Expected Expression token but found "+ t);
		}
	}
	Kind[] firstExpressionNotMinus = {OP_EXCLAMATION,INTEGER_LITERAL, BOOLEAN_LITERAL, FLOAT_LITERAL, IDENTIFIER, LPAREN, KW_sin, KW_cos, KW_atan,KW_abs,KW_log,KW_cart_x/* cart_x*/, KW_cart_y/* cart_y */, 
			KW_polar_a/* polar_a*/, KW_polar_r, KW_int/* int */, KW_float /* float */, KW_width /* width */, KW_height,KW_red, KW_blue, KW_green, KW_alpha, KW_Z, KW_default_width/* default_width */, KW_default_height,LPIXEL};
	
	public Expression unaryExpressionNotPlusMinus() throws SyntaxException {
		Token a = t;
		Token op = null;
		if(isKind(firstExpressionNotMinus)){
			if(isKind(OP_EXCLAMATION)){
				op = match(OP_EXCLAMATION);
				Expression unary = unaryExpression();
				return new ExpressionUnary(a, op, unary);
			}else{
				return primary();
			}
		}
		else{
			throw new SyntaxException(t,"Expected Expression token but found "+ t);
		}
	}
	Kind[] firstFunctionName = { KW_sin, KW_cos, KW_atan,KW_abs,KW_log,KW_cart_x/* cart_x*/, KW_cart_y/* cart_y */, 
			KW_polar_a/* polar_a*/, KW_polar_r, KW_int/* int */, KW_float /* float */, KW_width /* width */, KW_height,KW_red, KW_blue, KW_green, KW_alpha};
	
	public Expression primary() throws SyntaxException {
		Token a = t;
		if(isKind(firstExpressionNotMinus) && !isKind(OP_EXCLAMATION)){
			if(isKind(INTEGER_LITERAL)){
				Token intLit = match(INTEGER_LITERAL);
				return new ExpressionIntegerLiteral(a, intLit);
			}
			else if (isKind(BOOLEAN_LITERAL)){
				Token boolLit=match(BOOLEAN_LITERAL);
				return new ExpressionBooleanLiteral(a, boolLit);
			}
			else if (isKind(FLOAT_LITERAL)){
				Token floatLit = match(FLOAT_LITERAL);
				return new ExpressionFloatLiteral(a, floatLit);
			}
			else if (isKind(IDENTIFIER)){
				Token ident = match(IDENTIFIER);
				if(isKind(LSQUARE)){
					PixelSelector p = pixelSelector();
					return new ExpressionPixel(a,ident,p);
				}
				return new ExpressionIdent(a, ident);
			}
			else if (isKind(LPAREN)){
				match(LPAREN);
				Expression e= expression();
				match(RPAREN);
				return e;
			}
			else if(isKind(firstFunctionName)){
				return functionApplication();
		    }
			else if(isKind(KW_Z) || isKind(KW_default_width) || isKind(KW_default_height)){
				return predefinedName();
			}
			else if(isKind(LPIXEL)){
				return pixelConstructor();
			}
			else{
				throw new SyntaxException(t,"Expected Primary token but found "+ t);
			}
		}
		else{
			throw new SyntaxException(t,"Expected Primary token but found "+ t);
		}
	}	
	
	public ExpressionPixelConstructor pixelConstructor() throws SyntaxException{
		Token a = t;
		if(isKind(LPIXEL)){
			match(LPIXEL);
			Expression alpha = expression();
			match(COMMA);
			Expression red = expression();
			match(COMMA);
			Expression green = expression();
			match(COMMA);
			Expression blue = expression();
			match(RPIXEL);
			return new ExpressionPixelConstructor(a, alpha, red, green, blue);
		}
		else{
			throw new SyntaxException(t,"Expected pixelConstructor token but found "+ t);
		}
	}
	
	
	public ExpressionPredefinedName predefinedName() throws SyntaxException{
		Token a = t;
		Token name = null;
		if(isKind(KW_Z) || isKind(KW_default_width) || isKind(KW_default_height)){
			if(isKind(KW_Z)){
				name = match(KW_Z);
			}
			else if(isKind(KW_default_width)){
				name = match(KW_default_width);
			}
			else{
				name = match(KW_default_height);
			}
			return new ExpressionPredefinedName(a,  name);
		}
		else{
			throw new SyntaxException(t,"Expected PredefinedName token but found "+ t);
		}
	}
	
	public PixelSelector pixelSelector() throws SyntaxException{
		Token a = t;
		if(isKind(LSQUARE)){
			match(LSQUARE);
			Expression e1 = expression();
			match(COMMA);
			Expression e2 = expression();
			match(RSQUARE);
			return new PixelSelector(a, e1, e2);
		}
		else{
			throw new SyntaxException(t,"Expected pixelSelector token but found "+ t);
		}
	}
	
		public Expression functionApplication() throws SyntaxException{
			Token a = t;
			if(isKind(firstFunctionName)){
				Token name = functionName();
				if(isKind(LPAREN)){
					match(LPAREN);
					Expression e1 = expression();
					match(RPAREN);
					return new ExpressionFunctionAppWithExpressionArg(a,name, e1);
				}else{
					match(LSQUARE);
					Expression e0 = expression();
					match(COMMA);
					Expression e1 = expression();
					match(RSQUARE);
					return new ExpressionFunctionAppWithPixel(a,name, e0, e1);
				}
			}
			else{
				throw new SyntaxException(t,"Expected funcAppl error but found"+t);
			}
		}
		
		public Token functionName() throws SyntaxException {
			if(isKind(firstFunctionName)){
				Token name = null;
				if(isKind(KW_sin)){
					name = match(KW_sin);
				}
				else if (isKind(KW_cos)){
					name = match(KW_cos);
				}
				else if (isKind(KW_atan)){
					name = match(KW_atan);
				}
				else if (isKind(KW_abs)){
					name = match(KW_abs);
				}
				else if (isKind(KW_log)){
					name = match(KW_log);
				}
				else if(isKind(KW_cart_x)){
					name = match(KW_cart_x);
			    }
				else if(isKind(KW_cart_y)){
					name = match(KW_cart_y);
			    }
				else if(isKind(KW_polar_a)){
					name = match(KW_polar_a);
			    }
				else if(isKind(KW_polar_r)){
					name = match(KW_polar_r);
			    }
				else if(isKind(KW_int)){
					name = match(KW_int);
			    }
				else if(isKind(KW_float)){
					name = match(KW_float);
			    }
				else if(isKind(KW_width)){
					name = match(KW_width);
			    }
				else if(isKind(KW_height)){
					name = match(KW_height);
			    }
				else{
					name = color();
			    }
				return name;
			}
			else{
				throw new SyntaxException(t,"Syntax Error"+t);
			}
		}
			
		public Token color() throws SyntaxException {
			
			if(isKind(KW_red) || isKind(KW_blue) || isKind(KW_green) || isKind(KW_alpha)){
				Token color = null;
				if(isKind(KW_red)){
					color = match(KW_red);
				}
				else if (isKind(KW_blue)){
					color = match(KW_blue);
				}
				else if (isKind(KW_green)){
					color = match(KW_green);
				}
				else if (isKind(KW_alpha)){
					color = match(KW_alpha);
				}
				return color;
			}
			else{
				throw new SyntaxException(t,"Expected color token but found "+ t);
			}
		}
		
	
	public Token type() throws SyntaxException{
		 
		if(isKind(KW_int) ||isKind(KW_float) || isKind(KW_boolean) ||isKind(KW_image) || isKind(KW_filename)){
			Token type = null;
			if(isKind(KW_int)){
			type = match(KW_int);
		}
		else if(isKind(KW_float)){
			type = match(KW_float);
		}
		else if(isKind(KW_boolean)){
			type = match(KW_boolean);
		}
		else if(isKind(KW_image)){
			type = match(KW_image);
		}
		else if(isKind(KW_filename)){
			type = match(KW_filename);
		}
		return type;
		}
		else{
			throw new SyntaxException(t,"Expected type token but found "+ t);
		}
	}
	public Statement statement() throws SyntaxException {
		//TODO
		if(isKind(firstStatement)){
			if(isKind(KW_input)){
				return statementInput();
			}
			else if(isKind(KW_write)){
				return statementWrite();
			}
			else if(isKind(IDENTIFIER) || isKind(KW_red) || isKind(KW_blue) || isKind(KW_green) || isKind(KW_alpha)){
				return statementAssignment();
			}
			else if(isKind(KW_while)){
				return statementWhile();
			}
			else if(isKind(KW_if)){
				return statementIf();
			}
			else if(isKind(KW_show)){
				return statementShow();
			}
			else if(isKind(KW_sleep)){
				return statementSleep();
			}
			else{
				throw new SyntaxException(t,"Expected Statement token but found "+ t);
			}
		}
		else{
			throw new SyntaxException(t,"Expected Statement token but found "+ t);
	}
	}
	
	public StatementSleep statementSleep() throws SyntaxException{
		Token firstToken = t;
		if(isKind(KW_sleep)){
			match(KW_sleep);
			Expression duration = expression();
			return new StatementSleep(firstToken,duration);
		}
		else{
			throw new SyntaxException(t,"Syntax Error");
		}
	}
	
	public StatementShow statementShow() throws SyntaxException{
		Token firstToken = t;
		if(isKind(KW_show)){
			match(KW_show);
			Expression e = expression();
			return new StatementShow(firstToken,e);
		}
		else{
			throw new SyntaxException(t,"Syntax Error");
		}
	}
	
	public StatementIf statementIf() throws SyntaxException{
		Token firstToken = t;
		if(isKind(KW_if)){
			match(KW_if);
			match(LPAREN);
			Expression guard = expression();
			match(RPAREN);
			Block b = block();
			return new StatementIf(firstToken, guard,b);
		}
		else{
			throw new SyntaxException(t,"Syntax Error");
		}
	}
	
	public StatementWhile statementWhile() throws SyntaxException{
		Token firstToken = t;
		if(isKind(KW_while)){
			match(KW_while);
			match(LPAREN);
			Expression guard = expression();
			match(RPAREN);
			Block b = block();
			return new StatementWhile(firstToken, guard, b);
		}
		else{
			throw new SyntaxException(t,"Syntax Error");
		}
	}
	
	
	public StatementAssign statementAssignment() throws SyntaxException{
		Token firstToken = t;
		if(isKind(IDENTIFIER) || isKind(KW_red) || isKind(KW_blue) || isKind(KW_green) || isKind(KW_alpha)){
			LHS lhs = LHS();
			match(OP_ASSIGN);
			Expression e= expression();
			return new StatementAssign(firstToken,lhs, e);
		}
		else{
			throw new SyntaxException(t,"Syntax Error");
		}
	}
	
	public LHS LHS() throws SyntaxException{
		Token a = t;
		if(isKind(IDENTIFIER) || isKind(KW_red) || isKind(KW_blue) || isKind(KW_green) || isKind(KW_alpha)){
			if(isKind(IDENTIFIER)){
				Token name = match(IDENTIFIER);
				if(isKind(LSQUARE)){
					PixelSelector p = pixelSelector();
					return new LHSPixel(a, name, p);
				}
				return new LHSIdent(a,name);
			}
				else if(isKind(KW_red) || isKind(KW_blue) || isKind(KW_green) || isKind(KW_alpha)){
					Token color = color();
					match(LPAREN);
					Token name = match(IDENTIFIER);
					PixelSelector pixel = pixelSelector();
					match(RPAREN);
					return new LHSSample(a, name, pixel, color);
				}
				else{
					throw new SyntaxException(t,"Expected LHS token but found "+ t);
				}
			}			
		
		else{
			throw new SyntaxException(t,"Expected LHS token but found "+ t);
		}
	}
	
	public StatementWrite statementWrite() throws SyntaxException{
		Token a = t;
		if(isKind(KW_write)){
			match(KW_write);
			Token sourceName = match(IDENTIFIER);
			match(KW_to);
			Token destName = match(IDENTIFIER);
			return new StatementWrite(a, sourceName, destName);
		}
		else{
			throw new SyntaxException(t,"Syntax Error");
		}
	}
	
	
	public StatementInput statementInput() throws SyntaxException{
		Token a = t;
		if(isKind(KW_input)){
			match(KW_input);
			Token destName = match(IDENTIFIER);
			match(KW_from);
			match(OP_AT);
			Expression e0 = expression();
			return new StatementInput(a,destName, e0);
		}
		else{
			throw new SyntaxException(t,"Syntax Error");
		}
	}
	
	
	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}


	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}else{
			throw new SyntaxException(t,"Expected "+kind+" but encountered "+ t); //TODO  give a better error message!
		}
		
	}


	private Token consume() throws SyntaxException {
		Token tmp = t;
		if (isKind( EOF)) {
			throw new SyntaxException(t,"Unexpected EOF token"); //TODO  give a better error message!  
			//Note that EOF should be matched by the matchEOF method which is called only in parse().  
			//Anywhere else is an error. */
		}
		t = scanner.nextToken();
		return tmp;
	}


	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (isKind(EOF)) {
			return t;
		}
		throw new SyntaxException(t,"EOF expected -- error"); //TODO  give a better error message!
	}
	
	

}

