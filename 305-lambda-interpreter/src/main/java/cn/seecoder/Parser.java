package cn.seecoder;
import java.util.ArrayList;

public class Parser {
    Lexer lexer;
    public Parser(Lexer l){
        lexer = l;
    }
    public AST parse(){
        AST ast = term(new ArrayList<>());
        return ast;
    }
    private AST term(ArrayList<String> ctx){
        String param;String param_value;
        if(lexer.skip(TokenType.LAMBDA)){
            if(lexer.next(TokenType.LCID)){
                param=lexer.tokenvalue;
                lexer.match(TokenType.LCID);
                if(lexer.skip(TokenType.DOT)){
                    ctx.add(0,param);
                    param_value=""+ctx.indexOf(param);
                    AST a=term(ctx);
                    ctx.remove(ctx.indexOf(param));
                    return new Abstraction(new Identifier(param,param_value),a);
                }
            }
        }
        else {return application(ctx);}
        return null;
    }
    private AST application(ArrayList<String> ctx){
        AST lhs=atom(ctx);
        AST rhs;
        while(true){
            rhs=atom(ctx);
            if(rhs==null){
                return lhs;
            }
            else {
               lhs= new Application(lhs,rhs);
            }
        }
    }
    private AST atom(ArrayList<String> ctx){
        String param;String param_value;
        if(lexer.skip(TokenType.LPAREN)){
            AST a=term(ctx);
            if(lexer.skip(TokenType.RPAREN)){return a;}
        }
        else if(lexer.next(TokenType.LCID)){
            param=lexer.tokenvalue;
            param_value=""+ctx.indexOf(param);
            lexer.match(TokenType.LCID);
            return new Identifier(param,param_value);
        }
        return null;
    }
    public static void main(String[] args) {
        Lexer lexer = new Lexer("(\\x.\\y.(x y)x)(\\x.x)(\\y.y)");
        Parser parser = new Parser(lexer);
        AST ast = parser.parse();
    }
}
