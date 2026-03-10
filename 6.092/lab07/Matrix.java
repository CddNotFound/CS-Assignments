import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

class Matrix {
    private int n;
    private ArrayList<ArrayList<Integer>> num;

    public Matrix() {
        n = 0;
        num = new ArrayList<ArrayList<Integer>>();
    }

    public void read(String file) {
        try (FileReader fr = new FileReader(file)) {
            BufferedReader br = new BufferedReader(fr);
            String str;
            for (int i = 0; (str = br.readLine()) != null; i++) {
                if (str.length() <= 0) {
                    i -= 1;
                    continue;
                }

                String[] line = str.split("\t");
                n = line.length;
                num.add(new ArrayList<Integer>());
                for (int j = 0; j < n; j++) {
                    num.get(i).add(Integer.valueOf(line[j]));
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void print() {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                System.out.print(num.get(i).get(j) + " ");
            }
            System.out.println();
        }
    }

    public boolean check() {
        boolean[] used = new boolean[n * n + 1];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int x = num.get(i).get(j);
                if (used[x] == true) {
                    System.out.println("Duplicate numbers!");
                    return false;
                }
                used[x] = true;
            }
        }

        int standard = 0;
        for (int i = 0; i < n; i++) {
            standard += num.get(i).get(0);
        }

        for (int i = 0; i < n; i++) {
            int rowSum = 0;
            for (int j = 0; j < n; j++) {
                rowSum += num.get(i).get(j);
            }
            if (rowSum != standard) {
                System.out.println("Row sum diff!");
                return false;
            }
        }

        for (int j = 0; j < n; j++) {
            int colSum = 0;
            for (int i = 0; i < n; i++) {
                colSum += num.get(i).get(j);
            }
            if (colSum != standard) {
                System.out.println("Column sum diff!");
                return false;
            }
        }

        int diagSum1 = 0, diagSum2 = 0;
        for (int i = 0; i < n; i++) {
            diagSum1 += num.get(i).get(i);
            diagSum2 += num.get(i).get(n - 1 - i);
        }
        if (diagSum1 != standard || diagSum2 != standard) {
            System.out.println("Diagonal sum diff!");
            return false;
        }

        return true;
    }
}