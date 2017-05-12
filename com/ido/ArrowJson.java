package hss.isis.gtap.vbs.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.junit.Test;
public class ArrowJson {
	private static String ARRAY_START = "[";
	private static String ARRAY_END = "]";
	private static String OBJECT_END = "}";
	private static String OBJECT_START = "{";
	private static String COMMA = ",";
	private static String COLON_S = ":";
	
	private static final char BRACE_START = '{';
	private static final char BRACE_END = '}';
	private static final char BRACKET_START = '[';
	private static final char BRACKET_END = ']';
	private static final char QUOTE = '"';
	private static final char COLON = ':';
	private static final char TRUE = 'T';
	private static final char FALSE = 'F';
	private static final char COMMA_C = ',';
	
	static public String toJson(Object obj) throws IllegalArgumentException,
			IllegalAccessException {
		
		
		Class clz = obj.getClass();
		if(clz.isArray() || obj instanceof List ){
			StringBuffer sb = new StringBuffer();
			sb.append(ARRAY_START);
			Object[] objArray ;
			List<Object> objList ;
			if(clz.isArray()){
				objArray = (Object[]) obj;
				for(Object o : objArray){
					sb.append(getObjectJson(o)).append(COMMA);
				}
			}else{
				objList = (List<Object>) obj;
				for(Object o : objList){
					sb.append(getObjectJson(o)).append(COMMA);
				}
			}
			
			sb.append(ARRAY_END);
			return sb.toString();
		}
		
		return OBJECT_START+getObjectJson(obj)+OBJECT_END;
	}

	private static String getObjectJson(Object obj)
			throws IllegalAccessException {
		Class clz = obj.getClass();
		StringBuffer sb = new StringBuffer();
		appendQuotation(sb, clz.getSimpleName()).append(COLON_S);
		sb.append(OBJECT_START).append("\n");
		Field[] allFileds = clz.getDeclaredFields();
		for (Field f : allFileds) {
			f.setAccessible(true);
			Object val = f.get(obj);
			if(val == null){
				continue;
			}
			
			if (val instanceof String) {
				appendQuotation(appendQuotation(sb, f.getName()).append(COLON_S),
						(String) val).append(COMMA);
			} else if ( val instanceof Number) {
				appendQuotation(sb, f.getName()).append(COLON_S).append(val)
						.append(COMMA);
			}else {
				sb.append((getObjectJson(val))).append(COMMA);
			}
		}
		
		sb = removeLastComma(sb);
		sb.append("\n").append(OBJECT_END);
		return sb.toString();
	}
	
	private static StringBuffer removeLastComma(StringBuffer in){
		StringBuffer result = new StringBuffer(in.subSequence(0, in.length()-2));
		return result;
	}
	
	private static void println(Object in){
		System.out.print(in);
	}
	
	/**
	 * append " to field name 
	 * @param in
	 * @param name
	 * @return
	 */
	static private StringBuffer appendQuotation(StringBuffer in, String name) {
		in.append("\"").append(name).append("\"");
		return in;
	}
	
	/**
	 * 
	 * @param clz the target object type to create
	 * @param jsonString the json string 
	 * @return the initialized target object
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T jsonToObject(Class<T> clz,String jsonString) throws InstantiationException, IllegalAccessException{
		T result = clz.newInstance();
		
		Parser parser = new Parser(jsonString.toCharArray(),clz);
		char[] ins = jsonString.toCharArray();
		return result;
		
	}
	
	private static class Parser<T>{
		private static Token look_ahead = null;
		private char[] jsonChars;
		Lexemer lexemer;
		private boolean moved;
		T  result  ;
		Class<T> clz;
		Field f;
		Object val;
		public Parser(char[] jsonChars,Class<T> clz) throws InstantiationException, IllegalAccessException {
			super();
			this.jsonChars = jsonChars;
			lexemer = new Lexemer();
			this.result =clz.newInstance();
			this.clz = clz;
		}

		public T Object()throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
			expect(BRACE_START);
			getNextToken();
			if(look_ahead instanceof Str){
				Members();
				expect(BRACE_END);
			}else{
				expect(BRACE_END);
			}
			
			/*getNextToken();
			if(look_ahead instanceof Keyword ){
				switch (((Keyword)look_ahead).ch) {
				case '{':
					String();expect(COLON);FieldValue();
					getNextToken();
					if( ((Keyword)look_ahead).ch == ',' ){
						String();
						expect(COLON);
						FieldValue();
					}else{
						if(((Keyword)look_ahead).ch != '}' ){
							throw new RuntimeException("syntax error : expecte char : "+ BRACE_END);
						}
					}
					break;
				case ',':
					expect(BRACE_END);
					break;
				default:
					throw new RuntimeException("syntax error ");
				}
			}*/
			
			return result;
			
				
		}
		
		public void Members() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
			
			if(look_ahead instanceof Str){
				Pair();
			}
			getNextToken();
			if(look_ahead instanceof Keyword){
				if(((Keyword) look_ahead).ch == ','){
					
					getNextToken();
					Members();
				}
			}
			moveToPreviousToken();
			
		}
		
		public void Pair() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
			String();expect(COLON);FieldValue();
		}
		
		public void String() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
			Field();
		}
		
		public void FieldValue() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
			getNextToken();
			if(look_ahead instanceof Str){
				
				this.val = ((Str)look_ahead).val;
				f.setAccessible(true);
				f.set(result, val);
			}else if(look_ahead instanceof Num){
				this.val = ((Num)look_ahead).val;
				f.setAccessible(true);
				f.set(result, val);
			}else if(look_ahead instanceof Keyword){
				if(((Keyword)look_ahead).ch == '{'){
					Object();
				}else if(((Keyword)look_ahead).ch == TRUE){
					this.val = ((Num)look_ahead).val;
					f.setAccessible(true);
					f.set(result, true);
				}else if(((Keyword)look_ahead).ch == FALSE){
					this.val = ((Num)look_ahead).val;
					f.setAccessible(true);
					f.set(result, false);
				}else if(((Keyword)look_ahead).ch == ','){
					String();expect(COLON);FieldValue();
				}
			}else{
				throw new RuntimeException("syntax error : expecte field name ");
			
			}
		}
		
		
		public void Number(){
			
		}
		
		public void Field() throws NoSuchFieldException, SecurityException{
//			getNextToken();
			if(!(look_ahead instanceof Str)){
				throw new RuntimeException("syntax error : expecte field name ");
			}
			
			this.f = this.clz.getDeclaredField(((Str)look_ahead).val);
			
		}
		
		public void Array(){
			for(;;){
				expect(BRACKET_START);expect(BRACKET_END);
			}
		}
		
		public void Identity(){
			for(;;){
				expect(QUOTE);expect(QUOTE);
			}
		}
		
		
		private void expect(char ch){
			getNextToken();
			if(look_ahead instanceof Keyword && ch  == ((Keyword)look_ahead).ch ){
				
			}else{
				throw new RuntimeException("syntax error : expecte char : "+ ch);
			}
			
		}
		
		private void getNextToken(){
			look_ahead = lexemer.getToken(jsonChars);
			moved = false;
		}
		
		private void moveToPreviousToken(){
			if(!moved){
				lexemer.moveAhead();
				moved = true;
			}
		}
		
		
	}
	
	private static class Lexemer{
		private StringBuilder sb = new StringBuilder();
		
		private static int temp = 0;
		private static int curentIndex = 0;
		
		private Stack<Token> previousTokens = new Stack<>();
		private Token previousToken ;
		
		public  Token getPreviousToken(){
			return previousToken;
		}
		
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
					previousToken = new Num(result);
					return previousToken;
				}
				
				if(current == BRACE_START ||current == BRACE_END || current == BRACKET_START ||current == BRACKET_END||current == COLON ||current == COMMA_C   ){
					curentIndex++;
					previousToken = new Keyword(current);
					return previousToken;
				}
				if(current == QUOTE){
					current = ins[++curentIndex] ;
					if(Character.isLetter(current)){
						do{
							sb.append(current);
							current = ins[++curentIndex] ;
						}while(Character.isLetter(current) || current == ' ' || current == '\\'  || current == '\n'|| current == '\b'|| current == '\f'|| current == '\r'|| current == '\t'|| current == '\'');
						if(current != '"'){
							throw new RuntimeException("syntax error, expect: \" ");
						}
						++curentIndex;//skip this "
						String outString = sb.toString();
						sb = new StringBuilder();
						previousToken = new Str(outString);
						return previousToken;
						
					}else if(current == QUOTE){
						previousToken = new Str("");
						return previousToken;
					}else{
						throw new RuntimeException("syntax error, expect: \" ");
					}
				}
				
			}
			
			return null;
		}
	}
	
	
	private static class Token{
//		 TokenType type;
		public Token() {
			super();
		}
		
	}
	
	private static class Keyword extends Token{
		private char ch;
		public Keyword(char ch) {
			this.ch = ch;
		}

		
		
	}
	
	private static class Num extends Token{
		private int val;

		public Num(int val) {
			this.val = val;
		}
		
	}
	
	private static class Str extends Token{
		private String val;

		public Str(String val) {
			this.val = val;
		}

		
		
	}
	
	private  enum TokenType{
		NUM("number"),STR("String"),KEYWORD("keyword");
		
		private String description;

		private TokenType(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

		
		
		
		
	}

	public static class Person {
		int age;
		String name;
		String address;
	}
	
	public static class Student {
		private String name;
	}
	
	@Test
	public void testParser() throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException, IllegalArgumentException{
		String s = "{\"name\" : \"ido\",\"age\" : 10}"; 
		String s2 = "{\"name\" : \"ido\",\"age\" : 10,\"address\":\"bijiangcun \\\'yanjiangdonglu\"}"; 
		char[] ins = s2.toCharArray();
		Parser<Person> parser = new Parser<>(ins, Person.class);
		Person result = parser.Object();
		System.out.println(result.name);
		System.out.println(result.age);
		System.out.print(result.address);
		
	}
	
//	@Test
	public void testLexer(){
		Lexemer lexemer = new Lexemer();
		String s = "{\"name\" : \"ido\"}"; 
		char[] ins = s.toCharArray();
		while(true){
			Token token = lexemer.getToken(ins);
			if(token == null){
				return;
			}
//			String put = "Token type : "+token.type.getDescription()+", Token value " +String.valueOf(token.val);
//			System.out.println( put);
		}
	}

//	@Test
	public void testSingalObject() throws IllegalArgumentException, IllegalAccessException {
		Person p = new Person();
		p.age = 10;
//		p.name = nu;

		System.out.print(ArrowJson.toJson(p));
	}
//	@Test
	public void  testArray() throws IllegalArgumentException, IllegalAccessException{
		Person[] persons = new Person[10];
		for(int j = 0 ; j< 10; j++){
			persons[j] = new Person();
		}
		int  i = 0;
		for(Person p : persons){
			p.age = i;
			p.name = "Ido"+i;
			i++;
		}
		
		println(ArrowJson.toJson(persons));
	}
	
//	@Test
	public void testList() throws IllegalArgumentException, IllegalAccessException{
		ArrayList<Person> list = new ArrayList<Person>(10);
		for(int j = 0 ; j< 10; j++){
			list.add(new Person());
		}
		int  i = 0;
		for(Person p : list){
			p.age = i;
			p.name = "Ido"+i;
			i++;
		}
		
		println(ArrowJson.toJson(list));
	}
	private static class Address{
		String line;
		int		homeNumber;
	}
	
	private static class Department{
		Person person;
		String name;
		Address address;
	}
	
//	@Test
	public void testOjectNested() throws IllegalArgumentException, IllegalAccessException{
		Department department = new Department();
		department.person = new Person();
		department.person.age = 10;
//		department.person.name = "ido";
		department.name = "department name";
		department.address = new Address();
		department.address.homeNumber = 100;
		department.address.line = "bijiang cun yanjiang lu";
		println(ArrowJson.toJson(department));
//		println(department.getClass().isMemberClass());
	}
}