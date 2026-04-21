import java.util.HashMap;
import java.util.AbstractMap.SimpleEntry;

public class LiveInterval {
    HashMap<String, Integer> begin, end;

    public LiveInterval() {
        begin = new HashMap<String, Integer>();
        end = new HashMap<String, Integer>();
    }

    public void push(String varName, int idx) {
        if (begin.getOrDefault(varName, null) == null) {
            begin.put(varName, idx);
            end.put(varName, idx);
        } else {
            int st = begin.get(varName), ed = end.get(varName);
            st = min(st, idx);
            ed = max(ed, idx);
            begin.put(varName, st);
            end.put(varName, ed);
        }
    }

    public SimpleEntry<Integer, Integer> getInterval(String varName) {
        if (begin.getOrDefault(varName, null) == null) {
            return null;
        } else {
            int st = begin.get(varName), ed = end.get(varName);
            SimpleEntry<Integer, Integer> p = new SimpleEntry<>(st, ed);
            return p;
        }
    }

    private int min(int x, int y) {
        if (x <= y) {
            return x;
        } else {
            return y;
        }
    }
    private int max(int x, int y) {
        if (x >= y) {
            return x;
        } else {
            return y;
        }
    }
}
