//package cop5556fa18;
//
//
//
//
//import cop5556fa18.*;
//import java.util.Stack;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//import cop5556fa18.PLPAST.*;
//
//public class SymbolTable {
//	
//	
//	//TODO  add fields
//	class STAttribute {
//		int s;
//		Declaration dec;
//		public STAttribute(int s, Declaration dec) {
//			this.s=s;
//			this.dec=dec;
//		}		
//	}
//	
//	HashMap<String, ArrayList<STAttribute>> entry;
//	Stack<Integer> scope;
//	int c_scope, n_scope;
//
//	/** 
//	 * to be called when block entered
//	 */
//	public void enterScope(){
//		//TODO:  IMPLEMENT THIS
//		c_scope = n_scope;
//		n_scope++;
//		scope.push(c_scope);		
//	}
//	
//	
//	/**
//	 * leaves scope
//	 */
//	public void closeScope(){
//		//TODO:  IMPLEMENT THIS
//		scope.pop();
//		c_scope = scope.peek();
//	}
//	
//	public boolean insert(String ident, Declaration dec){
//		//TODO:  IMPLEMENT THIS
//		if(entry.containsKey(ident)) {
//			ArrayList<STAttribute> aList = entry.get(ident);
//			for(int i=0;i<aList.size();i++) {
//				STAttribute listEntry = aList.get(i);
//				if(listEntry.s==c_scope) {
//					return false;
//				}
//			}
//			aList.add(new STAttribute(c_scope,dec));
//			return true;
//		}
//		ArrayList<STAttribute> newAttribList = new ArrayList<STAttribute>();
//		STAttribute newAttrib = new STAttribute(c_scope,dec);
//		newAttribList.add(newAttrib);
//		entry.put(ident, newAttribList);		
//		return true;
//	}
//	
//	public Declaration lookup(String ident){
//		//TODO:  IMPLEMENT THIS
//		
//		if(entry.containsKey(ident)){
//			ArrayList<STAttribute> attribList = entry.get(ident);
//			Declaration decIdent=null;
//			
//			for(int i=0; i<attribList.size(); i++){
//				STAttribute listEntry = attribList.get(i);				
//				if(listEntry.s<=c_scope){
//					if(scope.contains(listEntry.s)){
//						decIdent=listEntry.dec;
//					}
//				}
//				else{
//					break;
//				}
//			}
//			return decIdent;
//		}
//		return null;
//	}
//		
//	public SymbolTable() {
//		//TODO:  IMPLEMENT THIS
//		this.entry = new HashMap<String,ArrayList<STAttribute>>();
//		this.scope = new Stack<Integer>();
//		this.c_scope = 0;
//		this.scope.push(this.c_scope);
//		this.n_scope=1;
//	}
//
//
//	@Override
//	public String toString() {
//		//TODO:  IMPLEMENT THIS
//		
//		return "currentScope="+c_scope;
//	}
//	
//	
//
//
//}
package cop5556fa18;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import cop5556fa18.PLPAST.Declaration;

public class SymbolTable {
	int current_scope;
	int next_scope;
	Stack<Integer> scope_stack = new Stack<>();
	
	public class thing{
		int current_scope_thing;
		Declaration dec_thing = null;
		
		public thing(int current_scope, Declaration dec) {
			this.current_scope_thing = current_scope;
			this.dec_thing = dec; 
		}
	}
	
	
	public void enterScope(){   
		current_scope = next_scope++; 
		scope_stack.push(current_scope);
	}

	public void closeScope(){  
		scope_stack.pop();
		if(!scope_stack.isEmpty()) current_scope = scope_stack.peek();
	}
	
	List<thing> list = new ArrayList<>();
	HashMap<String, List<thing>> map = new HashMap<>();
	

	public boolean insert(String ident, Declaration dec) {
		List<thing> temp = new ArrayList<>();
		thing t = new thing(current_scope, dec);
		if(map.containsKey(ident)) {
			temp = map.get(ident);
			for(thing e : temp) {
				if(e.current_scope_thing==current_scope) {
					return false; //we already have the scope number, dont need to put a new one
				}
			}
		}
		temp.add(t);
		map.put(ident, temp);
		return true;
		
	}
	
	
	public Declaration lookup(String ident) {
		if(!map.containsKey(ident)) {
			return null;
		}
		else {
			List<Integer> list = new ArrayList<>();
			for(thing e : map.get(ident)) {
				if(e.current_scope_thing<=current_scope) {
					list.add(e.current_scope_thing);
				}
			}
			if(list.size()==0) return null;
			int max = 0;
			for(int can : list){
				if(can > max){
					max = can;
				}
			}

			
			for(thing e : map.get(ident)) {
				if(e.current_scope_thing==max) {
					return e.dec_thing;
				}
			}
		}
		return null;
	}
	
	public SymbolTable() {
		this.scope_stack.push(0);
		current_scope = 0;
		next_scope = 1;
	}
}


