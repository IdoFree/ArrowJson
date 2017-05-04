 public static class ArrowJson{
    	static public  String toJson(Object obj) throws IllegalArgumentException, IllegalAccessException{
    		StringBuffer sb = new StringBuffer();
    		sb.append("{ ");
    		Class clz =  obj.getClass();
    		Field[] allFileds = clz.getDeclaredFields();
    		for(Field f : allFileds){
    			f.setAccessible(true);
    			Object val = f.get(obj);
    			 if ( val instanceof String ) {
    				 appendQuotation(appendQuotation(sb,f.getName()).append(" : "),(String) val).append(",");
				}else if ( val instanceof Integer || val instanceof Float || val instanceof Double || val instanceof Number){
					appendQuotation(sb,f.getName()).append(" : ").append(val).append(",");
				}
    		}
    		sb.append(" }");
    		return sb.toString();
    	}
    	
    	
    	static private StringBuffer appendQuotation(StringBuffer in,String name){
    		in.append("\"").append(name).append("\"");
    		return in ;
    	}
    }