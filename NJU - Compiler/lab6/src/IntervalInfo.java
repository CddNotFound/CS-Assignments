public class IntervalInfo {
    String name;
    int beginIdx, endIdx;
    public IntervalInfo() {
        this.name = "";
        this.beginIdx = 0;
        this.endIdx = 0;
    }
    public IntervalInfo(String name, int beginIdx, int endIdx) {
        this.name = name;
        this.beginIdx = beginIdx;
        this.endIdx = endIdx;
    }

}
