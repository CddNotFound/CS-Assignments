class FooCorporation {
    public static void CalculateSalary(double basePay, int workingHours) {
        if (basePay < 8) {
            System.out.println("error");
            return ;
        }
        if (workingHours > 60) {
            System.out.println("error");
            return ;
        }

        double salary = 0;
        salary = Math.min(workingHours, 40) * basePay
               + Math.max(workingHours - 40, 0) * basePay * 1.5;

        System.out.println("Salary: " + salary);
    }
    public static void main(String[] args) {
        CalculateSalary(7.5, 35);
        CalculateSalary(8.2, 47);
        CalculateSalary(10, 73);
    }
}