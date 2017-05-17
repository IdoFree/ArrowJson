package hss.isis.gtap.vbs.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.impl.jam.internal.elements.ArrayClassImpl;
import org.junit.Assert;
import org.junit.Test;

import hss.isis.gtap.vbs.utils.Lexemer.Token;
public class ArrowJson {
	
	
	
	
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
			sb.append(Constants.ARRAY_START);
			Object[] objArray ;
			List<Object> objList ;
			if(clz.isArray()){
				objArray = (Object[]) obj;
				for(Object o : objArray){
					sb.append(getObjectJson(o)).append(Constants.COMMA);
				}
			}else{
				objList = (List<Object>) obj;
				for(Object o : objList){
					sb.append(getObjectJson(o)).append(Constants.COMMA);
				}
			}
			
			sb.append(Constants.ARRAY_END);
			return sb.toString();
		}
		
		return Constants.OBJECT_START+getObjectJson(obj)+Constants.OBJECT_END;
	}

	private static String getObjectJson(Object obj)
			throws IllegalAccessException {
		Class clz = obj.getClass();
		StringBuffer sb = new StringBuffer();
		appendQuotation(sb, clz.getSimpleName()).append(Constants.COLON_S);
		sb.append(Constants.OBJECT_START).append("\n");
		Field[] allFileds = clz.getDeclaredFields();
		for (Field f : allFileds) {
			f.setAccessible(true);
			Object val = f.get(obj);
			if(val == null){
				continue;
			}
			
			if (val instanceof String) {
				appendQuotation(appendQuotation(sb, f.getName()).append(Constants.COLON_S),
						(String) val).append(Constants.COMMA);
			} else if ( val instanceof Number || val instanceof Boolean) {
				appendQuotation(sb, f.getName()).append(Constants.COLON_S).append(val)
						.append(Constants.COMMA);
			}else {
				sb.append((getObjectJson(val))).append(Constants.COMMA);
			}
		}
		
		sb = removeLastComma(sb);
		sb.append("\n").append(Constants.OBJECT_END);
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
	
	
	private static String getNameFromArray( Object array){
		Class clz =  array.getClass();
		if(!(clz.isArray())){
			throw new IllegalArgumentException("not a array");
		}
		String arrayClassName =  clz.getName();
		return arrayClassName.substring(2,arrayClassName.length()-1);
		
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
	
//	@Test
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
		System.out.println(result.address);
		System.out.println(result.male);
		
	}
	
	@Test
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