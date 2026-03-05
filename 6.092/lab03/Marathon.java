class Marathon {
    public static int SecondBestRunner(int[] times) {
        int bestId = BestRunner(times);
        int id = 0, n = times.length;
        for (int i = 0; i < n; i++) {
            if (i == bestId) {
                continue;
            }

            if (times[i] < times[id]) {
                id = i;
            }
        }

        return id;
    }

    public static int BestRunner(int[] times) {
        int id = 0, n = times.length;
        for (int i = 1; i < n; i++) {
            if (times[i] < times[id]) {
                id = i;
            }
        }

        return id;
    }

    public static void main (String[] arguments) {
        String[] names = {
                "Elena", "Thomas", "Hamilton", "Suzie", "Phil", "Matt", "Alex",
                "Emma", "John", "James", "Jane", "Emily", "Daniel", "Neda",
                "Aaron", "Kate"
        };

        int[] times = {
                341, 273, 278, 329, 445, 402, 388, 275, 243, 334, 412, 393, 299,
                343, 317, 265
        };

        for (int i = 0; i < names.length; i++) {
            System.out.println(names[i] + ": " + times[i]);
        }

        int bestId = BestRunner(times), secondBestId = SecondBestRunner(times);
        System.out.println("The best runner is " + names[bestId] + " and his/her score is " + times[bestId] + " minutes.");
        System.out.println("The second best runner is " + names[secondBestId] + " and his/her score is " + times[secondBestId] + " minutes.");
    }
}