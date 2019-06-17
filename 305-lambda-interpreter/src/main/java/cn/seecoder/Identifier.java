package cn.seecoder;

public class Identifier extends AST {
    String name; //名字
    String value;//De Bruijn index值
    public Identifier(String n,String v){
        name = n;
        value = v;
    }
    public Integer getInt(){return Integer.parseInt(value);}
    public String toString(){
        return value;
    }
}
