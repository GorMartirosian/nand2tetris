package main.compiler.codegeneration;

public enum VariableKinds {
    STATIC("static"),
    FIELD("this"),
    VAR("local"),
    ARG("argument"),
    NONE(null);

    final String segment;
    private VariableKinds(String segment){
        this.segment = segment;
    }
}
