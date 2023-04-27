import org.python.util.PythonInterpreter;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

public class PythonTokenizer {

    private static final String fileWithCodePath = "C:\\Users\\Aleksej\\Desktop\\Лабораторные\\МСИС\\3\\in1.py";
    public static LinkedList<TToken> tokenFlow;
    private static int next = 0;
    private static boolean isInsideChoiceBlock = false;
    private static boolean isFunctionIOParams = false;
    private static boolean isAssignNow = false;
    private static int LIMIT = 0;
    public static HashMap<TToken, TokenInformation> fullChapinMetric = new HashMap<>();
    public static HashMap<TToken, TokenInformation> ioChapinMetric = new HashMap<>();

    private static void countLIMIT(){LIMIT = tokenFlow.size() - 1;}

    public static void main(String[] args) {
        count();
    }

    public static void count() {
//        operators.clear();
//        operands.clear();
        tokenFlow = tokenize(readCodeFromFile(fileWithCodePath));
        countLIMIT();

        TToken item;
        for (int i = 0; i < tokenFlow.size(); i++) {
            item = tokenFlow.get(i);
            switch (item.tokenType()){
                case OPERATION -> {
                    switch (item.tokenValue()){
                        case ":" -> exitFromChoiceBlock();
                        case ")","}" -> isFunctionIOParams = false;
                        case "{" -> isFunctionIOParams = true;
                        case "=" -> isAssignNow = false;
                    }
                }
                case INNER_FUNCTION, IDENTIFIER -> {
                    if (retToSave(i) && formattedIO()) {
                        tokenFlow.addAll(i + 5, getTokenFlowFromString(tokenFlow.get(i + 3).tokenValue()));
                        countLIMIT();
                    }
                    if (!tokenFlow.get(i-1).tokenValue().equals("def")){
                        if (retToSave(i) && function()) {
                            if (item.tokenValue().matches("(input|print)")) isFunctionIOParams = true;
                        }
                        else if (retToSave(i) && assignment() || retToSave(i) && getArrayElem()) {
                            isAssignNow=true;
                            addToMap(fullChapinMetric,item);
                        }
                        else if (retToSave(i) && term(TokenType.IDENTIFIER)) addToMap(fullChapinMetric, item);
                    }
                }
                case KEY_WORD -> {
                    if (((retToSave(i) && ifStatement()) ||
                            (retToSave(i) && whileStatement()) ||
                            (retToSave(i) && forStatement())) &&
                            enterInsideChoiceBlock())
                    {}
                }
            }
        }
        System.out.println("\tFull Chapin's metric");
        printMap(fullChapinMetric);
        System.out.println("Summary spen: "+getSum(fullChapinMetric)+"\n\n\n");

        System.out.println("\tInput-output Chapin's metric");
        printMap(ioChapinMetric);
        System.out.println("Summary spen: "+getSum(ioChapinMetric)+"\n\n\n");
    }

    public static boolean enterInsideChoiceBlock(){
        return isInsideChoiceBlock = true;
    }

    public static void exitFromChoiceBlock(){
        isInsideChoiceBlock = false;
    }

    public static int getSum(HashMap<TToken,TokenInformation> hashMap){
        int sum=0;
        for (TokenInformation information:hashMap.values()) sum+=information.count;
        return sum;
    }

    private static LinkedList<TToken> getTokenFlowFromString(String string){
        StringBuilder params = new StringBuilder(string);
        StringBuilder result = new StringBuilder();
        int start, end;

        while ((start = params.indexOf("{")) >= 0 && (end = params.indexOf("}")) >= 0){
            result.append(params.substring(start,end+1));
            params.delete(start, end+1);
        }

        return tokenize(result.toString());
    }

    private static void printMap(HashMap<TToken, TokenInformation> map){
        map.forEach((key, value) -> {System.out.println(key.tokenValue() + " spen: "+ (value.count-1)+ " variableGroup: "+ value.variableGroup);});
    }

    private static void addToMap(HashMap<TToken, TokenInformation> map, TToken item){
        TokenInformation newInformation = new TokenInformation();
        if (isInsideChoiceBlock) newInformation.variableGroup = EVariableGroup.CHOICE;
        else if (isFunctionIOParams) newInformation.variableGroup = EVariableGroup.PROCESSING;
        else if (isAssignNow) newInformation.variableGroup = EVariableGroup.TROPHIC;

        if (map.putIfAbsent(item, newInformation) != null) {
            TokenInformation toEdit = map.get(item);
            toEdit.count++;
            if ((isInsideChoiceBlock || isFunctionIOParams || isAssignNow) && toEdit.variableGroup.compareTo(newInformation.variableGroup) <= 0)
                toEdit.variableGroup = newInformation.variableGroup;
            else if (toEdit.variableGroup.compareTo(EVariableGroup.MODIFIED)<0) toEdit.variableGroup = EVariableGroup.MODIFIED;
        }
        if (isFunctionIOParams)
            if (ioChapinMetric.putIfAbsent(item, newInformation) != null) {
            TokenInformation toEdit = ioChapinMetric.get(item);
            toEdit.count++;
            if ((isInsideChoiceBlock || isFunctionIOParams || isAssignNow) && toEdit.variableGroup.compareTo(newInformation.variableGroup) <= 0)
                toEdit.variableGroup = newInformation.variableGroup;
            else if (toEdit.variableGroup.compareTo(EVariableGroup.MODIFIED)<0) toEdit.variableGroup = EVariableGroup.MODIFIED;
        }
    }

    private static String readCodeFromFile(String path){
        File f = new File(path);
        StringBuilder res = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(f.toPath()))) {
            String tempLine;
            while ((tempLine = reader.readLine()) != null) res.append(tempLine).append('\n');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return res.toString();
    }

    private static TokenType getTokenType(String[] lineGroup){
        TokenType res = null;
        switch (lineGroup[1]){
            case "NAME"->{
                if (lineGroup[2].matches(TokensMap.keyWords)) res = TokenType.KEY_WORD;
                else if (lineGroup[2].matches(TokensMap.logicalOPs)) res = TokenType.OPERATION;
                else if (lineGroup[2].matches(TokensMap.inner_functions)) res = TokenType.INNER_FUNCTION;
                else if (lineGroup[2].matches(TokensMap.build_in_vars)) res = TokenType.BUILD_IN_VAR;
                else if (lineGroup[2].matches(TokensMap.identifiers)) res = TokenType.IDENTIFIER;
            }
            case "STRING"->res = TokenType.STRING;
            case "NUMBER"->res = TokenType.NUMBER;
            case "OP"->res = TokenType.OPERATION;
            case "COMMENT" -> res = TokenType.COMMENT;
            default -> res = TokenType.UNKNOWN;
        }
        return res;
    }

    public static LinkedList<TToken> tokenize(String code) {
        StringWriter a = new StringWriter(100);
        LinkedList<TToken> res = new LinkedList<>();

        try (PythonInterpreter interpreter = new PythonInterpreter()) {
            interpreter.setIn(new StringReader(code));
            interpreter.setOut(a);
            interpreter.execfile("C:\\Users\\Aleksej\\Downloads\\jython2.7.3\\Lib\\tokenize.py");
        }

        Scanner vConsole = new Scanner(a.toString());
        String[] lineGroup;
        while (vConsole.hasNextLine()){
            lineGroup = vConsole.nextLine().split("\t");
            lineGroup[2] = lineGroup[2].substring(1, lineGroup[2].length()-1);
            res.add(new TToken(lineGroup[2], getTokenType(lineGroup)));
        }

//        res.forEach(item -> {System.out.println(item.tokenValue() +" "+ item.tokenType());});

        return res;
    }

    private static boolean termOP(final String expected){
        if (next>LIMIT) return false;
        return tokenFlow.get(next++).tokenValue().equals(expected);
    }

    private static boolean term(final TokenType expected){
        if (next>LIMIT) return false;
        return tokenFlow.get(next++).tokenType() == expected;
    }
    /**
    *    functions detecting
    */
    private static boolean function(){
        int save = next;
        return (anyTypeOfFunction() && termOP("(") && params_enc() && termOP(")"))||
                (retToSave(save) && formattedIO())||
                (retToSave(save) && anyTypeOfFunction() && termOP("(") && term(TokenType.STRING) && termOP("*") && term(TokenType.NUMBER) && termOP(")"));
    }

    private static boolean formattedIO(){
        int save = next;
        return (termOP("input") && termOP("(") && termOP("f") && term(TokenType.STRING) && termOP(")"))||
                (retToSave(save) && termOP("print") && termOP("(") && termOP("f") && term(TokenType.STRING) && termOP(")"));
    }

    private static boolean anyTypeOfFunction(){
        int save = next;
        return (term(TokenType.IDENTIFIER)) || (retToSave(save) && term(TokenType.INNER_FUNCTION));
    }

    private static boolean retToSave(final int save) {next = save; return true;}

    private static boolean getArrayElem(){
        return (term(TokenType.IDENTIFIER) && termOP("[") && expression() && termOP("]"));
    }

    private static boolean expression(){
        int save = next;
        return expressionWithBraces() ||
                (retToSave(save) && operand() && anyOperation() && anyOperation() && expression()) ||
                (retToSave(save) && operand() && anyOperation() && expression()) ||
                (retToSave(save) && operand());
    }

    private static boolean expressionWithBraces(){
        int save = next;
        return (retToSave(save) && termOP("(") && expression() && termOP(")") && anyOperation() && expression()) ||
                (retToSave(save) && termOP("(") && expression() && termOP(")"));
    }

    private static boolean anyOperation(){
        return (checkArifmOperation());
    }

    private static boolean operand(){
        int save = next;
        return (retToSave(save) && term(TokenType.NUMBER))||
                (retToSave(save) && method())||
                (retToSave(save) && function())||
                (retToSave(save) && getArrayElem()) ||
                (retToSave(save) && term(TokenType.BUILD_IN_VAR)) ||
                (retToSave(save) && term(TokenType.KEY_WORD)) ||
                (retToSave(save) && term(TokenType.IDENTIFIER))||
                (retToSave(save) && term(TokenType.STRING))||
                (retToSave(save) && termOP("[") && term(TokenType.STRING) && termOP("]") && termOP("*") && term(TokenType.NUMBER))||
                (retToSave(save) && arrayCreating());
    }

    private static boolean method(){
        return term(TokenType.IDENTIFIER) && termOP(".") && function();
    }

    private static boolean arrayCreating(){
        return (termOP("[") && params_enc() && termOP("]"));
    }

    private static boolean checkArifmOperation(){
        int save = next;
        return (retToSave(save) && tokenFlow.get(next++).tokenValue().matches(TokensMap.operations))||
                (retToSave(save) && tokenFlow.get(next++).tokenValue().matches(TokensMap.logicalOPs));
    }

    private static boolean params_enc(){
        int save = next;
        return (operand() && termOP(",") && params_enc()) ||
                (retToSave(save) && operand()) ||
                (retToSave(save));
    }

    /**
    *    assignments detecting
    */

    private static boolean assignment(){
        int save = next;
        return (retToSave(save) && operand() && termOP("=") && expression());
    }

    /**
    *   recursive if detecting
    */

    private static boolean ifStatement(){
        int save = next;
        return (retToSave(save) && termOP("if") && expression() && termOP(":")) ||
             (retToSave(save) && termOP("elif") && expression() && termOP(":"));
    }
    private static boolean whileStatement(){
        int save = next;
        return (retToSave(save) && termOP("while") && expression() && termOP(":"));
    }
    private static boolean forStatement(){
        int save = next;
        return (retToSave(save) && termOP("for") && identifierSequence() && termOP("in") && sequenceSN() && termOP(":"))||
            (retToSave(save) && termOP("for") && identifierSequence() && termOP("in") && term(TokenType.IDENTIFIER) && termOP(":"))||
            (retToSave(save) && termOP("for") && identifierSequence() && termOP("in") && termOP("range") && termOP("(") && params_enc() && termOP(")") && termOP(":"));
    }

    private static boolean sequenceSN(){
        int save = next;
        return  (retToSave(save) && term(TokenType.NUMBER) && termOP(",") && sequenceSN())||
                (retToSave(save) && term(TokenType.STRING) && termOP(",") && sequenceSN())||
                (retToSave(save) && term(TokenType.STRING))||
                (retToSave(save) && term(TokenType.NUMBER));
    }

    private static boolean identifierSequence(){
        int save = next;
        return (retToSave(save) && term(TokenType.IDENTIFIER) && termOP(",") && identifierSequence()) ||
                (retToSave(save) && term(TokenType.IDENTIFIER));
    }

    private static boolean curlyBracketsEnc(){
        return (termOP("{") && params_enc() && termOP("}"));
    }

}
record TToken(String tokenValue, TokenType tokenType){
    static EVariableGroup variableGroup = EVariableGroup.UNDEFINED;
}
class TokenInformation{
    public int count = 1;
    public EVariableGroup variableGroup = EVariableGroup.UNDEFINED;
}
enum EVariableGroup{UNDEFINED, TROPHIC, PROCESSING, MODIFIED, CHOICE}