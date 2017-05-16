package hss.isis.gtap.vbs.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;

import hss.isis.gtap.vbs.utils.Lexemer.Keyword;
import hss.isis.gtap.vbs.utils.Lexemer.Num;
import hss.isis.gtap.vbs.utils.Lexemer.Str;
import hss.isis.gtap.vbs.utils.Lexemer.Token;


public class Parser<T> {
	
	
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
		expect(Constants.BRACE_START);
		getNextToken();
		if(look_ahead instanceof Str){
			Members();
			expect(Constants.BRACE_END);
		}else{
			expect(Constants.BRACE_END);
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
		String();expect(Constants.COLON);FieldValue();
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
//				getNextToken();
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
				
			}else if(((Keyword)look_ahead).ch == Constants.TRUE){
				if(curClz.isArray()){
					listResult.add(val);
				}else{
					f.setAccessible(true);
					f.set(curObject, true);
				}
			}else if(((Keyword)look_ahead).ch == Constants.FALSE){
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
//			Object instance = getNameFromArray(curClz);
//			listResult.add(instance);
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
		}
		
		
		
		if(look_ahead instanceof Keyword){
			if(((Keyword)look_ahead).ch == ','){
				FieldValue();
			}else{
				expect(Constants.BRACKET_END);
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
	
	public void Elements(){
		getNextToken();
		
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
