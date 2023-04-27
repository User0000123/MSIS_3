import java.util.HashMap;

public class TokensMap {
    private HashMap<String, TokenType> mainKeywords;
    static String keyWords = "(def|break|continue|f|if|else|elif|for|return|while)";
    static String logicalOPs = "(as|and|with|in|is|not)";
    static String inner_functions = "(abs|bool|bytes|chr|hash|len|pow|print|sorted|type|input|isdigit|int|range)";
    static String identifiers = "[a-zA-Z_]+[a-zA-Z0-9_]*";
    static String build_in_vars = "(__name__|False|True|None)";
    static String operations = "(\\+|\\-|\\/|\\*|\\>\\=|\\>|\\<\\=|\\<|\\=\\=|\\!\\=|\\%)";
    static String delimiters = "(\\.|\\;|\\,)";
}
