package cn.seecoder;
import java.util.regex.Pattern;
public class Lexer{
    public String source;
    public int index;
    public TokenType token;
    public String tokenvalue;
    public Lexer(String s){
        index = 0;
        source = s;
        nextToken();
    }
    private TokenType nextToken(){ //得到下一个token
        char nextChar='\0';
        do{
            nextChar=continueChar();
        }while(Pattern.matches("\\s", String.valueOf(nextChar)));//://s表示所有空白符号，如回车、tap、空格
        if(nextChar=='('){this.token=TokenType.LPAREN;tokenvalue="(";}
        else if(nextChar==')'){this.token=TokenType.RPAREN;tokenvalue=")";}
        else if(nextChar=='.'){this.token=TokenType.DOT;tokenvalue=".";}
        else if(nextChar=='\\'||nextChar=='λ'){this.token=TokenType.LAMBDA;tokenvalue="\\";}
        else if(nextChar=='\0'){this.token=TokenType.EOF;tokenvalue="\0";}
        else {
            if(Pattern.matches("[a-z]",String.valueOf(nextChar))){
                String lcid_line="";
                do {
                    lcid_line=lcid_line+nextChar;
                    nextChar=continueChar();
                } while (Pattern.matches("[a-zA-Z]", String.valueOf(nextChar)));
                index--;
                this.token =TokenType.LCID;
                tokenvalue=lcid_line;
            }
        }
        System.out.println(token.toString());
        return token;
    }
    private char continueChar(){ //得到下一个char
        char result;
        if(index>=source.length()){
            result='\0';
        }
        else {
            result=source.charAt(index);
        }
        index++;
        return result;
    }
    //check token == t
    public boolean next(TokenType t){//检查下一个token是否是t类型，如果是返回真
        return t==token;
    }
    //assert matching the token type, and move next token
    public void match(TokenType t){//如果下一个token是t，得到下一个token
        if(next(t)){nextToken();}
    }
    //skip token  and move next token
    public boolean skip(TokenType t){//如果下一个token是t，那么跳过并返回真
        if(t==token){
            nextToken();
            return true;
        }
            return false;
        //write your code here
    }
    private void translate() {//打印该source，方便测试
        while (index < source.length()) {
            if (!next(null)) { System.out.println(token.toString());}
            nextToken();
        }
        System.out.println(TokenType.EOF.toString());
    }
    public static void main(String[] args) {
        String test="(\\f.\\x.xxxx)";
        Lexer lexer=new Lexer(test);
    }
}
