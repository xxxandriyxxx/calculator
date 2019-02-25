package calculator;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator {


    public static void start() {
        Scanner scan = new Scanner(System.in);
        String str = "";
        while (true) {
            System.out.println("========== CALCULATOR ==========\n" +
                    "Input 'faq' to show instruction.\n" +
                    "Input 'ex' to exit.\n" +
                    "--------------------------------\n" +
                    "Input your arithmetic operations in a single expression:");
            str = scan.nextLine();
            str = str.replaceAll("\\s+", "");
            str = str.toUpperCase();
            switch (str) {
                case "FAQ":
                    showInstruction();
                    break;
                case "EX":
                    System.out.println("Goodbye !!!");
                    return;
                default:
                    str = validation(str);
                    System.out.println("--------------------------------");
                    if (!str.contains("ERROR")) str = calculate(str);
                    if (!str.contains("ERROR")) System.out.println("RESULT = " + str);
                    else System.out.println(str);
                    System.out.println("--------------------------------");
                    break;
            }
        }
    }


    private static String validation(String entStr) {
        int brackets = 0;
        int operators = 0;
        String type = "none";
        String arabicStr = "";
        String tempStr = "";
        boolean find = false;
        char[] operation = {'+', '-', '*', '/', '(', ')'};

        if (entStr.isEmpty()) return "ERROR: Your expression is empty !!!";
        if (entStr.contains("/0")) return "ERROR: Dividing by zero !!!";
        if (entStr.contains(",")) return "ERROR: Use a character '.' instead ',' !!!";

        if ((entStr.matches("\\(?[-]?[1-9].*?") || entStr.matches("\\(?(-0\\.)?[0-9].*?")) && entStr.matches(".*?([0-9]|\\))")) {
            if (!Pattern.compile("[^+\\-/*().\\d]").matcher(entStr).find()) type = "arabic";
        }
        if (entStr.matches("\\(?[-]?[IVXLCDM].*?") && entStr.matches(".*?([IVXLCDM]|\\))")) {
            if (!Pattern.compile("[^+\\-/*()IVXLCDMivxlcdm]").matcher(entStr).find()) type = "roman";
        }

        if (type.equals("roman")) {
            for (int i = 0; i < entStr.length(); i++) {
                for (int j = 0; j < operation.length; j++) {
                    if (entStr.charAt(i) == operation[j]) {
                        if (!tempStr.equals("")) {
                            if (convertToArabic(tempStr).equals("[error]")) {
                                return "ERROR: You entered the incorrect expression !!!";
                            } else {
                                arabicStr = arabicStr + convertToArabic(tempStr);
                                tempStr = "";
                            }
                        }
                        find = true;
                        arabicStr = arabicStr + entStr.charAt(i);
                        break;
                    } else find = false;
                }
                if (!find) {
                    tempStr = tempStr + entStr.charAt(i);
                    if (i == entStr.length() - 1) {
                        if (convertToArabic(tempStr).equals("[error]")) {
                            return "ERROR: You entered the incorrect expression !!!";
                        } else {
                            arabicStr = arabicStr + convertToArabic(tempStr);
                        }
                    }
                }
            }
            entStr = arabicStr;
            System.out.println("--------------------------------");
            System.out.println("Converted to Arabic numerals:");
            System.out.println(entStr);
        }

        for (int i = 0; i < entStr.length(); i++) {
            if (entStr.charAt(i) == '(') brackets ++;
            if (entStr.charAt(i) == ')') brackets --;
            if (entStr.substring(i, i + 1).matches("[+\\-/*.]")) {
                operators += 1;
            } else {
                operators = 0;
            }
            if (operators > 1) return "ERROR: Сheck your operators or points !!!";
            if (brackets < 0) return "ERROR: Сheck your brackets !!!";
        }
        if (brackets != 0) return "ERROR: Сheck your brackets !!!";

        Pattern pat = Pattern.compile("(\\(\\+)|(\\(\\*)|(\\(\\/)|(\\(\\))|(\\+\\))|(\\-\\))|(\\*\\))|(\\/\\))|(\\)\\d)|(\\d\\()|(\\(\\-0[^\\.])");
        Matcher mat = pat.matcher(entStr);
        if (mat.find()) return "ERROR: Сheck your brackets and operators !!!";

        if (type.equals("none")) return "ERROR: You entered the incorrect expression !!!";

        return entStr;
    }


    private static String convertToArabic(String romStr) {
        int arabNum = 0;
        int prev = 0;
        int temp = 0;

        Map<Character, Integer> hm = new HashMap<>();
        hm.put('I', 1);
        hm.put('V', 5);
        hm.put('X', 10);
        hm.put('L', 50);
        hm.put('C', 100);
        hm.put('D', 500);
        hm.put('M', 1000);

        if (romStr.matches("^(M{0,3})(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$")) {
            for (int i = romStr.length() - 1; i >= 0; i--) {
                temp = hm.get(romStr.charAt(i));
                if (temp < prev)
                    arabNum -= temp;
                else
                    arabNum += temp;
                prev = temp;
            }
            return String.valueOf(arabNum);
        } else {
            return "[error]";
        }
    }


    private static String calculate(String entStr) {        //used Reverse Polish Notation
        Stack<Double> dStack = new Stack<>();
        Stack<Character> chStack = new Stack<>();
        int minus = 1;
        int priority;
        double operand;
        String strOperand = "";

        for (int i = 0; i < entStr.length(); i++) {
            priority = getPriority(entStr.charAt(i));
            if ((entStr.charAt(i) == '-' && (i == 0 || entStr.charAt(i - 1) == '('))) {
                minus = -1;
            } else if (priority == 0) {
                while (getPriority(entStr.charAt(i)) == 0) {
                    strOperand += entStr.charAt(i);
                    i++;
                    if (i == entStr.length()) break;
                }
                try {
                    operand = Double.parseDouble(strOperand) * minus;
                    dStack.push(operand);
                    strOperand = "";
                    minus = 1;
                    //System.out.println("operand= " + operand);
                } catch (NumberFormatException e) {
                    return e.getMessage();
                }
                i--;
            } else if (priority > 1) {
                if (chStack.empty() || priority > getPriority(chStack.peek()) || chStack.peek() == '(') {
                    chStack.push(entStr.charAt(i));
                } else {
                    if (dStack.peek() == 0 && chStack.peek() == '/') return "ERROR: Dividing by zero !!!";
                    else {
                        dStack.push(mathAction(dStack.pop(), dStack.pop(), chStack.pop()));
                        //System.out.println("res= " + dStack.peek());
                        i--;
                    }
                }
            } else if (entStr.charAt(i) == '(') {
                chStack.push('(');
            } else if (entStr.charAt(i) == ')') {
                if (chStack.peek() == '(') chStack.pop();
                else {
                    if (dStack.peek() == 0 && chStack.peek() == '/') return "ERROR: Dividing by zero !!!";
                    else {
                        dStack.push(mathAction(dStack.pop(), dStack.pop(), chStack.pop()));
                        //System.out.println("res= " + dStack.peek());
                        i--;
                    }
                }
            }
        }
        while (!chStack.empty()) {
            if (dStack.peek() == 0 && chStack.peek() == '/') {
                return "ERROR: Dividing by zero !!!";
            } else {
                dStack.push(mathAction(dStack.pop(), dStack.pop(), chStack.pop()));
            }
        }
        //double res = dStack.pop();
        //System.out.println("dSteck is emty = " + dStack.empty());
        //System.out.println("chSteck is emty = " + chStack.empty());
        return String.valueOf(dStack.pop());
    }


    private static double mathAction(double b, double a, char operation) {
        double res = 0;
        if (b == 0 && operation == '/') {
            System.out.println("Dividing by zero !!!");
        } else {
            if (operation == '+') res = a + b;
            else if (operation == '-') res = a - b;
            else if (operation == '*') res = a * b;
            else if (operation == '/') res = a / b;
        }
        return res;
    }


    private static int getPriority(char operation) {
        if (operation == '*' || operation == '/') return 3;
        else if (operation == '+' || operation == '-') return 2;
        else if (operation == '(') return 1;
        else if (operation == ')') return -1;
        else return 0;
    }


    private static void showInstruction() {
        System.out.println("=================================== INSTRUCTION ===================================\n" +
                "Application can perform basic math operations such as: addition, subtraction,\n" +
                "multiplication and division.\n" +
                "You can use either Arabic or Roman numerals.\n" +
                "-----------------------------------------------------------------------------------\n" +
                "If you use ARABIC numbers:\n" +
                "- there are such number types you can use: integers, decimal or even fractions;\n" +
                "- the expression should start with zero, open bracket, positive or negative number;\n" +
                "- expression in brackets should start with a positive, negative number or zero.\n" +
                "------- Examples of CORRECT expressions:\n" +
                "-2*8\n" +
                "12/(-3)\n" +
                "0+8*4\n" +
                "(2+2)*2\n" +
                "-0.25+6\n" +
                "(-0.25+6)*(-10-2)\n" +
                "------- Examples of INCORRECT expressions:\n" +
                "12/-3\n" +
                "-0+8*4\n" +
                "(-0+8)*4\n" +
                "2+(-0+8)*4\n" +
                "(0+9))+(9-8)5+(*8+)-6(9+)-\n" +
                "-----------------------------------------------------------------------------------\n" +
                "If you use ROMAN numbers:\n" +
                "- range of numbers varies from -3999 to 3999;\n" +
                "- you can use uppercase or lowercase;\n" +
                "- other rules are the same to rules from an Arabic section.\n" +
                "------- Examples of CORRECT expressions:\n" +
                "XX+V\n" +
                "-XX+V\n" +
                "x*v+dV-LxviII\n" +
                "------- Examples of INCORRECT expressions:\n" +
                "XX.X-VII\n" +
                "XXXX+VV\n" +
                "------- Some Roman numbers:\n" +
                "I = 1               ІV = 4                  CD = 400\n" +
                "V = 5               XL = 40                 DCCC = 800\n" +
                "X = 10              XС = 90                 DCCCLXXXVIII = 888\n" +
                "L = 50              XCIX = 99               СМ = 900\n" +
                "C = 100             CXXV = 125              MD = 1500\n" +
                "D = 500             CC = 200                MMM = 3000\n" +
                "M = 1000            CCLXXXIII = 283         MMMCMXCIX = 3999\n" +
                "===================================================================================");
    }


}
