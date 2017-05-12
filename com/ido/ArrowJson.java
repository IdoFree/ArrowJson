package hss.isis.gtap.vbs.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.impl.jam.internal.elements.ArrayClassImpl;
import org.junit.Assert;
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
	
	static public <T> T fromJson(String json,Class<T> clz) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException{
		Parser<T> p = new Parser<T>(json.toCharArray(),clz);
		T result = p.Object();
		return result;
		
	}
	
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
			} else if ( val instanceof Number || val instanceof Boolean) {
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
		StringBuffer result = new StringBuffer(in.subSequence(0, in.length()-1));
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
	
	private static String getNameFromArray( Object array){
		Class clz =  array.getClass();
		if(!(clz.isArray())){
			throw new IllegalArgumentException("not a array");
		}
		String arrayClassName =  clz.getName();
		return arrayClassName.substring(2,arrayClassName.length()-1);
		
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
		Class subClz;
		Class curClz;
		Object subResult;
		Object curObject;
		private static boolean isOriginalClz = true;
		private ArrayList listResult;
		public Parser(char[] jsonChars,Class<T> clz) throws InstantiationException, IllegalAccessException {
			super();
			this.jsonChars = jsonChars;
			lexemer = new Lexemer();
			if(clz.isArray()){
				listResult = new ArrayList<>();
			}else{
				this.result =clz.newInstance();
			}
			this.clz = clz;
			this.curClz = clz;
			this.curObject = result;
		}

		public T Object()throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException{
			expect(BRACE_START);
			getNextToken();
			if(look_ahead instanceof Str){
				Members();
				expect(BRACE_END);
			}else{
				expect(BRACE_END);
			}
			
			
			return result;
			
				
		}
		
		public void Members() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException{
			
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
		
		public void Pair() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException{
			String();expect(COLON);FieldValue();
		}
		
		public void String() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
			Field();
		}
		
		public void FieldValue() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InstantiationException{
			getNextToken();
			if(look_ahead instanceof Str){
				
				this.val = ((Str)look_ahead).val;
				if(curClz.isArray()){
					listResult.add(val);
				}else{
					f.setAccessible(true);
					f.set(curObject, val);
				}
			}else if(look_ahead instanceof Num){
				this.val = ((Num)look_ahead).val;
				if(curClz.isArray()){
					listResult.add(val);
				}else{
					f.setAccessible(true);
					f.set(curObject, val);
				}
			}else if(look_ahead instanceof Keyword){
				if(((Keyword)look_ahead).ch == '{'){
					moveToPreviousToken();
					subResult = curClz.newInstance();
					
					f.setAccessible(true);
					if(isOriginalClz){
						f.set(result, subResult);
						isOriginalClz = false;
					}else{
						f.set(curObject, subResult);
					}
					curObject = subResult;
					Object();
				}else if(((Keyword)look_ahead).ch == '['){
					//TODO handle array parsing
					Array();
					
				}else if(((Keyword)look_ahead).ch == TRUE){
					if(curClz.isArray()){
						listResult.add(val);
					}else{
						f.setAccessible(true);
						f.set(curObject, true);
					}
				}else if(((Keyword)look_ahead).ch == FALSE){
					if(curClz.isArray()){
						listResult.add(val);
					}else{
						f.setAccessible(true);
						f.set(curObject, false);
					}
				}else if(((Keyword)look_ahead).ch == ','){
					Members();
				}
			}else{
				throw new RuntimeException("syntax error : expecte field name ");
			
			}
		}
		
		
		
		
		public void Field() throws NoSuchFieldException, SecurityException{
			if(!(look_ahead instanceof Str)){
				throw new RuntimeException("syntax error : expecte field name ");
			}
			
			
			//handle array 
			if(this.curClz.isArray()){
//				Object instance = getNameFromArray(curClz);
//				listResult.add(instance);
				return;
			}
			this.f = this.curClz.getDeclaredField(((Str)look_ahead).val);
			if(this.f.getType() != String.class && !this.f.getType().isPrimitive() ){
				curClz = this.f.getType();
			}
			
		}
		
		public void Array() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InstantiationException{
			getNextToken();
			if(look_ahead instanceof Num ){
				moveToPreviousToken();
				FieldValue();
			}else if(look_ahead instanceof Keyword){
				if(((Keyword)look_ahead).ch == ','){
					FieldValue();
				}else{
					expect(BRACKET_END);
				}
			}
			/*if(curClz.isArray()){
				try {
					Object instance =Class.forName(getNameFromArray(curClz)) ;
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e.getMessage());
				}
			}*/
		}
		
		
		
		private void expect(char ch){
			getNextToken();
			if(look_ahead instanceof Keyword && ch  == ((Keyword)look_ahead).ch ){
				
			}else{
				throw new RuntimeException("syntax error : expecte char : "+ ch+ "but get "+ ((Keyword)look_ahead).ch);
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
				
				if(current == BRACE_START ||current == BRACE_END || current == BRACKET_START ||current == BRACKET_END||current == COLON ||current == COMMA_C   ){
					curentIndex++;
					return  new Keyword(current);
				}
				if(current == QUOTE){
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
							return new Keyword(TRUE);
						}else if(outString.equals("false")){
							return new Keyword(FALSE);
						}else{
							sb = new StringBuilder();
							return new Str(outString);
						}
						
					}else if(current == QUOTE){
						return new Str("");
					}else{
						throw new RuntimeException("syntax error, expect: \" ");
					}
				}
				
			}
			
			return null;
		}
	}
	
	
	private static class Token{
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
	

	public static class Person {
		int age;
		String name;
		String address;
		boolean male;
	}
	
	public static class Student {
		private String name;
	}
	
	public static class Room{
		String name;
		Person person;
		
	}
	
	@Test
	public void tsetParseArray() throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException, IllegalArgumentException{
		String s = " {\"nums\" :[2,3,4,5]}"; 
		char[] ins = s.toCharArray();
		Parser<String[]> parser = new Parser<>(ins, String[].class);
		String[] result = parser.Object();
		
		for(String string : result){
			System.out.println(string);
		}
	}
	
//	@Test
	public void testParser() throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException, IllegalArgumentException{
		String s = "{\"name\" : \"ido\",\"age\" : 10}"; 
		String s2 = "{\"name\" : \"ido\",\"age\" : 10,\"address\":\"bijiangcun \\\'yanjiangdonglu\", \"male\":\"true\"}"; 
		char[] ins = s2.toCharArray();
		Parser<Person> parser = new Parser<>(ins, Person.class);
		Person result = parser.Object();
		System.out.println(result.name);
		System.out.println(result.age);
		System.out.print(result.address);
		
	}
	
//	@Test
	public void testNestedObjectParsing() throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException, IllegalArgumentException{
		String s2 = "{\"name\" : \"room1\",\"person\":{\"name\" : \"ido\",\"age\" : 10,\"address\":\"bijiangcun \\\'yanjiangdonglu\", \"male\":\"true\"}}"; 
		char[] ins = s2.toCharArray();
		Parser<Room> parser = new Parser<>(ins, Room.class);
		Room result = parser.Object();
		System.out.println(result.name);
		System.out.println(result.person.name);
		System.out.println(result.person.age);
		System.out.println(result.person.address);
		System.out.println(result.person.male);
		
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
		}
	}
	
//	@Test
	public void testGetInstanceFromArray() throws ClassNotFoundException{
		Address[] addresses = new Address[5];
		Class clz =  addresses.getClass();
		System.out.println(clz.getName());
//		Object result = Class.forName("hss.isis.gtap.vbs.utils.ArrowJson$Address");
		Object result = Class.forName(getNameFromArray(addresses));
		System.out.println(getNameFromArray(addresses));
		
		
	}
	
	 
	
//	@Test
	public void testArrayClz() throws InstantiationException, IllegalAccessException {
		Class clz = Address.class;
		try {

			Address[] aObject = (Address[]) Array.newInstance(clz, 5); // 5 is
																		// length
			int length = Array.getLength(aObject); // will be 5
			for (int i = 0; i < length; i++) {
				Address ads = new Address();
				ads.line = "line" + i;
				Array.set(aObject, i, ads); // set your val here
			}
			for (Address address : (Address[]) aObject) {
				System.out.println(address.line);
			}
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

//	@Test
	public void testSingalObject() throws IllegalArgumentException, IllegalAccessException {
		Person p = new Person();
		p.age = 10;
		p.name = "dsa";

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