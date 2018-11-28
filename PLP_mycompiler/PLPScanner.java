/**
* Initial code for the Scanner
*/

package cop5556fa18;

import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Arrays;

import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPAST.LHS;

public class PLPScanner {
	
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {

		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}

		public int getPos() {
			return pos;
		}
	}
	
	public static enum Kind {
		IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, FLOAT_LITERAL,
		STRING_LITERAL, CHAR_LITERAL,
		KW_print        /* print       */,
		KW_sleep        /* sleep       */,
		KW_int          /* int         */,
		KW_float        /* float       */,
		KW_boolean      /* boolean     */,
		KW_if           /* if          */,
		KW_while 		/* while 	   */,
		KW_char         /* char        */,
		KW_string       /* string      */,
		KW_abs			/* abs 		   */,
		KW_sin			/* sin 		   */,
		KW_cos			/* cos 		   */, 
		KW_atan			/* atan        */,
		KW_log			/* log 		   */,
		OP_ASSIGN       /* =           */, 
		OP_EXCLAMATION  /* !           */,
		OP_QUESTION		/* ? 		   */,
		OP_EQ           /* ==          */,
		OP_NEQ          /* !=          */, 
		OP_GE           /* >=          */,
		OP_LE           /* <=          */,
		OP_GT           /* >           */,
		OP_LT           /* <           */,
		OP_AND			/* & 		   */, 
		OP_OR			/* | 		   */,
		OP_PLUS         /* +           */,
		OP_MINUS        /* -           */,
		OP_TIMES        /* *           */,
		OP_DIV          /* /           */,
		OP_MOD          /* %           */,
		OP_POWER        /* **          */, 
		LPAREN          /* (           */,
		RPAREN          /* )           */,
		LBRACE          /* {           */, 
		RBRACE          /* }           */,
		LSQUARE			/* [           */, 
		RSQUARE			/* ]           */, 
		SEMI            /* ;           */,
		OP_COLON		/* : 		   */,
		COMMA           /* ,           */,
		DOT             /* .           */,
		EOF				/* end of file */,
	}
	
	/**
	 * Class to represent Tokens.
	 *
	 */
	public class Token {
		public final Kind kind;
		public final int pos; // position of first character of this token in the input. Counting starts at 0
								// and is incremented for every character.
		public final int length; // number of characters in this token

		public Token(Kind kind, int pos, int length) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}
		
		/**
		 * Calculates and returns the line on which this token resides. The first line
		 * in the source code is line 1.
		 * 
		 * @return line number of this Token in the input.
		 */
		public int line() {
			return PLPScanner.this.line(pos) + 1;
		}

		/**
		 * Returns position in line of this token.
		 * 
		 * @param line.
		 *            The line number (starting at 1) for this token, i.e. the value
		 *            returned from Token.line()
		 * @return
		 */
		public int posInLine(int line) {
			return PLPScanner.this.posInLine(pos, line - 1) + 1;
		}

		/**
		 * Returns the position in the line of this Token in the input. Characters start
		 * counting at 1. Line termination characters belong to the preceding line.
		 * 
		 * @return
		 */
		public int posInLine() {
			return PLPScanner.this.posInLine(pos) + 1;
		}

		public String toString() {
			int line = line();
			return "[" + kind + "," +
			       String.copyValueOf(chars, pos, length) + "," +
			       pos + "," +
			       length + "," +
			       line + "," +
			       posInLine(line) + "]";
		}
		public String getString() {
			return String.copyValueOf(chars, pos ,length);
		}

		/**
		 * Since we override equals, we need to override hashCode, too.
		 * 
		 * See
		 * https://docs.oracle.com/javase/9/docs/api/java/lang/Object.html#hashCode--
		 * where it says, "If two objects are equal according to the equals(Object)
		 * method, then calling the hashCode method on each of the two objects must
		 * produce the same integer result."
		 * 
		 * This method, along with equals, was generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + pos;
			return result;
		}

		/**
		 * Override equals so that two Tokens are equal if they have the same Kind, pos,
		 * and length.
		 * 
		 * This method, along with hashcode, was generated by eclipse.
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (pos != other.pos)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is associated with.
		 * 
		 * @return
		 */
		private PLPScanner getOuterType() {
			return PLPScanner.this;
		}
	}
	
	/**
	 * Array of positions of beginning of lines. lineStarts[k] is the pos of the
	 * first character in line k (starting at 0).
	 * 
	 * If the input is empty, the chars array will have one element, the synthetic
	 * EOFChar token and lineStarts will have size 1 with lineStarts[0] = 0;
	 */
	int[] lineStarts;

	int[] initLineStarts() {
		ArrayList<Integer> lineStarts = new ArrayList<Integer>();
		int pos = 0;

		for (pos = 0; pos < chars.length; pos++) {
			lineStarts.add(pos);
			char ch = chars[pos];
			while (ch != EOFChar && ch != '\n' && ch != '\r') {
				pos++;
				ch = chars[pos];
			}
			if (ch == '\r' && chars[pos + 1] == '\n') {
				pos++;
			}
		}
		// convert arrayList<Integer> to int[]
		return lineStarts.stream().mapToInt(Integer::valueOf).toArray();
	}
	
	int line(int pos) {
		int line = Arrays.binarySearch(lineStarts, pos);
		if (line < 0) {
			line = -line - 2;
		}
		return line;
	}

	public int posInLine(int pos, int line) {
		return pos - lineStarts[line];
	}

	public int posInLine(int pos) {
		int line = line(pos);
		return posInLine(pos, line);
	}
	
	/**
	 * Sentinal character added to the end of the input characters.
	 */
	static final char EOFChar = 128;

	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;

	/**
	 * An array of characters representing the input. These are the characters from
	 * the input string plus an additional EOFchar at the end.
	 */
	final char[] chars;

	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;
	
	PLPScanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input string terminated with null char
		chars[numChars] = EOFChar;
		tokens = new ArrayList<Token>();
		lineStarts = initLineStarts();
	}
	
	private enum State {START,DIGITS,FLOAT_NUM,IN_IDENT,HAVE_ZEROS};  //TODO:  this is incomplete
	
	public PLPScanner scan() throws LexicalException {
		int pos = 0;
		State state = State.START;
		int startPos = 0;
		
		//TODO:  this is incomplete

		while (pos < chars.length) {
			char ch = chars[pos];
			switch(state) {
				case START: {
					startPos = pos;
					switch (ch) {
					case EOFChar: {
						tokens.add(new Token(Kind.EOF, startPos, 0));
						pos++; // next iteration will terminate loop
					}
					break;
					case ',': {
						tokens.add(new Token(Kind.COMMA, startPos, 1));
						pos++;
					}
					break;
					case '.': {/* float number */
						if (Character.isDigit(chars[pos+1])) {
							state = State.FLOAT_NUM;
						}
						else {
							error(pos, line(pos), posInLine(pos), "illegal char");

						}
					}
					break;
					case ':': {
						tokens.add(new Token(Kind.OP_COLON, startPos, 1));
						pos++;
					}
					break;
					case '[': {
						tokens.add(new Token(Kind.LSQUARE, startPos, 1));
						pos++;
					}
					break;
					case ']': {
						tokens.add(new Token(Kind.RSQUARE, startPos, 1));
						pos++;
					}
					break;
					case ';': {
						tokens.add(new Token(Kind.SEMI, startPos, 1));
						pos++;
					}
					break;
					case '}': {
						tokens.add(new Token(Kind.RBRACE, startPos, 1));
						pos++;
					}
					break;
					case '{': {
						tokens.add(new Token(Kind.LBRACE, startPos, 1));
						pos++;
					}
					break;
					case ')': {
						tokens.add(new Token(Kind.RPAREN, startPos, 1));
						pos++;
					}
					break;
					case '(': {
						tokens.add(new Token(Kind.LPAREN, startPos, 1));
						pos++;
					}
					break;
					case '%': {/* comment */
						int i =pos+1;
						if(chars[i]=='{') {
							while(true) {
								if (i==chars.length-1 && chars[i]=='}') {
									error(pos, line(i), posInLine(i), "illegal char");
								}
								else if (chars[i]=='%' && chars[i+1]=='{') {
									error(pos, line(i), posInLine(i), "illegal char");
								}
								else if (chars[i]!='}') {
									i++;
								}
								else {
									break;
								}
							}
							if (chars[i-1]!='%') {
								error(pos, line(i), posInLine(i), "illegal char");
								break;
							}
							pos = i+1;
						}
						else {
							tokens.add(new Token(Kind.OP_MOD, startPos, 1));
							pos++;
						}
					}
					
					break;
					case '/': {
						tokens.add(new Token(Kind.OP_DIV, startPos, 1));
						pos++;
					}
					break;
					case '*': {
						if (chars[pos+1] == '*') {/* * */
							tokens.add(new Token(Kind.OP_POWER, startPos, 2));
							pos = pos+2;
						}
						else {/* * */
							tokens.add(new Token(Kind.OP_TIMES, startPos, 1));
							pos++;
						}
					}
					break;
					case '-': {
						tokens.add(new Token(Kind.OP_MINUS, startPos, 1));
						pos++;
					}
					break;
					case '+': {
						tokens.add(new Token(Kind.OP_PLUS, startPos, 1));
						pos++;
					}
					break;
					case '|': {
						tokens.add(new Token(Kind.OP_OR, startPos, 1));
						pos++;
					}
					break;
					case '&': {
						tokens.add(new Token(Kind.OP_AND, startPos, 1));
						pos++;
					}
					break;
					case '<': {
						if (chars[pos+1] == '=') {/* <= */
							tokens.add(new Token(Kind.OP_LE, startPos, 2));
							pos = pos+2;
						}
					
						else {/* < */
							tokens.add(new Token(Kind.OP_LT, startPos, 1));
							pos++;
						}
					}
					break;
					case '>': {
						if (chars[pos+1] == '=') {/* >= */
							tokens.add(new Token(Kind.OP_GE, startPos, 2));
							pos = pos+2;
						}
					
						else {/* > */
							tokens.add(new Token(Kind.OP_GT, startPos, 1));
							pos++;
						}
					}
					break;
					case '!': {
						if (chars[pos+1] == '=') {/* != */
							tokens.add(new Token(Kind.OP_NEQ, startPos, 2));
							pos = pos+2;
						}
						else {/* ! */
							tokens.add(new Token(Kind.OP_EXCLAMATION, startPos, 1));
							pos++;
						}
					}
					break;
					case '=': {
						if (chars[pos+1] == '=') {/* == */
							tokens.add(new Token(Kind.OP_EQ, startPos, 2));
							pos = pos+2;
						}
						else {/* = */
							tokens.add(new Token(Kind.OP_ASSIGN, startPos, 1));
							pos++;
						}
					}
					break;
					case '?': {
						tokens.add(new Token(Kind.OP_QUESTION, startPos, 1));
						pos++;
					}
					break;
					
					case ' ': {
						pos++;
					}
					break;
					case '\n': {
						pos++;
					}
					break;
					case '\r': {
						pos++; 
					}
					break;
					case '\t': {
						pos++; 
					}
					break;
					case '\f': {
						pos++; 
					}
					break;
					case '\'': {
						
						if ( chars[pos+1] =='\'') {
							tokens.add(new Token(Kind.CHAR_LITERAL, startPos, pos - startPos+1));
							pos += 2;
						}
						else if ( chars[pos+1]!='\'' && chars[pos+2]=='\'') {
							tokens.add(new Token(Kind.CHAR_LITERAL, startPos, pos - startPos+2));
							pos += 3;
						}
					
						else {
							error(pos, line(pos), posInLine(pos), "illegal char");
						}
					}
					break;
					

					case '\"': {
						int count = 1;
						int i = pos;
						pos++;
						while (pos<chars.length+1) {
							if (chars[pos] == '\"') {
								count++;
								pos++;
								tokens.add(new Token(Kind.STRING_LITERAL, startPos, pos - startPos));
								break;
							}
							else {
								pos++;
							}
						}
						if (count != 2) error(pos, line(i), posInLine(i), "illegal char");
					}				
					break;
					case '_': {
						while (true) {
							if(chars[pos]=='_') {
							pos++;
							}
							else {
								break;
							}
						}
						
						if (Character.isJavaIdentifierStart(chars[pos])) {
							
							state = State.IN_IDENT;
						}
						else {
							error(pos, line(pos), posInLine(pos), "illegal char");
						}
					}
						default: {
							if (Character.isDigit(ch)&&ch!='0') {
								
								state = State.DIGITS;
							}
							else if (ch == '0') {
								
								System.out.println("input string HAVE_ZEROS");
								state = State.HAVE_ZEROS;
							}
							else if (Character.isJavaIdentifierStart(ch)) {
								
								state = State.IN_IDENT;	
							}
							else {
								error(pos, line(pos), posInLine(pos), "illegal char");
							}
						}
						break;	
					}//switch ch
				}//switch state
				break;

				case DIGITS: {
					
					while (Character.isDigit(chars[pos])) {
						pos++;
					}
					if (chars[pos]=='.') {
						
						state = State.FLOAT_NUM;
					}
					else {
						String string= String.copyValueOf(chars, startPos,pos - startPos); 
						int inte=Integer.valueOf(string);
						if(inte>2147483647)error(pos, line(pos), posInLine(pos), "illegal char");
						tokens.add(new Token(Kind.INTEGER_LITERAL, startPos, pos - startPos));
				    	state=State.START;
					}
				}
				break;

				case HAVE_ZEROS: {
					pos = pos+1;
					if (chars[pos]!='.') {
						tokens.add(new Token(Kind.INTEGER_LITERAL, startPos, pos-startPos));
						
						state = State.START;
					}
					else if (chars[pos]=='.' && Character.isDigit(chars[pos+1])) {
						
						state = State.FLOAT_NUM;
					}
					else {
						error(pos, line(pos), posInLine(pos), "illegal char");
					}
				}
				break;

				case FLOAT_NUM: {
					int i = pos;
					while (i<=chars.length) {
						i++;
						if (Character.isDigit(chars[i])) {
						}
						else {
							break;
						}
					}
					pos = i;
					String str = String.copyValueOf(chars, startPos, pos - startPos);
					float num = Float.parseFloat(str);
					/* here consider the saturation */
					if(num>3.4028235E38) {
						error(pos, line(pos), posInLine(pos), "illegal char");
					}
					tokens.add(new Token(Kind.FLOAT_LITERAL, startPos, pos - startPos));
					state = State.START;
				}
				break;
				case IN_IDENT: {
					String str = String.copyValueOf(chars, startPos, pos - startPos);
					if(Character.isJavaIdentifierStart(ch)||Character.isDigit(ch)) {
						pos++;
					}
					else {
						switch(str) {
							case "print": tokens.add(new Token(Kind.KW_print, startPos, pos - startPos)); break;
							case "sleep": tokens.add(new Token(Kind.KW_sleep, startPos, pos - startPos)); break;
							case "int" : tokens.add(new Token(Kind.KW_int, startPos, pos - startPos));break;
							case "float" : tokens.add(new Token(Kind.KW_float, startPos, pos - startPos));break;
							case "boolean" : tokens.add(new Token(Kind.KW_boolean, startPos, pos - startPos));break;
							case "if" : tokens.add(new Token(Kind.KW_if, startPos, pos - startPos));break;
							case "char" : tokens.add(new Token(Kind.KW_char, startPos, pos - startPos));break;									
							case "string" : tokens.add(new Token(Kind.KW_string, startPos, pos - startPos));break;						
							case "true" : tokens.add(new Token(Kind.BOOLEAN_LITERAL, startPos, pos - startPos));break;
							case "false" : tokens.add(new Token(Kind.BOOLEAN_LITERAL, startPos, pos - startPos));break;
							case "log" : tokens.add(new Token(Kind.KW_log, startPos, pos - startPos));break;
							case "atan" : tokens.add(new Token(Kind.KW_atan, startPos, pos - startPos));break;
							case "sin" : tokens.add(new Token(Kind.KW_sin, startPos, pos - startPos));break;
							case "cos" : tokens.add(new Token(Kind.KW_cos, startPos, pos - startPos));break;
							case "abs" : tokens.add(new Token(Kind.KW_abs, startPos, pos - startPos));break;
							case "while" : tokens.add(new Token(Kind.KW_while, startPos, pos - startPos));break;
							default: tokens.add(new Token(Kind.IDENTIFIER, startPos, pos - startPos));break;
						}
						str = "";
						state = State.START;
					}
				}
				break;
				default: {
					error(pos, 0, 0, "undefined state");
				}
			}// switch state
		} // while
		
		return this;
	}
	
	private void error(int pos, int line, int posInLine, String message) throws LexicalException {
		String m = (line + 1) + ":" + (posInLine + 1) + " " + message;
		throw new LexicalException(m, pos);
	}

	/**
	 * Returns true if the internal iterator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that the next
	 * call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition: hasTokens()
	 * 
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}

	/**
	 * Returns the next Token, but does not update the internal iterator. This means
	 * that the next call to nextToken or peek will return the same Token as
	 * returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition: hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}

	/**
	 * Resets the internal iterator so that the next call to peek or nextToken will
	 * return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens and line starts
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		sb.append("Line starts:\n");
		for (int i = 0; i < lineStarts.length; i++) {
			sb.append(i).append(' ').append(lineStarts[i]).append('\n');
		}
		return sb.toString();
	}

}