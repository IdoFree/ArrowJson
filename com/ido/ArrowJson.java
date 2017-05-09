package hss.isis.gtap.vbs.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ArrowJson {
	private static String ARRAY_START = "[";
	private static String ARRAY_END = "]";
	private static String OBJECT_END = "}";
	private static String OBJECT_START = "{";
	private static String COMMA = ",";
	private static String COLON = ":";
	
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
		appendQuotation(sb, clz.getSimpleName()).append(COLON);
		sb.append(OBJECT_START).append("\n");
		Field[] allFileds = clz.getDeclaredFields();
		for (Field f : allFileds) {
			f.setAccessible(true);
			Object val = f.get(obj);
			if(val == null){
				continue;
			}
			
			if (val instanceof String) {
				appendQuotation(appendQuotation(sb, f.getName()).append(COLON),
						(String) val).append(COMMA);
			} else if ( val instanceof Number) {
				appendQuotation(sb, f.getName()).append(COLON).append(val)
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
	public static <T> T jsonToObject(Class<T> clz,String jsonString) throws InstantiationException, IllegalAccessException{
		T result = clz.newInstance();
		for(;;){
			Token token = Lexemer.getToken(jsonString);
			if(token.type == TokenType.ID ){
				return null;
			}
		}
	}
	
	private static class Parser{
		
	}
	
	private static class Lexemer{
		private StringBuffer sb = new StringBuffer();
		private static final byte BRACE_START = '{';
		private static int temp = 0;
		
		public static Token getToken(String jsonStream){
			if(jsonStream == null || jsonStream == ""){
				return null;
			}
			char[] ins = jsonStream.toCharArray();
			for(int i = 0; i< ins.length ; i++){
				
				
				char current = ins[i] ;
				if(current == ' ' || current == '\t' || current == '\n' ){
					continue;
				}
				
				if(Character.isDigit(current)){
					do{
						temp = temp*10 + current;
						current = ins[++i] ;
					}while(Character.isDigit(current));
				}
				
			}
			
			return null;
		}
	}
	
	
	private static class Token{
		 TokenType type;
		 Object val;
		
	}
	
	private  enum TokenType{
		Keyword,ID
	}

	private static class Person {
		int age;
		String name;
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
	
	@Test
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
