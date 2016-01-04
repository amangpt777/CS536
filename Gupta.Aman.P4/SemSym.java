import java.util.List;

public class SemSym {
    private String type;
    
    public SemSym(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public String toString() {
        return type;
    }
}


class StructSym extends SemSym {
	
    private IdNode structType;  
    
    public StructSym(IdNode id) {
        super(id.getStrVal());
        structType = id;
    }
    public IdNode getStructType() {
        return structType;
    }    
}

class FuncSym extends SemSym {
    private String returnType;
    private int numParams;
    private List<String> paramTypes;
    
    public FuncSym(String type, int numparams) {
        super(type);
        returnType = type;
        numParams = numparams;
    }

    public void addFormals(List<String> L) {
        paramTypes = L;
    }
    
    public String getReturnType() {
        return returnType;
    }

    public int getNumParams() {
        return numParams;
    }

    public List<String> getParamTypes() {
        return paramTypes;
    }

    public String toString() {
        String str = "";
        boolean notfirst = false;
        for (String type : paramTypes) {
            if (notfirst)
                str += ",";
            else
                notfirst = true;
            str += type.toString();
        }

        str += "->" + returnType.toString();
        return str;
    }
}
