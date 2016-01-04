import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a Mini program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of 
// children) or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have.  All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
//     Subclass            Kids
//     --------            ----
//     ProgramNode         DeclListNode
//     DeclListNode        linked list of DeclNode
//     DeclNode:
//       VarDeclNode       TypeNode, IdNode, int
//       FnDeclNode        TypeNode, IdNode, FormalsListNode, FnBodyNode
//       FormalDeclNode    TypeNode, IdNode
//       StructDeclNode    IdNode, DeclListNode
//
//     FormalsListNode     linked list of FormalDeclNode
//     FnBodyNode          DeclListNode, StmtListNode
//     StmtListNode        linked list of StmtNode
//     ExpListNode         linked list of ExpNode
//
//     TypeNode:
//       IntNode           -- none --
//       BoolNode          -- none --
//       VoidNode          -- none --
//       StructNode        IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       IntLitNode          -- none --
//       StrLitNode          -- none --
//       TrueNode            -- none --
//       FalseNode           -- none --
//       IdNode              -- none --
//       DotAccessNode       ExpNode, IdNode
//       AssignNode          ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         AndNode
//         OrNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         GreaterNode
//         LessEqNode
//         GreaterEqNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of kids, or
// internal nodes with a fixed number of kids:
//
// (1) Leaf nodes:
//        IntNode,   BoolNode,  VoidNode,  IntLitNode,  StrLitNode,
//        TrueNode,  FalseNode, IdNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, FormalsListNode, StmtListNode, ExpListNode
//
// (3) Internal nodes with fixed numbers of kids:
//        ProgramNode,     VarDeclNode,     FnDeclNode,     FormalDeclNode,
//        StructDeclNode,  FnBodyNode,      StructNode,     AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode, ReadStmtNode,   WriteStmtNode   
//        IfStmtNode,      IfElseStmtNode,  WhileStmtNode,  CallStmtNode
//        ReturnStmtNode,  DotAccessNode,   AssignExpNode,  CallExpNode,
//        UnaryExpNode,    BinaryExpNode,   UnaryMinusNode, NotNode,
//        PlusNode,        MinusNode,       TimesNode,      DivideNode,
//        AndNode,         OrNode,          EqualsNode,     NotEqualsNode,
//        LessNode,        GreaterNode,     LessEqNode,     GreaterEqNode
//
// **********************************************************************

// **********************************************************************
// ASTnode class (base class for all other kinds of nodes)
// **********************************************************************

abstract class ASTnode { 
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void doIndent(PrintWriter p, int indent) {
        for (int k=0; k<indent; k++) p.print(" ");
    }
}

// **********************************************************************
// ProgramNode,  DeclListNode, FormalsListNode, FnBodyNode,
// StmtListNode, ExpListNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    /**
     * nameAnalysis
     * Creates an empty symbol table for the outermost scope, then processes
     * all of the globals, struct defintions, and functions in the program.
     */
    public void nameAnalysis() {
        SymTable symTab = new SymTable();
        myDeclList.nameAnalysis(symTab);
    }
    
    public void typeCheck() {
        myDeclList.typeCheck();
    }
    
    
    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    // 1 kid
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

    public void typeCheck() {
    	for (DeclNode node : myDecls) {
            node.typeCheck();
        }
	}

	/**
     * nameAnalysis
     * Given a symbol table symTab, process all of the decls in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        nameAnalysis(symTab, symTab);
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab and a global symbol table globalTab
     * (for processing struct names in variable decls), process all of the 
     * decls in the list.
     */    
    public void nameAnalysis(SymTable symTab, SymTable globalTab) {
        for (DeclNode node : myDecls) {
            if (node instanceof VarDeclNode) {
                ((VarDeclNode)node).nameAnalysis(symTab, globalTab);
            } else {
                node.nameAnalysis(symTab);
            }
        }
    }    
    
    public void unparse(PrintWriter p, int indent) {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    // list of kids (DeclNodes)
    private List<DeclNode> myDecls;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * for each formal decl in the list
     *     process the formal decl
     *     if there was no error, add type of formal decl to list
     */
    public List<Type> nameAnalysis(SymTable symTab) {
        List<Type> typeList = new LinkedList<Type>();
        for (FormalDeclNode node : myFormals) {
            SemSym sym = node.nameAnalysis(symTab);
            if (sym != null) {
                typeList.add(sym.getType());
            }
        }
        return typeList;
    }    
    
    /**
     * Return the number of formals in this list.
     */
    public int length() {
        return myFormals.size();
    }
    
    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    // list of kids (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
}

class FnBodyNode extends ASTnode {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the declaration list
     * - process the statement list
     */
    public void nameAnalysis(SymTable symTab) {
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
    }    
    
    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    public void typeCheck(Type returnType) {
        myStmtList.typeCheck(returnType);
    } 
    
    // 2 kids
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    public void typeCheck(Type returnType) {
    	for(StmtNode node : myStmts) {
    		if((node instanceof ReturnStmtNode) || (node instanceof IfStmtNode) || (node instanceof IfElseStmtNode) || (node instanceof WhileStmtNode))
    			node.typeCheck(returnType);
    		else
    			node.typeCheck();
        }
	}

	/**
     * nameAnalysis
     * Given a symbol table symTab, process each statement in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        for (StmtNode node : myStmts) {
            node.nameAnalysis(symTab);
        }
    }    
    
    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    // list of kids (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, process each exp in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        for (ExpNode node : myExps) {
            node.nameAnalysis(symTab);
        }
    }
    
    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    public int getNumExp() {
    	return myExps.size();
    }
    
    // list of kids (ExpNodes)
    private List<ExpNode> myExps;

	public List<ExpNode> getExpList() {
		return myExps;
	}
}

// **********************************************************************
// DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
    /**
     * Note: a formal decl needs to return a sym
     */
    abstract public SemSym nameAnalysis(SymTable symTab);

	public void typeCheck() {
	}
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

    /**
     * nameAnalysis (overloaded)
     * Given a symbol table symTab, do:
     * if this name is declared void, then error
     * else if the declaration is of a struct type, 
     *     lookup type name (globally)
     *     if type name doesn't exist, then error
     * if no errors so far,
     *     if name has already been declared in this scope, then error
     *     else add name to local symbol table     
     *
     * symTab is local symbol table (say, for struct field decls)
     * globalTab is global symbol table (for struct type names)
     * symTab and globalTab can be the same
     */
    public SemSym nameAnalysis(SymTable symTab) {
        return nameAnalysis(symTab, symTab);
    }
    
    public SemSym nameAnalysis(SymTable symTab, SymTable globalTab) {
        boolean badDecl = false;
        String name = myId.name();
        SemSym sym = null;
        IdNode structId = null;

        if (myType instanceof VoidNode) {  // check for void type
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        
        else if (myType instanceof StructNode) {
            structId = ((StructNode)myType).idNode();
            sym = globalTab.lookupGlobal(structId.name());
            
            // if the name for the struct type is not found, 
            // or is not a struct type
            if (sym == null || !(sym instanceof StructDefSym)) {
                ErrMsg.fatal(structId.getLineNum(), structId.getCharNum(), 
                             "Invalid name of struct type");
                badDecl = true;
            }
            else {
                structId.link(sym);
            }
        }
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), 
                         "Multiply declared identifier");
            badDecl = true;            
        }
        
        if (!badDecl) {  // insert into symbol table
            try {
                if (myType instanceof StructNode) {
                    sym = new StructSym(structId);
                }
                else {
                    sym = new SemSym(myType.type());
                }
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return sym;
    }    
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
        p.println(";");
    }

    // 3 kids
    private TypeNode myType;
    private IdNode myId;
    private int mySize;  // use value NOT_STRUCT if this is not a struct type

    public static int NOT_STRUCT = -1;
}

class FnDeclNode extends DeclNode {
    public FnDeclNode(TypeNode type,
                      IdNode id,
                      FormalsListNode formalList,
                      FnBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name has already been declared in this scope, then error
     * else add name to local symbol table
     * in any case, do the following:
     *     enter new scope
     *     process the formals
     *     if this function is not multiply declared,
     *         update symbol table entry with types of formals
     *     process the body of the function
     *     exit scope
     */
    public SemSym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        FnSym sym = null;
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(),
                         "Multiply declared identifier");
        }
        
        else { // add function name to local symbol table
            try {
                sym = new FnSym(myType.type(), myFormalsList.length());
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in FnDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in FnDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        symTab.addScope();  // add a new scope for locals and params
        
        // process the formals
        List<Type> typeList = myFormalsList.nameAnalysis(symTab);
        if (sym != null) {
            sym.addFormals(typeList);
        }
        
        myBody.nameAnalysis(symTab); // process the function body
        
        try {
            symTab.removeScope();  // exit scope
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in FnDeclNode.nameAnalysis");
            System.exit(-1);
        }
        
        return null;
    }    
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
        p.print("(");
        myFormalsList.unparse(p, 0);
        p.println(") {");
        myBody.unparse(p, indent+4);
        p.println("}\n");
    }

    public void typeCheck() {
        myBody.typeCheck(myType.type());
    }
    
    // 4 kids
    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FnBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this formal is declared void, then error
     * else if this formal is already in the local symble table,
     *     then issue multiply declared error message and return null
     * else add a new entry to the symbol table and return that Sym
     */
    public SemSym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        boolean badDecl = false;
        SemSym sym = null;
        
        if (myType instanceof VoidNode) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), 
                         "Multiply declared identifier");
            badDecl = true;
        }
        
        if (!badDecl) {  // insert into symbol table
            try {
                sym = new SemSym(myType.type());
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return sym;
    }    
    
    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
    }

    // 2 kids
    private TypeNode myType;
    private IdNode myId;
}

class StructDeclNode extends DeclNode {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name is already in the symbol table,
     *     then multiply declared error (don't add to symbol table)
     * create a new symbol table for this struct definition
     * process the decl list
     * if no errors
     *     add a new entry to symbol table for this struct
     */
    public SemSym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        boolean badDecl = false;
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), 
                         "Multiply declared identifier");
            badDecl = true;            
        }

        SymTable structSymTab = new SymTable();
        
        // process the fields of the struct
        myDeclList.nameAnalysis(structSymTab, symTab);
        
        if (!badDecl) {
            try {   // add entry to symbol table
                StructDefSym sym = new StructDefSym(structSymTab);
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return null;
    }    
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("struct ");
        p.print(myId.name());
        p.println("{");
        myDeclList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("};\n");

    }

    // 2 kids
    private IdNode myId;
    private DeclListNode myDeclList;
}

// **********************************************************************
// TypeNode and its Subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
    /* all subclasses must provide a type method */
    abstract public Type type();
}

class IntNode extends TypeNode {
    public IntNode() {
    }

    /**
     * type
     */
    public Type type() {
        return new IntType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("int");
    }
}

class BoolNode extends TypeNode {
    public BoolNode() {
    }

    /**
     * type
     */
    public Type type() {
        return new BoolType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("bool");
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }
    
    /**
     * type
     */
    public Type type() {
        return new VoidType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
        myId = id;
    }

    public IdNode idNode() {
        return myId;
    }
    
    /**
     * type
     */
    public Type type() {
        return new StructType(myId);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
        p.print(myId.name());
    }
    
    // 1 kid
    private IdNode myId;
}

// **********************************************************************
// StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
    abstract public void nameAnalysis(SymTable symTab);
    abstract public void typeCheck(Type returnType);
    abstract public void typeCheck();
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignNode assign) {
        myAssign = assign;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myAssign.nameAnalysis(symTab);
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(";");
    }

    // 1 kid
    private AssignNode myAssign;

	public void typeCheck(Type retType) {
		typeCheck();
		
	}

	public void typeCheck() {
		myAssign.typeCheck();
		
	}
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("++;");
    }

    // 1 kid
    private ExpNode myExp;


	public void typeCheck(Type retType) {
		typeCheck();
		
	}

	
	public void typeCheck() {
		 Type expType = myExp.typeCheck();
	        
	        if (expType.isErrorType() && !expType.isIntType()) {
	            ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), "Arithmetic operator applied to non-numeric operand");
	        }
	}
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("--;");
    }

    // 1 kid
    private ExpNode myExp;

	
	public void typeCheck(Type retType) {
		typeCheck();
		
	}

	
	public void typeCheck() {
		 Type expType = myExp.typeCheck();
	        
	        if (!expType.isErrorType() && !expType.isIntType()) {
	            ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), "Arithmetic operator applied to non-numeric operand");
	        }
		
	}
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }    
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("cin >> ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    // 1 kid (actually can only be an IdNode or an ArrayExpNode)
    private ExpNode myExp;

	
	public void typeCheck(Type retType) {
		typeCheck();
		
	}

	
	public void typeCheck() {
		Type expType = myExp.typeCheck();
	        
	    if (expType.isFnType()) { 
	    	ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), "Attempt to read a function");
	        }
	        
	        if (expType.isStructDefType()) {
	            ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), "Attempt to read a struct name");
	        }
	        
	        if (expType.isStructType()) {
	            ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), "Attempt to read a struct variable");
	        }
	}
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("cout << ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    // 1 kid
    private ExpNode myExp;

	
	public void typeCheck(Type retType) {
		typeCheck();
		
	}

	
	public void typeCheck() {
		Type expType = myExp.typeCheck();
        
        if (expType.isFnType()) {
            ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), "Attempt to write a function");
        }
        
        if (expType.isStructDefType()) {
            ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), "Attempt to write a struct name");
        }
        
        if (expType.isStructType()) {
            ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), "Attempt to write a struct variable");
        }
        
        if (expType.isVoidType()) {
            ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), "Attempt to write void");
        }		
	}
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
    }

    // e kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
	
	public void typeCheck() {
		
	}

	
	public void typeCheck(Type retType) {
		Type expType = myExp.typeCheck();
        
        if (expType.isErrorType() == false && expType.isBoolType() == false) {
            ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), "Non-bool expression used as an if condition");        
        }
        
        myStmtList.typeCheck(retType);		
	}
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
                          StmtListNode slist1, DeclListNode dlist2,
                          StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts of then
     * - exit the scope
     * - enter a new scope
     * - process the decls and stmts of else
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myThenDeclList.nameAnalysis(symTab);
        myThenStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
        symTab.addScope();
        myElseDeclList.nameAnalysis(symTab);
        myElseStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myThenDeclList.unparse(p, indent+4);
        myThenStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
        doIndent(p, indent);
        p.println("else {");
        myElseDeclList.unparse(p, indent+4);
        myElseStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");        
    }

    // 5 kids
    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
	
	public void typeCheck(Type retType) {
		 Type expType = myExp.typeCheck();
	        
	        if (!expType.isErrorType() && !expType.isBoolType()) {
	            ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(),
	                         "Non-bool expression used as an if condition");        
	        }
	        
	        myThenStmtList.typeCheck(retType);
	        myElseStmtList.typeCheck(retType);		
	}

	
	public void typeCheck() {
		
	}
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("while (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
    }

    // 3 kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
	
	public void typeCheck(Type retType) {
		Type expType = myExp.typeCheck();
        
        if (!expType.isErrorType() && !expType.isBoolType()) {
            ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), "Non-bool expression used as a while condition");        
        }
        
        myStmtList.typeCheck(retType);
		
	}

	
	public void typeCheck() {
		
	}
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myCall.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myCall.unparse(p, indent);
        p.println(";");
    }

    // 1 kid
    private CallExpNode myCall;

	
	public void typeCheck(Type retType) {
		typeCheck();
		
	}

	
	public void typeCheck() {
		 myCall.typeCheck();		
	}
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child,
     * if it has one
     */
    public void nameAnalysis(SymTable symTab) {
        if (myExp != null) {
            myExp.nameAnalysis(symTab);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(";");
    }

    // 1 kid
    private ExpNode myExp; // possibly null

	@Override
	public void typeCheck(Type retType) {
		if(myExp == null && retType.isVoidType() == false)
		{
			ErrMsg.fatal(0, 0, "Missing return value"); 
		}
		if (myExp != null) {
            Type expType = myExp.typeCheck();
            
            if (retType.isVoidType()) {
                ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), "Return with a value in a void function");                
            }
            
            else if (!retType.equals(expType) && !retType.isErrorType() && !expType.isErrorType()){
                ErrMsg.fatal(myExp.getLineNum(), myExp.getCharNum(), "Bad return value");
            }
        }
		
	}

	
	public void typeCheck() {
		
	}
}

// **********************************************************************
// ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    /**
     * Default version for nodes with no names
     */
    public void nameAnalysis(SymTable symTab) { }
    abstract public Type typeCheck();
    abstract public int getCharNum();
    abstract public int getLineNum();
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    private int myLineNum;
    private int myCharNum;
    private int myIntVal;
	
	public Type typeCheck() {
		return new IntType();
	}

	
	public int getCharNum() {
		return myCharNum;
	}

	
	public int getLineNum() {
		return myLineNum;
	}
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    public int getLineNum() {
    	return myLineNum;
    }
    
    public int getCharNum() {
    	return myCharNum;
    }
    
    
    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
	
	public Type typeCheck() {
		return new StringType();
	}
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("true");
    }

    private int myLineNum;
    private int myCharNum;
	
	public Type typeCheck() {
		return new BoolType();
	}

	public int getCharNum() {
		return myCharNum;
	}

	
	public int getLineNum() {
		return myLineNum;
	}
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("false");
    }

    private int myLineNum;
    private int myCharNum;
	
	public Type typeCheck() {
		return new BoolType();
	}

	public int getCharNum() {
		return myCharNum;
	}

	
	public int getLineNum() {
		return myLineNum;
	}
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    /**
     * Link the given symbol to this ID.
     */
    public void link(SemSym sym) {
        mySym = sym;
    }
    
    /**
     * Return the name of this ID.
     */
    public String name() {
        return myStrVal;
    }
    
    /**
     * Return the symbol associated with this ID.
     */
    public SemSym sym() {
        return mySym;
    }
  
    
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - check for use of undeclared name
     * - if ok, link to symbol table entry
     */
    public void nameAnalysis(SymTable symTab) {
        SemSym sym = symTab.lookupGlobal(myStrVal);
        if (sym == null) {
            ErrMsg.fatal(myLineNum, myCharNum, "Undeclared identifier");
        } else {
            link(sym);
        }
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
        if (mySym != null) {
            p.print("(" + mySym + ")");
        }
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    private SemSym mySym;
	@Override
	public Type typeCheck() {
		return mySym.getType();
	}

	public int getCharNum() {
		return myCharNum;
	}

	
	public int getLineNum() {
		return myLineNum;
	}
}

class DotAccessExpNode extends ExpNode {
    public DotAccessExpNode(ExpNode loc, IdNode id) {
        myLoc = loc;    
        myId = id;
        mySym = null;
    }

    /**
     * Return the symbol associated with this dot-access node.
     */
    public SemSym sym() {
        return mySym;
    }    
    
    /**
     * Return the line number for this dot-access node. 
     * The line number is the one corresponding to the RHS of the dot-access.
     */
    public int lineNum() {
        return myId.getLineNum();
    }
    
    /**
     * Return the char number for this dot-access node.
     * The char number is the one corresponding to the RHS of the dot-access.
     */
    public int charNum() {
        return myId.getCharNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the LHS of the dot-access
     * - process the RHS of the dot-access
     * - if the RHS is of a struct type, set the sym for this node so that
     *   a dot-access "higher up" in the AST can get access to the symbol
     *   table for the appropriate struct definition
     */
    public void nameAnalysis(SymTable symTab) {
        badAccess = false;
        SymTable structSymTab = null; // to lookup RHS of dot-access
        SemSym sym = null;
        
        myLoc.nameAnalysis(symTab);  // do name analysis on LHS
        
        // if myLoc is really an ID, then sym will be a link to the ID's symbol
        if (myLoc instanceof IdNode) {
            IdNode id = (IdNode)myLoc;
            sym = id.sym();
            
            // check ID has been declared to be of a struct type
            
            if (sym == null) { // ID was undeclared
                badAccess = true;
            }
            else if (sym instanceof StructSym) { 
                // get symbol table for struct type
                SemSym tempSym = ((StructSym)sym).getStructType().sym();
                structSymTab = ((StructDefSym)tempSym).getSymTable();
            } 
            else {  // LHS is not a struct type
                ErrMsg.fatal(id.getLineNum(), id.getCharNum(), 
                             "Dot-access of non-struct type");
                badAccess = true;
            }
        }
        
        // if myLoc is really a dot-access (i.e., myLoc was of the form
        // LHSloc.RHSid), then sym will either be
        // null - indicating RHSid is not of a struct type, or
        // a link to the Sym for the struct type RHSid was declared to be
        else if (myLoc instanceof DotAccessExpNode) {
            DotAccessExpNode loc = (DotAccessExpNode)myLoc;
            
            if (loc.badAccess) {  // if errors in processing myLoc
                badAccess = true; // don't continue proccessing this dot-access
            }
            else { //  no errors in processing myLoc
                sym = loc.sym();

                if (sym == null) {  // no struct in which to look up RHS
                    ErrMsg.fatal(loc.lineNum(), loc.charNum(), 
                                 "Dot-access of non-struct type");
                    badAccess = true;
                }
                else {  // get the struct's symbol table in which to lookup RHS
                    if (sym instanceof StructDefSym) {
                        structSymTab = ((StructDefSym)sym).getSymTable();
                    }
                    else {
                        System.err.println("Unexpected Sym type in DotAccessExpNode");
                        System.exit(-1);
                    }
                }
            }

        }
        
        else { // don't know what kind of thing myLoc is
            System.err.println("Unexpected node type in LHS of dot-access");
            System.exit(-1);
        }
        
        // do name analysis on RHS of dot-access in the struct's symbol table
        if (!badAccess) {
        
            sym = structSymTab.lookupGlobal(myId.name()); // lookup
            if (sym == null) { // not found - RHS is not a valid field name
                ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), 
                             "Invalid struct field name");
                badAccess = true;
            }
            
            else {
                myId.link(sym);  // link the symbol
                // if RHS is itself as struct type, link the symbol for its struct 
                // type to this dot-access node (to allow chained dot-access)
                if (sym instanceof StructSym) {
                    mySym = ((StructSym)sym).getStructType().sym();
                }
            }
        }
    }    
    
    public void unparse(PrintWriter p, int indent) {
        myLoc.unparse(p, 0);
        p.print(".");
        myId.unparse(p, 0);
    }

    // 2 kids
    private ExpNode myLoc;    
    private IdNode myId;
    private SemSym mySym;          // link to Sym for struct type
    private boolean badAccess;  // to prevent multiple, cascading errors
	
	public Type typeCheck() {
		return myId.typeCheck();
	}

	
	public int getCharNum() {
		return myId.getCharNum();
	}

	
	public int getLineNum() {
		return myId.getLineNum();
	}
}

class AssignNode extends ExpNode {
    public AssignNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myLhs.nameAnalysis(symTab);
        myExp.nameAnalysis(symTab);
    }
    
    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)  p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myExp.unparse(p, 0);
        if (indent != -1)  p.print(")");
    }

    // 2 kids
    private ExpNode myLhs;
    private ExpNode myExp;
	
	public Type typeCheck() {
		Type lhsType = myLhs.typeCheck();
		Type expType = myExp.typeCheck();
		Type retType = new BoolType();
		
		
		
		/*if(exp1Type.isVoidType() == true && exp2Type.isVoidType() == true){
			ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Equality operator applied to void functions");
			retType = new ErrorType();
		}*/
		if(lhsType.isFnType() == true && expType.isFnType() == true){
			ErrMsg.fatal(getLineNum(), getCharNum(), "Function assignment");
			retType = new ErrorType();
		}
		if(lhsType.isStructDefType() == true && expType.isStructDefType() == true){
			ErrMsg.fatal(getLineNum(), getCharNum(), "Struct name assignment");
			retType = new ErrorType();
		}
		if(lhsType.isStructType() == true && expType.isStructType() == true){
			ErrMsg.fatal(getLineNum(), getCharNum(), "Struct variable assignment");
			retType = new ErrorType();
		}
		
		if (!lhsType.equals(expType) && lhsType.isErrorType() == false && expType.isErrorType() == false) {
            ErrMsg.fatal(getLineNum(), getCharNum(), "Type mismatch");
            retType = new ErrorType();
        }
		
		if(lhsType.isErrorType() || expType.isErrorType())
			retType = new ErrorType();
		return retType;
	}

	
	public int getCharNum() {
		return myLhs.getCharNum();
	}

	
	public int getLineNum() {
		return myLhs.getLineNum();
	}
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myId.nameAnalysis(symTab);
        myExpList.nameAnalysis(symTab);
    }    
    
    // ** unparse **
    public void unparse(PrintWriter p, int indent) {
        myId.unparse(p, 0);
        p.print("(");
        if (myExpList != null) {
            myExpList.unparse(p, 0);
        }
        p.print(")");
    }

    // 2 kids
    private IdNode myId;
    private ExpListNode myExpList;  // possibly null
	
	public Type typeCheck() {
		if(myId.typeCheck().isFnType() == false) {
			ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Attempt to call a non-function");
			return new ErrorType();
		}
		if(myExpList.getNumExp() != ((FnSym)myId.sym()).getNumParams()) {
			ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Function call with wrong number of args");
			return new ErrorType();
		}
		List<ExpNode> expectedExps = myExpList.getExpList(); 
		int argumentNum = 0;
		for (ExpNode node : expectedExps) {      // expectedExps are coming from already stored fnc def
            Type currentType = node.typeCheck(); //The types which we are processing right now
   
            if (currentType.isErrorType() == false) {
                Type actualType = ((FnSym)myId.sym()).getParamTypes().get(argumentNum);
                if (!actualType.equals(currentType)) {
                    ErrMsg.fatal(node.getLineNum(), node.getCharNum(), "Type of actual does not match type of formal");
                }
            }
            argumentNum++;
        }
       
		return ((FnSym)myId.sym()).getReturnType();
	}

	
	public int getCharNum() {
		return myId.getCharNum();
	}

	
	public int getLineNum() {
		return myId.getLineNum();
	}
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    // one child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myExp1.nameAnalysis(symTab);
        myExp2.nameAnalysis(symTab);
    }
    
    // two kids
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// Subclasses of UnaryExpNode
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        myExp.unparse(p, 0);
        p.print(")");
    }

	
	public Type typeCheck() {
		Type expType = myExp.typeCheck();
	    Type retType = new IntType();
	        
	    if (expType.isErrorType() == false && expType.isIntType() == false) {
	    	ErrMsg.fatal(getLineNum(), getCharNum(), "Arithmetic operator applied to non-numeric operand");
	    	retType = new ErrorType();
	    }
	        
	    if (expType.isErrorType()) {
	        retType = new ErrorType();
	    }  
	    return retType;
	}

	public int getCharNum() {
		return myExp.getCharNum();
	}

	
	public int getLineNum() {
		return myExp.getLineNum();
	}
}

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(!");
        myExp.unparse(p, 0);
        p.print(")");
    }

	
	public Type typeCheck() {
		Type expType = myExp.typeCheck();
        Type retType = new BoolType();
        
        if (expType.isErrorType() == false && expType.isBoolType() == false) {
            ErrMsg.fatal(getLineNum(), getCharNum(), "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }
        
        if (expType.isErrorType()) {
            retType = new ErrorType();
        }
        return retType;
	}

	public int getCharNum() {
		return myExp.getCharNum();
	}

	
	public int getLineNum() {
		return myExp.getLineNum();
	}
}

// **********************************************************************
// Subclasses of BinaryExpNode
// **********************************************************************

class PlusNode extends BinaryExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" + ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	
	public Type typeCheck() {
		Type exp1Type = myExp1.typeCheck();
        Type exp2Type = myExp2.typeCheck();
        Type retType = new IntType();
        
        if (exp1Type.isErrorType() == false && exp1Type.isIntType() == false) {
            ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (exp2Type.isErrorType() == false && exp2Type.isIntType() == false) {
            ErrMsg.fatal(myExp2.getLineNum(), myExp2.getCharNum(), "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (exp1Type.isErrorType() || exp2Type.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
	}

	public int getCharNum() {
		return myExp1.getCharNum();
	}

	
	public int getLineNum() {
		return myExp1.getLineNum();
	}
	
}

class MinusNode extends BinaryExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" - ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	
	public Type typeCheck() {
		Type exp1Type = myExp1.typeCheck();
        Type exp2Type = myExp2.typeCheck();
        Type retType = new IntType();
        
        if (exp1Type.isErrorType() == false && exp1Type.isIntType() == false) {
            ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (exp2Type.isErrorType() == false && exp2Type.isIntType() == false) {
            ErrMsg.fatal(myExp2.getLineNum(), myExp2.getCharNum(), "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (exp1Type.isErrorType() || exp2Type.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
	}

	public int getCharNum() {
		return myExp1.getCharNum();
	}

	
	public int getLineNum() {
		return myExp1.getLineNum();
	}
	
}

class TimesNode extends BinaryExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" * ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	
	public Type typeCheck() {
		Type exp1Type = myExp1.typeCheck();
        Type exp2Type = myExp2.typeCheck();
        Type retType = new IntType();
        
        if (exp1Type.isErrorType() == false && exp1Type.isIntType() == false) {
            ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (exp2Type.isErrorType() == false && exp2Type.isIntType() == false) {
            ErrMsg.fatal(myExp2.getLineNum(), myExp2.getCharNum(), "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (exp1Type.isErrorType() || exp2Type.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
	}

	public int getCharNum() {
		return myExp1.getCharNum();
	}

	
	public int getLineNum() {
		return myExp1.getLineNum();
	}
	
}

class DivideNode extends BinaryExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" / ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	
	public Type typeCheck() {
		Type exp1Type = myExp1.typeCheck();
        Type exp2Type = myExp2.typeCheck();
        Type retType = new IntType();
        
        if (exp1Type.isErrorType() == false && exp1Type.isIntType() == false) {
            ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (exp2Type.isErrorType() == false && exp2Type.isIntType() == false) {
            ErrMsg.fatal(myExp2.getLineNum(), myExp2.getCharNum(), "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (exp1Type.isErrorType() || exp2Type.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;

	}

	public int getCharNum() {
		return myExp1.getCharNum();
	}

	
	public int getLineNum() {
		return myExp1.getLineNum();
	}
	
}

class AndNode extends BinaryExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" && ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	@Override
	public Type typeCheck() {
		Type exp1Type = myExp1.typeCheck();
        Type exp2Type = myExp2.typeCheck();
        Type retType = new BoolType();
        
        if (exp1Type.isErrorType() == false && exp1Type.isBoolType() == false) {
            ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }
        
        if (exp2Type.isErrorType() == false && exp2Type.isBoolType() == false) {
            ErrMsg.fatal(myExp2.getLineNum(), myExp2.getCharNum(), "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }
        
        if (exp1Type.isErrorType() || exp2Type.isErrorType()) {
            retType = new ErrorType();
        }
        return retType;
	}

	public int getCharNum() {
		return myExp1.getCharNum();
	}

	
	public int getLineNum() {
		return myExp1.getLineNum();
	}
	
}

class OrNode extends BinaryExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" || ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	
	public Type typeCheck() {
		Type exp1Type = myExp1.typeCheck();
        Type exp2Type = myExp2.typeCheck();
        Type retType = new BoolType();
        
        if (exp1Type.isErrorType() == false && exp1Type.isBoolType() == false) {
            ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }
        
        if (exp2Type.isErrorType() == false && exp2Type.isBoolType() == false) {
            ErrMsg.fatal(myExp2.getLineNum(), myExp2.getCharNum(), "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }
        
        if (exp1Type.isErrorType() || exp2Type.isErrorType()) {
            retType = new ErrorType();
        }
        return retType;
	}

	public int getCharNum() {
		return myExp1.getCharNum();
	}

	
	public int getLineNum() {
		return myExp1.getLineNum();
	}
	
}

class EqualsNode extends BinaryExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" == ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	
	public Type typeCheck() {
		Type exp1Type = myExp1.typeCheck();
		Type exp2Type = myExp2.typeCheck();
		Type retType = new BoolType();
		
		if(exp1Type.isVoidType() == true && exp2Type.isVoidType() == true){
			ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Equality operator applied to void functions");
			retType = new ErrorType();
		}
		if(exp1Type.isFnType() == true && exp2Type.isFnType() == true){
			ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Equality operator applied to functions");
			retType = new ErrorType();
		}
		if(exp1Type.isStructDefType() == true && exp2Type.isStructDefType() == true){
			ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Equality operator applied to struct names");
			retType = new ErrorType();
		}
		if(exp1Type.isStructType() == true && exp2Type.isStructType() == true){
			ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Equality operator applied to struct variables");
			retType = new ErrorType();
		}
		
		if (!exp1Type.equals(exp2Type) && exp1Type.isErrorType() == false && exp2Type.isErrorType() == false) {
            ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Type mismatch");
            retType = new ErrorType();
        }
		
		if(exp1Type.isErrorType() || exp2Type.isErrorType())
			retType = new ErrorType();
		return retType;
	}

	public int getCharNum() {
		return myExp1.getCharNum();
	}

	
	public int getLineNum() {
		return myExp1.getLineNum();
	}
	
}

class NotEqualsNode extends BinaryExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" != ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	
	public Type typeCheck() {
		Type exp1Type = myExp1.typeCheck();
		Type exp2Type = myExp2.typeCheck();
		Type retType = new BoolType();
		
		if(exp1Type.isVoidType() == true && exp2Type.isVoidType() == true){
			ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Equality operator applied to void functions");
			retType = new ErrorType();
		}
		if(exp1Type.isFnType() == true && exp2Type.isFnType() == true){
			ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Equality operator applied to functions");
			retType = new ErrorType();
		}
		if(exp1Type.isStructDefType() == true && exp2Type.isStructDefType() == true){
			ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Equality operator applied to struct names");
			retType = new ErrorType();
		}
		if(exp1Type.isStructType() == true && exp2Type.isStructType() == true){
			ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Equality operator applied to struct variables");
			retType = new ErrorType();
		}
		
		if (!exp1Type.equals(exp2Type) && exp1Type.isErrorType() == false && exp2Type.isErrorType() == false) {
            ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Type mismatch");
            retType = new ErrorType();
        }
		
		if(exp1Type.isErrorType() || exp2Type.isErrorType())
			retType = new ErrorType();
		return retType;
		
	}

	public int getCharNum() {
		return myExp1.getCharNum();
	}

	
	public int getLineNum() {
		return myExp1.getLineNum();
	}
	
}

class LessNode extends BinaryExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" < ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	
	public Type typeCheck() {
		Type exp1Type = myExp1.typeCheck();
		Type exp2Type = myExp2.typeCheck();
		Type retType = new BoolType();
		if(exp1Type.isIntType() == false && exp1Type.isErrorType() == false){
			ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Relational operator applied to non-numeric operand");
			retType = new ErrorType();
		}
		if(exp2Type.isIntType() == false && exp2Type.isErrorType() == false){
			ErrMsg.fatal(myExp2.getLineNum(), myExp2.getCharNum(), "Relational operator applied to non-numeric operand");
			retType = new ErrorType();
		}
		if(exp1Type.isErrorType() || exp2Type.isErrorType())
			retType = new ErrorType();
		return retType;
		
	}

	public int getCharNum() {
		return myExp1.getCharNum();
	}

	
	public int getLineNum() {
		return myExp1.getLineNum();
	}
	
}

class GreaterNode extends BinaryExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" > ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	
	public Type typeCheck() {
		Type exp1Type = myExp1.typeCheck();
		Type exp2Type = myExp2.typeCheck();
		Type retType = new BoolType();
		if(exp1Type.isIntType() == false && exp1Type.isErrorType() == false){
			ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Relational operator applied to non-numeric operand");
			retType = new ErrorType();
		}
		if(exp2Type.isIntType() == false && exp2Type.isErrorType() == false){
			ErrMsg.fatal(myExp2.getLineNum(), myExp2.getCharNum(), "Relational operator applied to non-numeric operand");
			retType = new ErrorType();
		}
		if(exp1Type.isErrorType() || exp2Type.isErrorType())
			retType = new ErrorType();
		return retType;
		
	}

	public int getCharNum() {
		return myExp1.getCharNum();
	}

	
	public int getLineNum() {
		return myExp1.getLineNum();
	}
	
}

class LessEqNode extends BinaryExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" <= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	
	public Type typeCheck() {
		Type exp1Type = myExp1.typeCheck();
		Type exp2Type = myExp2.typeCheck();
		Type retType = new BoolType();
		if(exp1Type.isIntType() == false && exp1Type.isErrorType() == false){
			ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Relational operator applied to non-numeric operand");
			retType = new ErrorType();
		}
		if(exp2Type.isIntType() == false && exp2Type.isErrorType() == false){
			ErrMsg.fatal(myExp2.getLineNum(), myExp2.getCharNum(), "Relational operator applied to non-numeric operand");
			retType = new ErrorType();
		}
		if(exp1Type.isErrorType() || exp2Type.isErrorType())
			retType = new ErrorType();
		return retType;
		
	}

	public int getCharNum() {
		return myExp1.getCharNum();
	}

	
	public int getLineNum() {
		return myExp1.getLineNum();
	}
	
}

class GreaterEqNode extends BinaryExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" >= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	
	public Type typeCheck() {
		Type exp1Type = myExp1.typeCheck();
		Type exp2Type = myExp2.typeCheck();
		Type retType = new BoolType();
		if(exp1Type.isIntType() == false && exp1Type.isErrorType() == false){
			ErrMsg.fatal(myExp1.getLineNum(), myExp1.getCharNum(), "Relational operator applied to non-numeric operand");
			retType = new ErrorType();
		}
		if(exp2Type.isIntType() == false && exp2Type.isErrorType() == false){
			ErrMsg.fatal(myExp2.getLineNum(), myExp2.getCharNum(), "Relational operator applied to non-numeric operand");
			retType = new ErrorType();
		}
		if(exp1Type.isErrorType() || exp2Type.isErrorType())
			retType = new ErrorType();
		return retType;
		
	}

	
	public int getCharNum() {
		return myExp1.getCharNum();
	}

	
	public int getLineNum() {
		return myExp1.getLineNum();
	}
}
