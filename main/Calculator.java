package calculator;

import java.util.ArrayList;
import java.util.List;

public class Calculator {

    /**
     * Evaluate statement represented as string.
     *
     * @param statement mathematical statement containing digits, '.' (dot) as decimal mark,
     *                  parentheses, operations signs '+', '-', '*', '/'<br>
     *                  Example: <code>(1 + 38) * 4.5 - 1 / 2.</code>
     * @return string value containing result of evaluation or null if statement is invalid
     */
    public String evaluate(String statement) {
        if (statement == null || statement.length() == 0) return null;
        statement = statement.replace(" ", "");
        if (!checkPermittedSymbols(statement)) return null;

        List<String> tokens = extractTokens(statement);
        if (tokens == null) return null;
        List<String> tokensRPN = reversePolishNotation(tokens);
        if (tokensRPN == null) return null;

        return calculateRPN(tokensRPN);
    }

    /**
     * Permitted symbols are '(', ')', '*', '+', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
     * so the symbol unicode should be 40-43 or 45-57.
     *
     * @param input mathematical statement
     * @return true if input string contains only permitted symbols, otherwise false
     */
    private static boolean checkPermittedSymbols(String input) {
        for (int i = 0; i < input.length(); i++) {
            int symbolCode = (int)input.charAt(i);
            if (symbolCode < 40 || symbolCode == 44 || symbolCode > 57)
                return false;
            // Check that there are no two sequent operators (e.g. "+-")
            if (i > 1 && "+-*/".contains(String.valueOf(input.charAt(i - 1))) && "+-*/".contains(String.valueOf(input.charAt(i))))
                return false;
        }
        return true;
    }

    /**
     * Extract tokens from an input string.
     *
     * @param input mathematical statement
     * @return List<String> containing extracted tokens or null if statement is invalid
     */
    private static List<String> extractTokens(String input) {
        int pos = 0;        // Position of char in input string
        List<String> tokens = new ArrayList<>();

        String temp = "";
        // Extracting tokens
        while (pos < input.length()) {
            int symbolCode = (int)input.charAt(pos);
            // '(', ')', '*', '+', '-', '/'
            if ((symbolCode >= 40 && symbolCode <= 43) || symbolCode == 45 || symbolCode == 47) {
                if (temp.length() > 0) {
                    if (!isDouble(temp))  return null;
                    tokens.add(temp);
                    temp = "";
                }
                tokens.add(String.valueOf(input.charAt(pos)));
            }
            // '.', '0' - '9'
            else temp += input.charAt(pos);
            pos++;
        }
        // Checking last temp string
        if (temp.length() > 0) {
            if (!isDouble(temp)) return null;
            tokens.add(temp);
            temp = "";
        }
        // If last token is '+', '-', '*' or '/', then mathematical statement is not correct
        if ("+-*/".contains(tokens.get(tokens.size() - 1))) return null;

        return tokens;
    }

    /**
     * Check the double number for correctness (like '1.2', not '1..2').
     *
     * @param number string that should be converted to double
     * @return true if number is double, otherwise false
     */
    private static boolean isDouble(String number) {
        try {
            double temp = Double.parseDouble(number);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Transform mathematical statement into reverse Polish notation
     *
     * @param tokens List of input tokens
     * @return List<String> containing tokens in reverse Polish notation or null if mathematical statement is invalid
     */
    public static List<String> reversePolishNotation(List<String> tokens) {
        List<String> result = new ArrayList<>();
        List<String> stack = new ArrayList<>();

        for(String token : tokens)
            if ("+-*/".contains(token)) {
                if (stack.size() > 0) {
                    // Moving top tokens from stack to result while priority(token) <= priority(top token in stack)
                    while (stack.size() > 0 && priority(token) <= priority(stack.get(stack.size() - 1))) {
                        result.add(stack.get(stack.size() - 1));
                        stack.remove(stack.size() - 1);
                    }
                }
                stack.add(token);
            }
            else if (token.equals("(")) {
                stack.add(token);
            }
            else if (token.equals(")")) {
                // Find the position of "(" in stack
                int pos = -1;
                for (int i = 0; i < stack.size(); i++)
                    if (stack.get(i).equals("(")) {
                        pos = i;
                        break;
                    }
                // If there is no "(", then mathematical statement is wrong
                if (pos == -1) return null;
                // Moving top tokens from stack to result till "("
                int i = 0;
                for (i = stack.size() - 1; i > pos; i--) {
                    result.add(stack.get(i));
                    stack.remove(i);
                }
                // Deleting "(" from stack
                stack.remove(i);
            }
            // Numbers
            else {
                result.add(token);
            }
        // Moving stack to result. If remaining tokens in stack are not operators, then mathematical statement is wrong
        for (int i = stack.size() - 1; i >= 0; i--)
            if ("()0123456789.".contains(stack.get(i))) return null;
            else {
                result.add(stack.get(i));
                stack.remove(i);
            }

        return result;
    }

    /**
     * Get the priority of an operation.
     *
     * @param token current operator
     * @return priority
     */
    private static byte priority(String token) {
        if (token.equals("(")) return 1;
        if (token.equals("+") || token.equals("-")) return 2;
        if (token.equals("*") || token.equals("/")) return 3;
        return 4;
    }

    /**
     * Get the result of mathematical statement in reverse Polish notation
     *
     * @param tokens List of tokens in reverse Polish notation
     * @return result of mathematical statement or null
     */
    private static String calculateRPN(List<String> tokens) {
        List<String> stack = new ArrayList<>();
        for(String token : tokens)
            if ("+-*/".contains(token)) {
                switch (stack.size()) {
                    case 0:
                        return null;
                    case 1:
                        if (token.equals("-"))
                            stack.set(0, "-" + stack.get(0));
                        else
                            return null;
                        break;
                    default:
                        try {
                            double elem1 = Double.parseDouble(stack.get(stack.size() - 2));
                            double elem2 = Double.parseDouble(stack.get(stack.size() - 1));
                            double res = 0;
                            if (token.equals("+")) res = elem1 + elem2;
                            else if (token.equals("-")) res = elem1 - elem2;
                            else if (token.equals("*")) res = elem1 * elem2;
                            else {
                                // Check for 0 in denominator
                                if (elem2 == 0.0) return null;
                                else res = elem1 / elem2;
                            }
                            stack.remove(stack.size() - 1);
                            stack.set(stack.size() - 1, String.valueOf(res));
                        }
                        catch (NumberFormatException e) {
                            return null;
                        }
                }
            }
            else {
                stack.add(token);
            }
        // Round to 4 significant digits
        double res = Double.parseDouble(stack.get(stack.size() - 1));
        res = Math.round(res * 10000d) / 10000d;
        // If rounded result is integer, then symbols ".0" in the end should be removed
        if (res % 1 == 0) return String.valueOf(res).substring(0, String.valueOf(res).length() - 2);
        return String.valueOf(res);
    }
}
