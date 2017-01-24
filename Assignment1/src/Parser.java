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
                if(token == ifKey){
                    return parseIf();
                }
                if(token == letKey){
                    return parseLet();
                }
                if(token == mapKey){
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
            default:
                throw new
                        ParseException("illegal token");
        }
    }


    private AST parseIf(){
        AST t = parseExp();
        Token token1 = in.peek();
        if (token1 instanceof KeyWord){
            if (token1 == thenKey){
                in.readToken();
            } else {
                error(token1,"expecting if, then");
            }
        } else {
            error(token1,"expecting a keyword");
        }
        AST c = parseExp();
        Token token2 = in.peek();
        if (token2 instanceof KeyWord){
            if (token2 == elseKey){
                in.readToken();
            } else {
                error(token2,"expecting if, then, else");
            }
        } else {
            error(token2,"expecting keyWord");
        }
        AST a = parseExp();
        return new If(t,c,a);
    }

    private AST parseLet() {
        LinkedList<Def> link = new LinkedList<Def>();
        Token token = in.peek();
        if (token instanceof Variable){
            while (token instanceof Variable) {
                Variable var = (Variable) token;
                token = in.readToken();
                if (token instanceof KeyWord) {
                    if (token == defKey){
                        token = parseDef(link, var);
                    } else {
                        error(token,"expect :=");
                    }
                }
            }
            if (token instanceof KeyWord){
                if (!(token == inKey)) {
                    error(token,"expect let, in");
                }
            } else {
                error(token,"expect in");
            }
            Def[] arr = new Def[link.size()];
            link.toArray(arr);
            return new Let(arr,parseExp());
        }else error(token, "expect let");
        return null;
    }

    private Token parseDef(LinkedList<Def> link, Variable var){
        Def def = new Def(var,parseExp());
        link.add(def);
        Token token = in.peek();
        if (token instanceof SemiColon){
            token = in.readToken();
            return token;
        } else {
            error(token,"expect ;");
        }
        return null;
    }

    private AST parseMap() {
        Token token = in.readToken();
        LinkedList<Variable> link = new LinkedList<Variable>();

        if (token instanceof Variable) {
            while (token instanceof Variable) {
                Variable var = (Variable) token;
                link.add(var);
                token = in.readToken();
            }
            if (token instanceof KeyWord) {
                if (!(token == toKey)) {
                    error(token, "expect map to");
                }
            } else {
                error(token, "expect to");
            }
        } else if (token instanceof KeyWord) {
            if (!(token == toKey)) {
                error(token, "expect map to");
            }
        }

        Variable[] varArray = new Variable[link.size()];

        link.toArray(varArray);
        AST body = parseExp();
        Map map = new Map(varArray, body);
        return map;
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
                error(token,"expect binary OP");
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
                exp = null;
                return exp;
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
        LinkedList<AST> link = new LinkedList<AST>();
        Token token = in.peek();
        if (token != RightParen.ONLY){
            link.add(parseExp());
            token = in.peek();
            while (token != RightParen.ONLY) {
                if (token instanceof Comma){
                    in.readToken();
                }
                link.add(parseExp());
                token = in.peek();
            }
        }
        in.readToken();
        AST[] arr = new AST[link.size()];
        link.toArray(arr);
        return arr;
    }



    private void error(Token token, String message) throws ParseException{
        System.out.println(in.readToken());
        throw new ParseException(token + " + " + message);
    }
}
