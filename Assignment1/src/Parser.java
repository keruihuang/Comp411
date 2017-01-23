/**
 * Created by kerui_000 on 2017/1/18.
 */

import java.io.*;
import java.util.*;

/** Each parser object in this class contains an embedded lexer which contains an embedded input stream.  The
 * class include a parse() method that will translate the program text in the input stream to the corresponding
 * AST assuming that the program text forms a syntactically valid Jam program.
 */

class Parser {

    private Lexer in;
    private KeyWord ifKey;
    private KeyWord thenKey;
    private KeyWord elseKey;
    private KeyWord letKey;
    private KeyWord inKey;
    private KeyWord mapKey;
    private KeyWord toKey;
    private KeyWord defKey;


    Parser(Lexer i) {
        in = i;
        initParser();
    }

    Parser(Reader inputStream) { this(new Lexer(inputStream)); }

    Parser(String fileName) throws IOException { this(new FileReader(fileName)); }

    Lexer lexer() { return in; }

    private void initParser() {
        ifKey = (KeyWord)in.wordTable.get("if");
        thenKey = (KeyWord)in.wordTable.get("then");
        elseKey = (KeyWord)in.wordTable.get("else");
        letKey = (KeyWord)in.wordTable.get("let");
        inKey = (KeyWord)in.wordTable.get("in");
        mapKey = (KeyWord)in.wordTable.get("map");
        toKey = (KeyWord)in.wordTable.get("to");
        defKey = (KeyWord)in.wordTable.get(":=");
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
        AST result = null;
        Token token = in.readToken();
        TokenType type = token.getType();
        switch (type) {
            case BOOL:
            case INT:
            case NULL:
            case PRIM_FUN:
            case VAR:
                return parseVar(result,token);
            case OPERATOR:
            case KEYWORD:
                KeyWord key = (KeyWord) token;
                if(key == ifKey){
                    return parseIf(key, token);
                }
                if(key == letKey){
                    return parseLet(key, token);
                }
                if(key == mapKey){
                    return parseMap(key, token);
                }
            case LEFT_PAREN:
            case RIGHT_PAREN:
            case LEFT_BRACK:
            case RIGHT_BRACK:
            case LEFT_BRACE:
            case RIGHT_BRACE:
            case COMMA:
            case SEMICOLON:
            default:
                throw new
                        ParseException("illegal token");
        }
    }


    private AST parseIf(KeyWord key, Token token){
        AST t = parseExp();
        Token nextToken = in.peek();
        if (nextToken instanceof KeyWord){
            key = (KeyWord) nextToken;
            if (key == thenKey){
                in.readToken();
            } else {
                error(token,"expecting if, then");
            }
        } else {
            error(token,"expecting a keyword");
        }
        AST c = parseExp();
        nextToken = in.peek();
        if (nextToken instanceof KeyWord){
            key = (KeyWord) nextToken;
            if (key == elseKey){
                token = in.readToken();
            } else {
                error(token,"expecting if, then, else");
            }
        } else {
            error(token,"expecting keyWord");
        }
        AST a = parseExp();
        return new If(t,c,a);
    }

    private AST parseLet(KeyWord key, Token token) {
        ArrayList<Def> defs = new ArrayList<Def>();
        token = in.readToken();
        if (token instanceof Variable){
            while (token instanceof Variable) {
                Variable var = (Variable) token;
                token = in.readToken();
                if (token instanceof KeyWord) {
                    key =  (KeyWord) token;
                    if (key == defKey){
                        Def def = new Def(var,parseExp());
                        defs.add(def);
                        token = in.readToken();
                        if (token instanceof SemiColon){
                            token = in.readToken();
                        } else {
                            error(token,"expect ;");
                        }
                    } else {
                        error(token,"expect :=");
                    }
                }
            }
            if (token instanceof KeyWord){
                key = (KeyWord) token;
                if (!(key == inKey)) {
                    error(token,"expect let, in");
                }
            } else {
                error(token,"expect in");
            }
            Def[] arr = new Def[defs.size()];
            defs.toArray(arr);
            return new Let(arr,parseExp());
        }else error(token, "expect let");
        return null;
    }

    private AST parseMap(KeyWord key, Token token) {
        token = in.readToken();
        ArrayList<Variable> vars = new ArrayList<Variable>();

        return null;
    }
    private AST parseVar(AST result, Token token) {
        AST term = parseTerm(token);
        Token next = in.peek();
        if (next instanceof Op){
            token = in.readToken();
            Op op = (Op) token;
            if (op.isBinOp()){
                AST exp = parseExp();
                result = new BinOpApp(op,term,exp);
            } else {
                error(token,"expect term");
            }
        } else {
            result = term;
        }
        return result;

    }

    private AST parseFactor(Token token) {
        AST exp = null;
        if (token == LeftParen.ONLY){
            exp = parseExp();
            token = in.readToken();
            if (token == RightParen.ONLY){
                return exp;
            } else {
                error(token,"expect rightParen");
                return exp = null;
            }
        } else if (token instanceof PrimFun) {
            return (PrimFun) token;
        } else if (token instanceof Variable) {
            return (Variable) token;
        } else{
            error(token,"expect paren");
            return exp;
        }
    }



    private AST[] parseArgs() {
        ArrayList<AST> args = new ArrayList<AST>();
        Token next = in.peek();
        if (next != RightParen.ONLY){
            args.add(parseExp());
            next = in.peek();
            while (next != RightParen.ONLY) {
                if (next instanceof Comma){
                    in.readToken();
                }
                args.add(parseExp());
                next = in.peek();
            }
        }
        in.readToken();
        AST[] arr = new AST[args.size()];
        args.toArray(arr);
        return arr;
    }



    private void error(Token token, String message) throws ParseException{
        System.out.println(in.readToken());
        throw new ParseException(token + " + " + message);
    }
}
