package hss.isis.gtap.vbs.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.apache.poi.util.ArrayUtil;

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
		getNextToken();
		expect(Constants.BRACE_START);
		Members();
		expect(Constants.BRACE_END);
		moveToPreviousToken();
		
		return result;
		
			
	}
	
	public void Members() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException{
		
		if(look_ahead instanceof Str){
			Pair();
		}
		getNextToken();
		while(look_ahead instanceof Keyword && ((Keyword) look_ahead).ch == ','){
			expect(',');
			if(look_ahead instanceof Str){
				Pair();
			}
			getNextToken();
			
		}
		
	}
	
	public void Pair() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException{
		String();
		expect(Constants.COLON);
		FieldValue();
	}
	
	public void String() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		if(!(look_ahead instanceof Str)){
			throw new RuntimeException("syntax error : expecte field name ");
		}
		
		//handle array 
		if(this.curClz.isArray()){
//			this.curClz.getDeclaredFields();
//			this.f = this.curClz.getDeclaredField(((Str)look_ahead).val);
			getNextToken();
			return;
		}
		this.f = this.curClz.getDeclaredField(((Str)look_ahead).val);
		if(this.f.getType() != String.class && !this.f.getType().isPrimitive() ){
			curClz = this.f.getType();
		}
		getNextToken();
	}
	
	public void FieldValue() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InstantiationException{
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
	
	
	
	public void Array() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InstantiationException{
		expect('[');
		Elements();
		if(this.f == null){
			result =  (T) listResult.toArray();
			return ;
		}
		this.f.set(curObject, listResult);
		expect(']');
		moveToPreviousToken();
	}
	
	public void Elements() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InstantiationException{
		FieldValue();
		getNextToken();
		while(look_ahead instanceof Keyword  && ((Keyword)look_ahead).ch == Constants.COMMA_C){
			expect(',');
			FieldValue();
			getNextToken();
		}
		
		
	}
	
	
	
	private void expect(char ch){
		if(look_ahead instanceof Keyword && ch  == ((Keyword)look_ahead).ch ){
			getNextToken();
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
