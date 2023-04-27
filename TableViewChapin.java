import java.util.HashMap;
import java.util.Map;

public class TableViewChapin {
    public StringBuilder
            P = new StringBuilder(""),
            M = new StringBuilder(""),
            C = new StringBuilder(""),
            T = new StringBuilder("");


    public TableViewChapin(HashMap<TToken,TokenInformation> map){
        for (Map.Entry<TToken,TokenInformation> item:map.entrySet()){
            switch (item.getValue().variableGroup){
                case PROCESSING -> P.append(item.getKey().tokenValue()).append(",\n");
                case MODIFIED -> M.append(item.getKey().tokenValue()).append(",\n");
                case CHOICE -> C.append(item.getKey().tokenValue()).append(",\n");
                case TROPHIC -> T.append(item.getKey().tokenValue()).append(",\n");
            }
        }
        deleteCommas();
    }

    public void deleteCommas(){
        if (P.length() != 0) P.deleteCharAt(P.lastIndexOf(","));
        if (M.length() != 0) M.deleteCharAt(M.lastIndexOf(","));
        if (C.length() != 0) C.deleteCharAt(C.lastIndexOf(","));
        if (T.length() != 0) T.deleteCharAt(T.lastIndexOf(","));
    }

    public String getP(){
        return this.P.toString();
    }
    public String getM(){
        return this.M.toString();
    }
    public String getC(){
        return this.C.toString();
    }
    public String getT(){
        return this.T.toString();
    }
}
