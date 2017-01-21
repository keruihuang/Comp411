/**
 * Created by kerui_000 on 2017/1/18.
 */
/** Parser for Assignment 2 */

import java.io.*;
import java.util.*;

/** Each parser object in this class contains an embedded lexer which contains an embedded input stream.  The
 * class include a parse() method that will translate the program text in the input stream to the corresponding
 * AST assuming that the program text forms a syntactically valid Jam program.
 */
class Parser {

    private Lexer in;



    Parser(Lexer i) {
        in = i;
        initParser();
    }

    Parser(Reader inputStream) { this(new Lexer(inputStream)); }

    Parser(String fileName) throws IOException { this(new FileReader(fileName)); }

    Lexer lexer() { return in; }

    private void initParser() {

    }

    /** Parses the program text in the lexer bound to 'in' and returns the corresponding AST.
     * @throws ParseException if a syntax error is encountered (including lexical errors).
     */
    public AST parse() throws ParseException {
        AST result = null;
        try{
            result = parseExp();
        } catch (Exception e){
            throw new ParseException("");
        }
        Token token = in.peek();
        if(token == null){
            return result;
        } else {
            return null;
        }
    }



    /** Parses:
     *  <term>     ::= { <unop> } <term> | <constant> | <factor> {( <exp-list> )}
     *  <constant> ::= <null> | <int> | <bool>
     * @param token   first token in input stream to be parsed; remainder in Lexer named in.
     */
    private AST parseTerm(Token token) {

        if (token instanceof Op) {
            Op op = (Op) token;
            if (! op.isUnOp()) error(op,"unary operator");
            return new UnOpApp(op, parseTerm(in.readToken()));
        }

        if (token instanceof Constant) return (Constant) token;
        AST factor = parseFactor(token);
        Token next = in.peek();
        if (next == LeftParen.ONLY) {
            in.readToken();  // remove next from input stream
            AST[] exps = parseArgs();  // including closing paren
            return new App(factor,exps);
        }
        return factor;
    }

    /** Parses:
     *     <exp> :: = if <exp> then <exp> else <exp>
     *              | let <prop-def-list> in <exp>
     *              | map <id-list> to <exp>
     *              | <term> { <biop> <exp> }
     *
     * @return  the corresponding AST.
     */
    private AST parseExp() {
        AST exp = null;
        Token token = in.readToken();
        TokenType type = token.getType();
        switch (type) {
            case BOOL:
            case INT:
            case NULL:
            case PRIM_FUN:
            case VAR:
            case OPERATOR:
            case KEYWORD:
                KeyWord key = (KeyWord) token;
                if(key.getName().equals("if")){
                    return parseIf();
                }
                if(key.getName().equals("let")){
                    return parseLet();
                }
                if(key.getName().equals("map")){
                    return parseMap();
                }
            case LEFT_PAREN:
            case RIGHT_PAREN:
            case LEFT_BRACK:
            case RIGHT_BRACK:
            case LEFT_BRACE:
            case RIGHT_BRACE:
            case COMMA:
            case SEMICOLON:
        }
        return exp;
    }

    private AST parseIf(){
        AST t = parseExp();
        Token key1 = in.readToken();
        if (key1.getType().toString() != "then") {
            error(key1, "then?");
        }
        AST c = parseExp();
        Token key2 = in.readToken();
        if (key2.getType().toString() != "else") {
            error(key2, "else?");
        }
        AST a = parseExp();
        return new If(t,c ,a);
    }

    private AST parseMap() {
        Variable[] var = parseVars();
        AST b = parseExp();
        return new Map(var, b);
    }
    private Variable[] parseVars() {

        ArrayList<Variable> vars = new ArrayList<Variable>();
        Token t = in.readToken();
        if (((Variable) t).getName().equals("to")) {
            return new Variable[0];
        }
        do {
            if (!(t instanceof Variable)) {
                error(t, "variable");
            }
            vars.add(-1,(Variable)t);
            t = in.readToken();
            if (((Variable) t).getName().equals("to")) {
                break;
            }
            if (t != Comma.ONLY) {
                error(t, ",?");
            }
            t = in.readToken();
        } while(true);
        return vars.toArray(new Variable[0]);
    }

    private AST parseLet() {
        AST let = null;
        return let;
    }

    private AST parseFactor(Token token) {
        AST factor = null;
        return factor;
    }



    private AST[] parseArgs() {
        ArrayList<AST> args = new ArrayList<AST>();
        AST[] arr = new AST[args.size()];
        return arr;
    }



    private void error(Token token, String message) throws ParseException{
        System.err.println(token.toString() + " caused an error: " + message);
        throw new ParseException(message);
    }
}
