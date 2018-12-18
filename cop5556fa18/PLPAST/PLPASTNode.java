package cop5556fa18.PLPAST;

import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPTypes;

public abstract class PLPASTNode {
	
	final public Token firstToken;
	
	//public boolean isDec;
	public PLPTypes.Type type;
	public Declaration dec;

	public PLPASTNode(Token firstToken) {
		super();
		this.firstToken = firstToken;
	}
	
	public PLPTypes.Type getType(){
		return type;
	} 
	
	public void setType(PLPTypes.Type t) {
		type = t;
	}
	
	public Declaration getDec() {
		return dec;
	}
	
	public void setDec(Declaration d) {
		dec = d;	
	}
	
	public abstract Object visit(PLPASTVisitor v, Object arg) throws Exception;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((firstToken == null) ? 0 : firstToken.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PLPASTNode other = (PLPASTNode) obj;
		if (firstToken == null) {
			if (other.firstToken != null)
				return false;
		} else if (!firstToken.equals(other.firstToken))
			return false;
		return true;
	}

}
