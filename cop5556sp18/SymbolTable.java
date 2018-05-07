package cop5556sp18;

import java.util.ArrayList;
import java.util.HashMap;

import cop5556sp18.AST.Declaration;

public class SymbolTable {
	int  curr_scope, next_scope;
	
	public class Values {
		Declaration declaration;
		int scope_number;
		
		public Values() {
		    this.declaration = null;
		    this.scope_number = 0;
		  }
		
		public Values(Declaration declaration,int scope_number) {
	    this.declaration = declaration;
	    this.scope_number = scope_number;
	  }
	}
		
		HashMap<String,ArrayList<Values>> table;
		ArrayList<Integer> scope_stack;
		
		public SymbolTable() {
			table =  new HashMap<String,ArrayList<Values>>();
			scope_stack = new ArrayList<Integer>();
			this.next_scope = 1;
			this.curr_scope = 0;
			scope_stack.add(0);		
			}
		

		public void enterScope(){
			curr_scope = next_scope;
			next_scope++;
			scope_stack.add(new Integer(curr_scope));
		}
		
		public void leaveScope(){
			scope_stack.remove(scope_stack.size()-1);
			curr_scope = scope_stack.get(scope_stack.size()-1);
		}
		
		public void insert(String identifier, Declaration declaration){
			
			ArrayList<Values> entries = table.get(identifier);
			if (entries == null){
				entries = new ArrayList<Values>();
				table.put(identifier, entries);
			}
				entries.add(new Values(declaration,curr_scope));			
		}
		
		public boolean checkScope(String identifier, Declaration declaration){
			ArrayList<Values> entries = table.get(identifier);
			if(entries == null){
				return true;
			}
			for(int i=0;i<entries.size();i++){
				if(entries.get(i).scope_number == curr_scope){
					return false;
				}
			}
			return true;
		}
		
		public Declaration lookup(String identifier){
			
			ArrayList<Values> entries = table.get(identifier);
			if(entries == null){
				return null;
			}
			for(int j=scope_stack.size() - 1; j >=0; j--){
				for(int i=0; i<entries.size(); i++){
					if (scope_stack.get(j) == entries.get(i).scope_number){
						return entries.get(i).declaration;
					}
				}
			}
			return null;
		}
	}

