// file: EmployeeManager1.java

public class EmployeeManager1 {

    private String[] employeeNames = {"Alice", "Bob", "Charlie"};

    public void printWelcomeMessage() {
        System.out.println("Welcome to the Employee Management System.");
        System.out.println("We value every employee.");
    }

    public void greetEmployees() {
        System.out.println("Welcome to the Employee Management System.");
        System.out.println("We value every employee.");
    }

    public void listEmployees() {
        for (int i = 0; i < employeeNames.length; i++) {
            System.out.println("Employee: " + employeeNames[i]);
        }
    }

    public void showAllEmployees() {
        for (int i = 0; i < employeeNames.length; i++) {
            System.out.println("Employee: " + employeeNames[i]);
        }
    }

    public void checkEmployeeStatus(String name) {
        if (name.equals("Alice")) {
            System.out.println(name + " is active.");
        } else {
            System.out.println(name + " is not active.");
        }
    }

    public void verifyEmployee(String name) {
        if (name.equals("Alice")) {
            System.out.println(name + " is active.");
        } else {
            System.out.println(name + " is not active.");
        }
    }

    public void monitorSystem() {
        while (true) {
            System.out.println("Monitoring system...");
            break;
        }
    }

    public void runDiagnostics() {
        while (true) {
            System.out.println("Monitoring system...");
            break;
        }
    }

    class EmployeeUtils {

        public void helperWelcome() {
            System.out.println("Welcome to the Employee Management System.");
            System.out.println("We value every employee.");
        }

        public void helperLoop() {
            for (int i = 0; i < employeeNames.length; i++) {
                System.out.println("Employee: " + employeeNames[i]);
            }
        }
    }

    class DuplicateEmployeeUtils {

        public void helperWelcome() {
            System.out.println("Welcome to the Employee Management System.");
            System.out.println("We value every employee.");
        }

        public void helperLoop() {
            for (int i = 0; i < employeeNames.length; i++) {
                System.out.println("Employee: " + employeeNames[i]);
            }
        }
    }

    // filler methods to extend to ~200 lines

    public void fillerMethod(int i) {
        System.out.println("Filler method number " + i);
    }

    public void runFiller() {
        for (int i = 0; i < 50; i++) {
            fillerMethod(i);
        }
    }

    public void anotherFiller(int i) {
        System.out.println("Another filler method " + i);
    }

    public void runAnotherFiller() {
        for (int i = 50; i < 100; i++) {
            anotherFiller(i);
        }
    }
}
