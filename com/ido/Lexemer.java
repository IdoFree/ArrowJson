package hss.isis.gtap.vbs.utils;

public class Lexemer {
	
	private StringBuilder sb = new StringBuilder();
	
	private static int temp = 0;
	private static int curentIndex = 0;
	
	public  void moveAhead(){
		 curentIndex--;
	}
	
	/**
	 * 
	 * @param ins : the json char array
	 * @return the next token of the json stream
	 */
	public  Token getToken(char[] ins){
		if(ins.length ==  0 ){
			return null;
		}
		for(; curentIndex< ins.length ; curentIndex++){
			char current = ins[curentIndex] ;
			if(current == ' ' || current == '\t' || current == '\n' ){
				continue;
			}
			
			if(Character.isDigit(current)){
				do{
					int curenttDigit = Integer.valueOf(String.valueOf(current));
					
					temp = temp*10 + curenttDigit;
					current = ins[++curentIndex] ;
				}while(Character.isDigit(current));
				int result = temp;
				temp = 0;
				return new Num(result);
			}
			
			if(current == Constants.BRACE_START ||current == Constants.BRACE_END || current == Constants.BRACKET_START ||current == Constants.BRACKET_END||current == Constants.COLON ||current == Constants.COMMA_C   ){
				curentIndex++;
				return  new Keyword(current);
			}
			if(current == Constants.QUOTE){
				current = ins[++curentIndex] ;
				if(Character.isLetter(current)){
					do{
						sb.append(current);
						current = ins[++curentIndex] ;
					}while(Character.isLetter(current) ||Character.isDigit(current) || current == ' ' || current == '\\'  || current == '\n'|| current == '\b'|| current == '\f'|| current == '\r'|| current == '\t'|| current == '\'');
					if(current != '"'){
						throw new RuntimeException("syntax error, expect: \" "+"but get "+current);
					}
					++curentIndex;//skip this "
					String outString = sb.toString();
					if(outString.equals("true")){
						return new Keyword(Constants.TRUE);
					}else if(outString.equals("false")){
						return new Keyword(Constants.FALSE);
					}else{
						sb = new StringBuilder();
						return new Str(outString);
					}
					
				}else if(current == Constants.QUOTE){
					return new Str("");
				}else{
					throw new RuntimeException("syntax error, expect: \" ");
				}
			}
			
		}
		
		return null;
	}
	
	
	public static class Token{
		public Token() {
			super();
		}
		
	}
	
	public static class Keyword extends Token{
		public char ch;
		public Keyword(char ch) {
			this.ch = ch;
		}

		
		
	}
	
	public static class Num extends Token{
		public int val;

		public Num(int val) {
			this.val = val;
		}
		
	}
	
	public static class Str extends Token{
		public String val;

		public Str(String val) {
			this.val = val;
		}

		
		
	}

}
