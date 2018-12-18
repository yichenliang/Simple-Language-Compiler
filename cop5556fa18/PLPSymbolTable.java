// Yichen Liang    p5   Nov 5 11:59pm

package cop5556fa18;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;

import cop5556fa18.PLPAST.Declaration;

public class PLPSymbolTable {

	Stack<Integer> scope_stack = new Stack<>();  // keep records of scope number
	Hashtable<String, ArrayList<Pair>> hash = new Hashtable<>();  // store identifier with scope number
	int currentScope, nextScope;
	
	public PLPSymbolTable() {
		this.scope_stack = new Stack<Integer>();
		this.hash = new Hashtable<String, ArrayList<Pair>>();
		this.currentScope = 0;
		this.nextScope = 1;
		scope_stack.push(currentScope);
	}
	
	public void enterScope() {
		currentScope = nextScope++;
		scope_stack.push(currentScope);
	}
	
	public void addDec(String ident, Declaration dec) {
		if (hash.containsKey(ident)) {
			ArrayList<Pair> l = hash.get(ident);
			l.add(new Pair(currentScope, dec));
			hash.put(ident, l);
		} else {
			ArrayList<Pair> l = new ArrayList<>();
			l.add(new Pair(currentScope, dec));
			hash.put(ident, l);
		}
	}
	
	public void closeScope() {
		scope_stack.pop();
		currentScope = scope_stack.peek();
	}
	
	public Declaration lookup(String ident) {
		ArrayList<Pair> l = hash.get(ident);
		if (l == null) {
			return null;
		}
		
		Declaration dec = null;
		int delta = Integer.MAX_VALUE;
		for (Pair p: l) {
			if (p.getKey() <= currentScope && currentScope - p.getKey() <= delta) {
				if (scope_stack.contains(p.getKey())) {
					dec = p.getValue();
					delta = currentScope - p.getKey();
				}
			}
		}
		return dec;
	}
	
	public boolean duplicate(String name) {
		ArrayList<Pair> l = hash.get(name);
		if (l == null) {
			return false;
		}
		
		for (Pair p: l) {
			if (p.getKey() == currentScope) {
				return true;
			}
		}
		return false;
	}
	
	class Pair{
		int i;  //current scope
		Declaration d;
		
		public Pair(int i, Declaration d) {
			this.i = i;
			this.d = d;
		}
		
		public int getKey() {
			return i;
		}
		
		public Declaration getValue() {
			return d;
		}
	}
}
