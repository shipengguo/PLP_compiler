package cop5556fa18;

import cop5556fa18.PLPScanner.Token;

import java.util.ArrayList;
import java.util.List;

import cop5556fa18.PLPParser.SyntaxException;
import cop5556fa18.PLPScanner.Kind;
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
		matchEOF();
		return p;
	}
	
	/*
	 * Program -> Identifier Block
	 */
	public Program program() throws SyntaxException {
		Program p = null;
		Token first = t;
		String name = null;
		Block b =null;
		name = t.getString();
		match(Kind.IDENTIFIER);
		b = block();
		p = new Program(first,name,b);
		return p;
	}
	
	/*
	 * Block ->  { (  (Declaration | Statement) ; )* }
	 */
	
	Kind[] firstDec = { Kind.KW_int, Kind.KW_boolean, Kind.KW_float, Kind.KW_char, Kind.KW_string /* Complete this */ };
	Kind[] firstStatement = {Kind.KW_if, Kind.IDENTIFIER, Kind.KW_sleep, Kind.KW_print, Kind.KW_while/* Complete this */  };

	public Block block() throws SyntaxException {
		Block b =null;
		Token first = t;
		PLPASTNode d =null;
		PLPASTNode s = null;
		List<PLPASTNode> list = new ArrayList<PLPASTNode>();
		match(Kind.LBRACE);
		while (checkKind(firstDec) | checkKind(firstStatement)) {
	     if (checkKind(firstDec)) {
	    	 d = declaration();
			list.add(d);
		} else if (checkKind(firstStatement)) {
			 s = statement();
			list.add(s);
		}
			match(Kind.SEMI);
		}
		match(Kind.RBRACE);
		b = new Block(first, list);
		return b;
	}
	
	public Declaration declaration() throws SyntaxException {

		Declaration d = null;
		Token first = t;
		Kind type = null;
		Expression expression = null;
		List<String> names = new ArrayList<String>();
		type = type();
		String name = null;
		name = t.getString();
		names.add(name);
		match(Kind.IDENTIFIER);
		if (checkKind(Kind.OP_ASSIGN)) {
			match(Kind.OP_ASSIGN);
			expression = expression();
			d = new VariableDeclaration(first, type, name, expression);
			return d;
			}	
		else {
			boolean more = true;
			int count = 1;
			while (checkKind(Kind.COMMA)||checkKind(Kind.IDENTIFIER)) {
				if (checkKind(Kind.IDENTIFIER)) {
					names.add(t.getString());
					match(Kind.IDENTIFIER);
					more = true;
					count++;
				}
				else if (checkKind(Kind.COMMA)) {
					match(Kind.COMMA);
					more = false;
					count--;
				}
			}
			if (!more||count!=1) {
				System.out.println("illegal token;" + t);
				throw new SyntaxException(t, "Syntax Error");
				
			}
			if(names.size()==1) {
				d = new VariableDeclaration(first,type,name,expression);
				return d;
			}
			else {
				d = new VariableListDeclaration(first,type,names);
				return d;
			}
		}
	}

	public Statement statement() throws SyntaxException {
		Statement s = null;

		if (checkKind(Kind.KW_if)) {
			s = ifStatement();
		}
		else if (checkKind(Kind.IDENTIFIER)) {
			s = assignStatement();
		}
		else if (checkKind(Kind.KW_sleep)) {
			s = sleepStatement();
		}
		else if (checkKind(Kind.KW_print)) {
			s = printStatement();
		}
		else if (checkKind(Kind.KW_while)) {
			s = whileStatement();
		}
		else {
			System.out.println("illegal token:" + t);	
			throw new SyntaxException(t,"Syntax Error");
		}
		return s;
		//TODO
		
	}
	
	
	public Kind type() throws SyntaxException {
		Kind ty = null;
		if (checkKind(Kind.KW_int)) {
			ty = t.kind;
			match(Kind.KW_int);
		}
		else if (checkKind(Kind.KW_float)) {
			ty = t.kind;
			match(Kind.KW_float);
		}
		else if (checkKind(Kind.KW_boolean)) {
			ty = t.kind;
			match(Kind.KW_boolean);
		}
		else if (checkKind(Kind.KW_char)) {
			ty = t.kind;
			match(Kind.KW_char);
		}
		else if (checkKind(Kind.KW_string)) {
			ty = t.kind;
			match(Kind.KW_string);
		}
		else {
			System.out.println("illegal token:" + t);	
			throw new SyntaxException(t,"Syntax Error");
		}
		return ty;
	}
	
	public IfStatement ifStatement() throws SyntaxException {
		IfStatement ifstatement = null;
		Token first = t;
		Expression condition = null;
		Block b = null;
		if (checkKind(Kind.KW_if)) {
			match(Kind.KW_if);
			match(Kind.LPAREN);
			condition = expression();
			match(Kind.RPAREN);
			b = block();
			ifstatement = new IfStatement(first, condition, b);
		}
		else {
			System.out.println("illegal token:" + t);	
			throw new SyntaxException(t,"Syntax Error");
		}
		return ifstatement;
		
	}
	
	public WhileStatement whileStatement() throws SyntaxException {
		WhileStatement whilestatement = null;
		Token first = t;
		Expression condition = null;
		Block b = null;
		if (checkKind(Kind.KW_while)) {
			match(Kind.KW_while);
			match(Kind.LPAREN);
			condition = expression();
			match(Kind.RPAREN);
			b = block();
			whilestatement = new WhileStatement(first, condition, b);
		}
		else {
			System.out.println("illegal token:" + t);	
			throw new SyntaxException(t,"Syntax Error");
		}
		return whilestatement;
		
	}
	
	public AssignmentStatement assignStatement() throws SyntaxException {
		Token first = t;
		LHS lhs = null;
		Expression expression = null;
		AssignmentStatement assign = null;
		String name =null;
		if (checkKind(Kind.IDENTIFIER)) {
			name = t.getString();
			lhs = new LHS(first,name);
			match(Kind.IDENTIFIER);
			match(Kind.OP_ASSIGN);
			expression = expression();
			assign = new AssignmentStatement(first, lhs, expression);
		}
		else {
			System.out.println("illegal token:" + t);	
			throw new SyntaxException(t,"Syntax Error");
		}
		return assign;
		
	}
	
	public SleepStatement sleepStatement() throws SyntaxException {
		SleepStatement sleep = null;
		Token first = t;
		Expression time = null;
		if (checkKind(Kind.KW_sleep)) {
			match(Kind.KW_sleep);
			time = expression();
			sleep = new SleepStatement(first,time);
		}
		else {
			System.out.println("illegal token:" + t);	
			throw new SyntaxException(t,"Syntax Error");
		}
		return sleep;
	}
	
	public PrintStatement printStatement() throws SyntaxException {
		PrintStatement print = null;
		Token first = t;
		Expression expression = null;
		if (checkKind(Kind.KW_print)) {
			match(Kind.KW_print);
			expression = expression();
			print = new PrintStatement(first, expression);
		}
		else {
			System.out.println("illegal token:" + t);	
			throw new SyntaxException(t,"Syntax Error");
		}
		return print;
	}
	
	public Expression expression() throws SyntaxException {
		Token first = t;
		Expression e = null;
		Expression trueExpression = null;
		Expression falseExpression = null;
		e = OrExpression();
		if (checkKind(Kind.OP_QUESTION)) {
			match(Kind.OP_QUESTION);
			trueExpression = expression();
			match(Kind.OP_COLON);
			falseExpression = expression();
			e = new ExpressionConditional(first, e, trueExpression, falseExpression);
		}
		return e;	
	}
	
	
	public Expression OrExpression() throws SyntaxException {
		Token first = t;
		Expression left = null;
		Kind op =null;
		Expression right = null;
		left = AndExpression();
		while (checkKind(Kind.OP_OR)) {
			op = t.kind;
			match(Kind.OP_OR);
			right = AndExpression();
			left = new ExpressionBinary(first, left, op, right);
		}
		return left;
	}
	
	public Expression AndExpression() throws SyntaxException {
		Token first = t;
		Expression left = null;
		Kind op =null;
		Expression right = null;
		left = EqExpression();
		while (checkKind(Kind.OP_AND)) {
			op = t.kind;
			match(Kind.OP_AND);
			right = EqExpression();
			left = new ExpressionBinary(first, left, op, right);
		}
		return left;
	}
		
	public Expression EqExpression() throws SyntaxException {
		Token first = t;
		Expression left = null;
		Kind op =null;
		Expression right = null;
		left = RelExpression();
		while (checkKind(Kind.OP_EQ)||checkKind(Kind.OP_NEQ)) {
			if (checkKind(Kind.OP_EQ)) {
				op = t.kind;
				match(Kind.OP_EQ);
			}
			else if (checkKind(Kind.OP_NEQ)) {
				op = t.kind;
				match(Kind.OP_NEQ);
			}
			right = RelExpression();
			left = new ExpressionBinary(first, left, op, right);
		}
		return left;
	}
	
	public Expression RelExpression() throws SyntaxException {
		Token first = t;
		Expression left = null;
		Kind op =null;
		Expression right = null;
		left = AddExpression();
		while (checkKind(Kind.OP_GE)||checkKind(Kind.OP_LE)||checkKind(Kind.OP_GT)||checkKind(Kind.OP_LT)) {
			if (checkKind(Kind.OP_GE)) {
				op = t.kind;
				match(Kind.OP_GE);
			}
			else if (checkKind(Kind.OP_LE)) {
				op = t.kind;
				match(Kind.OP_LE);
			}
			else if (checkKind(Kind.OP_GT)) {
				op = t.kind;
				match(Kind.OP_GT);
			}
			else if (checkKind(Kind.OP_LT)) {
				op = t.kind;
				match(Kind.OP_LT);
			}
			right = AddExpression();
			left = new ExpressionBinary(first, left, op, right);
		}
		return left;
	}
	
	public Expression AddExpression() throws SyntaxException {
		Token first = t;
		Expression left = null;
		Kind op =null;
		Expression right = null;
		left = MultExpression();
		while (checkKind(Kind.OP_PLUS)||checkKind(Kind.OP_MINUS)) {
			if (checkKind(Kind.OP_PLUS)) {
				op = t.kind;
				match(Kind.OP_PLUS);
			}
			else if (checkKind(Kind.OP_MINUS)) {
				op = t.kind;
				match(Kind.OP_MINUS);
			}
			right = MultExpression();
			left = new ExpressionBinary(first, left, op, right);

		}
		return left;
	}
	
	public Expression MultExpression() throws SyntaxException {
		Token first = t;
		Expression left = null;
		Kind op =null;
		Expression right = null;
		left = PowerExpression();
		while (checkKind(Kind.OP_TIMES)||checkKind(Kind.OP_DIV)||checkKind(Kind.OP_MOD)) {
			if (checkKind(Kind.OP_TIMES)) {
				op = t.kind;
				match(Kind.OP_TIMES);
			}
			else if (checkKind(Kind.OP_DIV)) {
				op = t.kind;
				match(Kind.OP_DIV);
			}
			else if (checkKind(Kind.OP_MOD)) {
				op = t.kind;
				match(Kind.OP_MOD);
			}
			right = PowerExpression();
			left = new ExpressionBinary(first, left, op, right);
		}
		return left;
	}
	
	public Expression PowerExpression() throws SyntaxException {
		Token first = t;
		Expression left = null;
		Kind op =null;
		Expression right = null;
		left = UnaryExpression();
		if (checkKind(Kind.OP_POWER)) {
			op = t.kind;
			match(Kind.OP_POWER);
			right = PowerExpression();
			left = new ExpressionBinary(first, left, op, right);
		}
//		else {
//			op = null;
//			right = null;
//			e = new ExpressionBinary(first, left, op, right);
//		}
		return left;
	}
	
	Kind[] firstPrimary = {Kind.INTEGER_LITERAL, Kind.BOOLEAN_LITERAL, Kind.FLOAT_LITERAL, 
							Kind.CHAR_LITERAL, Kind.STRING_LITERAL, Kind.LPAREN, Kind.IDENTIFIER,
							Kind.KW_sin, Kind.KW_cos, Kind.KW_atan, Kind.KW_abs, Kind.KW_log, Kind.KW_int, Kind.KW_float };
	Kind[] firstFunction = {Kind.KW_sin, Kind.KW_cos, Kind.KW_atan, Kind.KW_abs, Kind.KW_log, Kind.KW_int, Kind.KW_float };

	public Expression UnaryExpression() throws SyntaxException {
		Expression e = null;
		Token first = t;
		Kind op = null;
		Expression ex = null;
		if (checkKind(Kind.OP_PLUS)) {
			op = t.kind;
			match(Kind.OP_PLUS);
			ex = UnaryExpression();
			e = new ExpressionUnary(first, op, ex);
		}
		else if (checkKind(Kind.OP_MINUS)) {
			op = t.kind;
			match(Kind.OP_MINUS);
			ex = UnaryExpression();
			e = new ExpressionUnary(first, op, ex);
		}
		else if (checkKind(Kind.OP_EXCLAMATION)) {
			op = t.kind;
			match(Kind.OP_EXCLAMATION);
			ex = UnaryExpression();
			e = new ExpressionUnary(first, op, ex);
		}
		else if (checkKind(firstPrimary)) {
			String name = null;
			int value;
			boolean value1;
			float value2;
			char text;
			String text1 = null;
			if (checkKind(Kind.INTEGER_LITERAL)) {
				String temp = t.getString();

				value = Integer.parseInt(temp);
				match(Kind.INTEGER_LITERAL);
				e = new ExpressionIntegerLiteral(first, value);
			}
			else if (checkKind(Kind.BOOLEAN_LITERAL)) {
				String temp = t.getString();
				value1 = Boolean.parseBoolean(temp);
				match(Kind.BOOLEAN_LITERAL);
				e = new ExpressionBooleanLiteral(first, value1);
			}
			else if (checkKind(Kind.FLOAT_LITERAL)) {
				String temp = t.getString();
				value2 = Float.parseFloat(temp);
				match(Kind.FLOAT_LITERAL);
				e = new ExpressionFloatLiteral(first, value2);
			}
			else if (checkKind(Kind.CHAR_LITERAL)) {
				String temp = t.getString();
				text = temp.charAt(1);
				match(Kind.CHAR_LITERAL);
				e = new ExpressionCharLiteral(first, text);
			}
			else if (checkKind(Kind.STRING_LITERAL)) {
				String temp = t.getString();
				text1 = temp.replace("\"","").replace("\"","");
				match(Kind.STRING_LITERAL);
				e = new ExpressionStringLiteral(first, text1);
			}
			else if (checkKind(Kind.LPAREN)) {
				match(Kind.LPAREN);
				e = expression();
				match(Kind.RPAREN);
			}
			else if (checkKind(Kind.IDENTIFIER)) {
				name = t.getString();
				name = name.replace("\"","").replace("\"","");
				match(Kind.IDENTIFIER);
				e = new ExpressionIdentifier(first, name);
				
			}
			else if (checkKind(firstFunction)) {
				e = Function();
			}
		}
		else {
			System.out.println("illegal token:" + t);	
			throw new SyntaxException(t,"Syntax Error");
		}
		return e;
	}
	
//	Kind[] firstFunction = {Kind.KW_sin, Kind.KW_cos, Kind.KW_atan, Kind.KW_abs, Kind.KW_log, Kind.KW_int, Kind.KW_float };
//	public Expression Primary() throws SyntaxException {
//		Expression e = null;
//		Token first = t;
//		String name = null;
//		int value;
//		boolean value1;
//		float value2;
//		char text;
//		String text1 = null;
//		if (checkKind(Kind.INTEGER_LITERAL)) {
//			String temp = t.getString();
//			value = Integer.parseInt(temp);
//			match(Kind.INTEGER_LITERAL);
//			e = new ExpressionIntegerLiteral(first, value);
//		}
//		else if (checkKind(Kind.BOOLEAN_LITERAL)) {
//			String temp = t.getString();
//			value1 = Boolean.parseBoolean(temp);
//			match(Kind.BOOLEAN_LITERAL);
//			e = new ExpressionBooleanLiteral(first, value1);
//		}
//		else if (checkKind(Kind.FLOAT_LITERAL)) {
//			String temp = t.getString();
//			value2 = Float.parseFloat(temp);
//			match(Kind.FLOAT_LITERAL);
//			e = new ExpressionFloatLiteral(first, value2);
//		}
//		else if (checkKind(Kind.CHAR_LITERAL)) {
//			String temp = t.getString();
//			text = temp.charAt(0);
//			match(Kind.CHAR_LITERAL);
//			e = new ExpressionCharLiteral(first, text);
//		}
//		else if (checkKind(Kind.STRING_LITERAL)) {
//			String temp = t.getString();
//			text1 = temp;
//			match(Kind.STRING_LITERAL);
//			e = new ExpressionStringLiteral(first, text1);
//		}
//		else if (checkKind(Kind.LPAREN)) {
//			match(Kind.LPAREN);
//			Expression();
//			match(Kind.RPAREN);
//		}
//		else if (checkKind(Kind.IDENTIFIER)) {
//			name = t.getString();
//			match(Kind.IDENTIFIER);
//			e = new ExpressionIdentifier(first, name);
//			
//		}
//		else if (checkKind(firstFunction)) {
//			Function();
//		}
//		else {
//			System.out.println("illegal token:" + t);	
//			throw new SyntaxException(t,"Syntax Error");
//		}
//		return e;
//	}
	
	public FunctionWithArg Function() throws SyntaxException {
		FunctionWithArg f = null;
		Token first = t;
		Kind name = null;
		Expression e =null;
		
		name = FunctionName();
		if (checkKind(Kind.LPAREN)) {
			match(Kind.LPAREN);
			e = expression();
			match(Kind.RPAREN);
			f = new FunctionWithArg(first, name, e);
		}
		else {
			System.out.println("illegal token:" + t);	
			throw new SyntaxException(t,"Syntax Error");
		}
		return f;
	}
	
	public Kind FunctionName() throws SyntaxException {
		Kind k =null;
		if (checkKind(Kind.KW_sin)) {
			k = t.kind;
			match(Kind.KW_sin);
		}
		else if (checkKind(Kind.KW_cos)) {
			k = t.kind;
			match(Kind.KW_cos);
		}
		else if (checkKind(Kind.KW_atan)) {
			k = t.kind;
			match(Kind.KW_atan);
		}
		else if (checkKind(Kind.KW_abs)) {
			k = t.kind;
			match(Kind.KW_abs);
		}
		else if (checkKind(Kind.KW_log)) {
			k = t.kind;
			match(Kind.KW_log);
		}
		else if (checkKind(Kind.KW_int)) {
			k = t.kind;
			match(Kind.KW_int);
		}
		else if (checkKind(Kind.KW_float)) {
			k = t.kind;
			match(Kind.KW_float);
		}
		else {
			System.out.println("illegal token:" + t);	
			throw new SyntaxException(t,"Syntax Error");
		}
		return k;
	}
	//TODO Complete all other productions

	protected boolean checkKind(Kind kind) {
		return t.kind == kind;
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
	 * @return
	 * @throws SyntaxException
	 */
	private void match(Kind kind) throws SyntaxException {
		if (checkKind(kind)) {
			t = scanner.nextToken();
			return;
		}
		//TODO  give a better error message!
		throw new SyntaxException(t,"Syntax Error");
	}

}
