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
        getToken();
    }
    //get next token
    private void getToken(){
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
    }
    // get next char
    private char continueChar(){
        char result='\0';
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
    public boolean next(TokenType t){
        return t==token;
    }
    //assert matching the token type, and move next token
    public void match(TokenType t){
        if(next(t)){getToken();}
        //write your code here
    }
    //skip token  and move next token
    public boolean skip(TokenType t){
        if(next(t)){
            getToken();
            return true;
        }
        return false;
        //write your code here
    }
    private void translate() {
        while (index < source.length()) {
            getToken();
            if (!next(null)) {
                System.out.println(token.toString());
            }
        }
        System.out.println(TokenType.EOF.toString());
    }
    public static void main(String[] args) {
        String test="(\\f.\\x.x)";
        Lexer lexer=new Lexer(test);
        lexer.translate();
    }
}
