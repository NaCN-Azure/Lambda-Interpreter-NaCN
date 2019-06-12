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
    int term_times,application_times,atom_times;
    private AST term(ArrayList<String> ctx){//term方法用来进行判断诸如\g.g 将后面的g转换为相应的数字
        term_times++;//term可以是一个应用，或者是一个抽象+term
        String param;String param_value;//param是\g.g中的第一个g，param_value是它的优先级
        if(lexer.skip(TokenType.LAMBDA)){
            if(lexer.next(TokenType.LCID)){
                param=lexer.tokenvalue;
                lexer.match(TokenType.LCID);
                if(lexer.skip(TokenType.DOT)){//以上的所有条件用来判断是否为\g.g的组成
                    ctx.add(0,param);//依次利用ctx数组来判断\g.g的优先顺序，后面的数永远加在0位
                    param_value=""+ctx.indexOf(param);//param_value是ctx中g所在位置
                    AST a=term(ctx);//开始递归创建一个小ast
                    ctx.remove(ctx.indexOf(param));//由于\g.g g应用的位置不同，所以一点一点remove ctx，把param所在的数字取出，最后由于递归的原因，会remove空
                    return new Abstraction(new Identifier(param,param_value),a);//因为你遇到的是\g.g 所以返回一个抽象类
                }
            }
        }
        else {return application(ctx);}//如果不是/g.g构成，比如遇到了（、）、LICD那么就是一个应用，利用application方法连接
        return null;
    }
    private AST application(ArrayList<String> ctx){//application方法用来连接左右树
        application_times++;//应用可以是一个应用+atom，也可以就是一个atom
        AST lhs=atom(ctx);//根据ctx构建一个左计算为主的应用左数
        AST rhs;
        while(true){
            rhs=atom(ctx);//根据ctx构建右树
            if(rhs==null){//当右树为空的时候，返回左树，这样的话左树一直是最长的一支树
                return lhs;
            }
            else {
               lhs= new Application(lhs,rhs);//把右树和左数拼在一起形成新的左数
            }
        }
    }
    private AST atom(ArrayList<String> ctx){//atom，最基础的应用底层，运算的最小单位
        atom_times++;//atom可以是单个字母，或者是括号括起来里的一个term
        String param;String param_value;
        if(lexer.skip(TokenType.LPAREN)){//遇到了括号
            AST a=term(ctx);//括号里面又是一个新的语法树，进行构建
            if(lexer.skip(TokenType.RPAREN)){return a;}//关掉括号，返回这个树
        }
        else if(lexer.next(TokenType.LCID)){//遇到了LCID，这一块是形如制作\f.\g.g f的后面的g f部分
            param=lexer.tokenvalue;
            param_value=""+ctx.indexOf(param);//根据param从ctx取出数字，代表优先程度，数字越大优先度越高
            lexer.match(TokenType.LCID);
            return new Identifier(param,param_value);//构造最底层的identifier，这是递归和树木的末端
        }
        return null;
    }
    public static void main(String[] args) {
        Lexer lexer = new Lexer("(\\n.\\f.\\x.f (n f x))");
        Parser parser = new Parser(lexer);
        AST ast = parser.parse();
        System.out.println(ast);
        System.out.println("调用term:"+parser.term_times+"次；调用app："+parser.application_times+"次；调用atom："+parser.atom_times+"次");
    }
}
