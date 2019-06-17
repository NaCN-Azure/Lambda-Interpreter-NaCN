package cn.seecoder;
public class Interpreter {
    Parser parser;
    AST astAfterParser;
    int evalAST_times,shift_times,subst_times;
    public Interpreter(Parser p){
        parser = p;
        astAfterParser = p.parse();
        //*System.out.println("After parser:"+astAfterParser.toString());
    }
    private  boolean isAbstraction(AST ast){
        return ast instanceof Abstraction;
    }
    private  boolean isApplication(AST ast){
        return ast instanceof Application;
    }
    private  boolean isIdentifier(AST ast){
        return ast instanceof Identifier;
    }
    public AST eval(){
        return evalAST(astAfterParser);
    }
    private   AST evalAST(AST ast){
        //具体收拢ast语法树的实现，分了很多种情况
        evalAST_times++;
        System.out.println("第"+evalAST_times+"次ast树："+ast);
        while (true) {
            if (isApplication(ast)) {//如果是应用ast
                if (isApplication(((Application) ast).lhs)) {//ast左枝是应用的话
                    System.out.println(("eval 左树:" + ((Application) ast).lhs));
                    ((Application) ast).lhs = evalAST(((Application) ast).lhs);//对左枝进行递归应用的化简
                    if (isApplication(((Application) ast).lhs)) {//左枝仍然是应用那么返回它，这样防止上一步以及下面的操作出现死栈溢出。
                        return ast;
                    }
                    System.out.println("eval 右树:" + ast);
                } else if (isAbstraction(((Application) ast).lhs)) {//ast左枝是一个抽象
                    if  (isApplication(((Application) ast).rhs)) {//右枝是一个应用的话
                        ((Application) ast).rhs = evalAST((Application) ((Application) ast).rhs);//对右枝进行递归应用的化简
                        System.out.println("eval 右树：" + ast);
                    }
                    System.out.println("替换之前：" + ast);
                    ast = substitute(((Abstraction) ((Application) ast).lhs).body,((Application) ast).rhs);//右边的应用被应用于左边的抽象函数，替换
                    System.out.println("替换之后：" + ast);
                } else {//ast左枝是基本的identifier
                    if ((isApplication(((Application) ast).rhs))||(isAbstraction(((Application) ast).rhs))){//右枝不是identifier
                        ((Application) ast).rhs = evalAST(((Application) ast).rhs);//构造应用继续化简
                        System.out.println("eval 右树：" + ast);
                        return ast;//这里返回数值，已经化简到了最底层
                    } else {
                        return ast;//否则返回，化简完毕
                    }
                }
            } else if (isAbstraction(ast)) {//如果是抽象ast
                Abstraction abs = ((Abstraction)ast);//根据该抽象新建一个abs（被去掉了最外面的一层的\.）
                abs.body = evalAST(((Abstraction)abs).body);//抽象的递归化简
                System.out.println("abstraction抽象：" + ast);
                return ast;
            }else{//如果只剩下identifier
                System.out.println("Identifier值："+ast);//化简终了
                return ast;
            }
        }
    }
    private AST substitute(AST node,AST value){//substitute：替换操作，就是语法树的代入计算
        return shift(-1,subst(node,shift(1,value,0),0),0);
        //shift(1,value,0)把内层相应自由变量移动+1位置
        //subst(node,shift.....,0)将自由变量移动后的部分进行替换
        //shift(-1,...)将整个部分的自由变量再降回来，这个是因为一层替换只需要将相应部分提高1，相当于删去一层lambda
    }
    /**
     *  value替换node节点中的变量：
     *  如果节点是Applation，分别对左右树替换；
     *  如果node节点是abstraction，替入node.body时深度得+1；
     *  如果node是identifier，则替换De Bruijn index值等于depth的identifier（替换之后value的值加深depth）
     *@param value 替换成为的value
     *@param node 被替换的整个节点
     *@param depth 外围的深度       
     *@return AST
     *@exception  (方法有异常的话加)
     */
    private AST subst(AST node, AST value, int depth){//替换操作的具体实现，node：ast树，value：数值，depth：外层深度
        subst_times++;
        System.out.println("node数值："+node);
        if(isApplication(node)){
            return new Application(
                    subst(((Application) node).lhs,value,depth),
                    subst(((Application) node).rhs,value,depth)
            );//应用情况，向下传递
        }else if(isAbstraction(node)){
            return new Abstraction(
                    (((Abstraction)node).param),
                    subst(((Abstraction)node).body,value,depth+1)
            );//抽象情况，param不变，body被替换且深度+1
        }else if(isIdentifier(node)){
            if(depth == ((Identifier)node).getInt()){
                return shift(depth,value,0);//identifier，替换所有值为depth的变量，不改变其他，找到那个所有需要替换的部分，并且深度正确的部分
            }else{
                return node;
            }
        }
        return null;

    }
    /**
     *  De Bruijn index值位移
     *  如果节点是Applation，分别对左右树位移；
     *  如果node节点是abstraction，新的body等于旧node.body位移by（from得+1）；
     *  如果node是identifier，则新的identifier的De Bruijn index值如果大于等于from则加by，否则加0（超出内层的范围的外层变量才要shift by位）.
        *@param by 位移的距离
     *@param node 位移的节点
     *@param from 内层的深度   
     *@return AST
     *@exception  (方法有异常的话加)
     */
    private AST shift(int by, AST node,int from){//shift方法通过不断递归寻找De Brui index从而实现贝塔规约，也就是位移寻找
        //by：需要位移的距离，node：ast树，from：内层深度
        shift_times++;
        System.out.print("shift操作,by:"+by+" from:"+from);
        System.out.println("  shift操作,node:"+node);
        if(isApplication(node)){
            return new Application(
                    shift(by,((Application) node).lhs,from),
                    shift(by,((Application) node).rhs,from)
            );//应用的情况，只需要分别向下传递
        }else if (isAbstraction(node)){
            return new Abstraction(
                    (Identifier)((Abstraction) node).param,
                    shift(by,((Abstraction)node).body,from+1)
            );//抽象的情况，替换body，from+1，from保存了内层的深度
        }else if(isIdentifier(node)){//identifier的情况，新identifier是德布鲁因数值
            int temp= ((Identifier) node).getInt();
            if(temp>=from){//大于from（内层深度）要加by，字母不属于里面的东西
                temp=temp+by;
            }//否则不用加by
            return new Identifier("".toString(),String.valueOf(temp));
        }
        return null;
    }
    static String ZERO = "(\\f.\\x.x)";
    static String SUCC = "(\\n.\\f.\\x.f (n f x))";
    static String ONE = app(SUCC, ZERO);
    static String TWO = app(SUCC, ONE);
    static String THREE = app(SUCC, TWO);
    static String FOUR = app(SUCC, THREE);
    static String FIVE = app(SUCC, FOUR);
    static String PLUS = "(\\m.\\n.((m "+SUCC+") n))";
    static String POW = "(\\b.\\e.e b)";       // POW not ready
    static String PRED = "(\\n.\\f.\\x.n(\\g.\\h.h(g f))(\\u.x)(\\u.u))";
    static String SUB = "(\\m.\\n.n"+PRED+"m)";
    static String TRUE = "(\\x.\\y.x)";
    static String FALSE = "(\\x.\\y.y)";
    static String AND = "(\\p.\\q.p q p)";
    static String OR = "(\\p.\\q.p p q)";
    static String NOT = "(\\p.\\a.\\b.p b a)";
    static String IF = "(\\p.\\a.\\b.p a b)";
    static String ISZERO = "(\\n.n(\\x."+FALSE+")"+TRUE+")";
    static String LEQ = "(\\m.\\n."+ISZERO+"("+SUB+"m n))";
    static String EQ = "(\\m.\\n."+AND+"("+LEQ+"m n)("+LEQ+"n m))";
    static String MAX = "(\\m.\\n."+IF+"("+LEQ+" m n)n m)";
    static String MIN = "(\\m.\\n."+IF+"("+LEQ+" m n)m n)";
    private static String app(String func, String x){
        return "(" + func + x + ")";
    }
    private static String app(String func, String x, String y){
        return "(" +  "(" + func + x +")"+ y + ")";
    }
    private static String app(String func, String cond, String x, String y){
        return "(" + func + cond + x + y + ")";
    }
    public static void main(String[] args) {
        // write your code here
        String[] sources = {
                ZERO,//0
                ONE,//1
                TWO,//2
                THREE,//3
                app(PLUS, ZERO, ONE),//4
                app(PLUS, TWO, THREE),//5
                app(POW, TWO, TWO),//6
                app(PRED, ONE),//7
                app(PRED, TWO),//8
                app(SUB, FOUR, TWO),//9
                app(AND, TRUE, TRUE),//10
                app(AND, TRUE, FALSE),//11
                app(AND, FALSE, FALSE),//12
                app(OR, TRUE, TRUE),//13
                app(OR, TRUE, FALSE),//14
                app(OR, FALSE, FALSE),//15
                app(NOT, TRUE),//16
                app(NOT, FALSE),//17
                app(IF, TRUE, TRUE, FALSE),//18
                app(IF, FALSE, TRUE, FALSE),//19
                app(IF, app(OR, TRUE, FALSE), ONE, ZERO),//20
                app(IF, app(AND, TRUE, FALSE), FOUR, THREE),//21
                app(ISZERO, ZERO),//22
                app(ISZERO, ONE),//23
                app(LEQ, THREE, TWO),//24
                app(LEQ, TWO, THREE),//25
                app(EQ, TWO, FOUR),//26
                app(EQ, FIVE, FIVE),//27
                app(MAX, ONE, TWO),//28
                app(MAX, FOUR, TWO),//29
                app(MIN, ONE, TWO),//30
                app(MIN, FOUR, TWO),//31
        };

            String source = "(\\f.\\x.(x f)) d";
            System.out.println("test:"+source);
            Lexer lexer = new Lexer(source);
            Parser parser = new Parser(lexer);
            Interpreter interpreter = new Interpreter(parser);
            AST result = interpreter.eval();
            System.out.println("调用evalAST:"+interpreter.evalAST_times+"次;"+"调用subti:"+interpreter.subst_times+"次;"+"调用shift:"+interpreter.shift_times+"次");
            System.out.println("test:" + result.toString());

    }
}
