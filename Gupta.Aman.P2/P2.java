import java.util.*;
import java.io.*;
import java_cup.runtime.*;  // defines Symbol

/**
 * This program is to be used to test the Scanner.
 * This version is set up to test all tokens, but more code is needed to test 
 * other aspects of the scanner (e.g., input that causes errors, character 
 * numbers, values associated with tokens)
 */
public class P2 {
    public static void main(String[] args) throws IOException {
                                           // exception may be thrown by yylex
        // test all tokens
	String infile = "allTokens.in";
	String outfile = "allTokens.out";
	System.out.println("Testing all tokens with allTokens.in input file");
        testAllTokens(infile, outfile);
        CharNum.num = 1;
	
	// test for eof.txt
	// should print error message of unterminated string
	infile = "eof.txt";
	//Dummy output file no purpose
	outfile = "eofOut.txt";
	System.out.println("\nTesting eof token with eof.txt input file");
	testAllTokens(infile, outfile);
	CharNum.num = 1;
    }

    /**
     * testAllTokens
     *
     * Open and read from file allTokens.txt
     * For each token read, write the corresponding string to allTokens.out
     * If the input file contains all tokens, one per line, we can verify
     * correctness of the scanner by comparing the input and output files
     * (e.g., using a 'diff' command).
     */
    private static void testAllTokens(String infile, String outfile) throws IOException {
        // open input and output files
        FileReader inFile = null;
        PrintWriter outFile = null;
        try {
            inFile = new FileReader(infile);
            outFile = new PrintWriter(new FileWriter(outfile));
        } catch (FileNotFoundException ex) {
            System.err.print("File allTokens.in not found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.print("allTokens.out cannot be opened.");
            System.exit(-1);
        }

        // create and call the scanner
        Yylex scanner = new Yylex(inFile);
        Symbol token = scanner.next_token();
        while (token.sym != sym.EOF) {
            switch (token.sym) {
            case sym.BOOL:
                outFile.print("bool"); 
		outFile.print("  ");
                outFile.print(((TokenVal)token.value).linenum + "  ");
		outFile.println(((TokenVal)token.value).charnum);
		//outFile.print(token.TokenVal.linenum + "  ");
		//outFile.println(token.TokenVal.charnum);
		break;
			case sym.INT:
                outFile.print("int");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.VOID:
                outFile.print("void");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.TRUE:
                outFile.print("true");
		outFile.print("  "); 
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.FALSE:
                outFile.print("false");
		outFile.print("  "); 
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.STRUCT:
                outFile.print("struct");
		outFile.print("  "); 
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.CIN:
                outFile.print("cin");
		outFile.print("  "); 
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.COUT:
                outFile.print("cout");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;				
            case sym.IF:
                outFile.print("if");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.ELSE:
                outFile.print("else");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.WHILE:
                outFile.print("while");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.RETURN:
                outFile.print("return");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.ID:
                outFile.print(((IdTokenVal)token.value).idVal);
		outFile.print("  ");
		outFile.print(((IdTokenVal)token.value).linenum + "  ");
                outFile.println(((IdTokenVal)token.value).charnum);
                break;
            case sym.INTLITERAL:  
                outFile.print(((IntLitTokenVal)token.value).intVal);
		outFile.print("  ");
		outFile.print(((IntLitTokenVal)token.value).linenum + "  ");
                outFile.println(((IntLitTokenVal)token.value).charnum);
                break;
            case sym.STRINGLITERAL: 
                outFile.print(((StrLitTokenVal)token.value).strVal);
		outFile.print("  ");
		outFile.print(((StrLitTokenVal)token.value).linenum + "  ");
                outFile.println(((StrLitTokenVal)token.value).charnum);
                break;    
            case sym.LCURLY:
                outFile.print("{");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.RCURLY:
                outFile.print("}");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.LPAREN:
                outFile.print("(");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.RPAREN:
                outFile.print(")");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.SEMICOLON:
                outFile.print(";");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.COMMA:
                outFile.print(",");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.DOT:
                outFile.print(".");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.WRITE:
                outFile.print("<<");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.READ:
                outFile.print(">>");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;				
            case sym.PLUSPLUS:
                outFile.print("++");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.MINUSMINUS:
                outFile.print("--");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;	
            case sym.PLUS:
                outFile.print("+");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.MINUS:
                outFile.print("-");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.TIMES:
                outFile.print("*");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.DIVIDE:
                outFile.print("/");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.NOT:
                outFile.print("!");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.AND:
                outFile.print("&&");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.OR:
                outFile.print("||");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.EQUALS:
                outFile.print("==");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.NOTEQUALS:
                outFile.print("!=");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.LESS:
                outFile.print("<");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.GREATER:
                outFile.print(">");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.LESSEQ:
                outFile.print("<=");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
            case sym.GREATEREQ:
                outFile.print(">=");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
			case sym.ASSIGN:
                outFile.print("=");
		outFile.print("  ");
		outFile.print(((TokenVal)token.value).linenum + "  ");
                outFile.println(((TokenVal)token.value).charnum);
                break;
			default:
				outFile.print("UNKNOWN TOKEN");
            } // end switch

            token = scanner.next_token();
        } // end while
        outFile.close();
    }
}
