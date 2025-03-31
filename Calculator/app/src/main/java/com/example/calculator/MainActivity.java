package com.example.calculator;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView display;
    private final StringBuilder currentInput = new StringBuilder();
    private boolean lastInputWasOperator = false;
    private boolean lastInputWasDecimal = false;
    private int openParenthesesCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        display = findViewById(R.id.display);
        display.setSingleLine(true);
        display.setHorizontallyScrolling(true);
        setupButtons();
    }

    private void setupButtons() {
        // Number buttons (direct resource references)
        findViewById(R.id.button_0).setOnClickListener(v -> appendNumber("0"));
        findViewById(R.id.button_1).setOnClickListener(v -> appendNumber("1"));
        findViewById(R.id.button_2).setOnClickListener(v -> appendNumber("2"));
        findViewById(R.id.button_3).setOnClickListener(v -> appendNumber("3"));
        findViewById(R.id.button_4).setOnClickListener(v -> appendNumber("4"));
        findViewById(R.id.button_5).setOnClickListener(v -> appendNumber("5"));
        findViewById(R.id.button_6).setOnClickListener(v -> appendNumber("6"));
        findViewById(R.id.button_7).setOnClickListener(v -> appendNumber("7"));
        findViewById(R.id.button_8).setOnClickListener(v -> appendNumber("8"));
        findViewById(R.id.button_9).setOnClickListener(v -> appendNumber("9"));

        // Operation buttons
        findViewById(R.id.button_add).setOnClickListener(v -> appendOperator("+"));
        findViewById(R.id.button_minus).setOnClickListener(v -> appendOperator("-"));
        findViewById(R.id.button_multiply).setOnClickListener(v -> appendOperator("*"));
        findViewById(R.id.button_divide).setOnClickListener(v -> appendOperator("/"));
        findViewById(R.id.button_module).setOnClickListener(v -> appendOperator("%"));

        // Special buttons
        findViewById(R.id.button_point).setOnClickListener(v -> appendDecimalPoint());
        findViewById(R.id.button_ac).setOnClickListener(v -> clearInput());
        findViewById(R.id.button_backspace).setOnClickListener(v -> removeLastCharacter());
        findViewById(R.id.button_equal_to).setOnClickListener(v -> calculateResult());

        // Double bracket button
        findViewById(R.id.button_a).setOnClickListener(v -> {
            if (currentInput.length() == 0 ||
                    lastInputWasOperator ||
                    currentInput.charAt(currentInput.length() - 1) == '(') {
                currentInput.append("(");
                openParenthesesCount++;
            } else {
                if (openParenthesesCount > 0) {
                    currentInput.append(")");
                    openParenthesesCount--;
                } else {
                    currentInput.append("*(");
                    openParenthesesCount++;
                }
            }
            lastInputWasOperator = false;
            lastInputWasDecimal = false;
            updateDisplay();
        });
    }

    private void appendNumber(String number) {
        currentInput.append(number);
        lastInputWasOperator = false;
        updateDisplay();
    }

    private void appendOperator(String operator) {
        if (currentInput.length() == 0) {
            if (operator.equals("-")) {
                currentInput.append(operator);
            }
            return;
        }

        char lastChar = currentInput.charAt(currentInput.length() - 1);
        if (isOperator(lastChar)) {
            currentInput.setLength(currentInput.length() - 1);
        }
        currentInput.append(operator);
        lastInputWasOperator = true;
        lastInputWasDecimal = false;
        updateDisplay();
    }

    private void appendDecimalPoint() {
        if (lastInputWasDecimal) return;

        if (currentInput.length() == 0 || lastInputWasOperator) {
            currentInput.append("0");
        }

        int i = currentInput.length() - 1;
        while (i >= 0 && Character.isDigit(currentInput.charAt(i))) {
            i--;
        }
        if (i >= 0 && currentInput.charAt(i) == '.') {
            return;
        }

        currentInput.append(".");
        lastInputWasDecimal = true;
        lastInputWasOperator = false;
        updateDisplay();
    }

    private void clearInput() {
        currentInput.setLength(0);
        lastInputWasOperator = false;
        lastInputWasDecimal = false;
        openParenthesesCount = 0;
        updateDisplay();
    }

    private void removeLastCharacter() {
        if (currentInput.length() > 0) {
            char lastChar = currentInput.charAt(currentInput.length() - 1);
            if (lastChar == '(') openParenthesesCount--;
            if (lastChar == ')') openParenthesesCount++;
            if (lastChar == '.') lastInputWasDecimal = false;
            if (isOperator(lastChar)) lastInputWasOperator = false;

            currentInput.deleteCharAt(currentInput.length() - 1);
            updateDisplay();
        }
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '%';
    }

    private void calculateResult() {
        try {
            while (openParenthesesCount > 0) {
                currentInput.append(")");
                openParenthesesCount--;
            }

            String expression = currentInput.toString();
            if (expression.isEmpty()) return;

            expression = expression.replace("%", "/100*");
            double result = evaluateExpression(expression);

            currentInput.setLength(0);
            if (result == (long) result) {
                currentInput.append((long) result);
            } else {
                currentInput.append(result);
            }

            updateDisplay();
        } catch (ArithmeticException e) {
            showError("Math error: " + e.getMessage());
        } catch (Exception e) {
            showError("Invalid expression");
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        clearInput();
    }

    private void updateDisplay() {
        display.setText(currentInput.length() > 0 ? currentInput.toString() : "0");
    }

    private double evaluateExpression(String expression) {
        expression = expression.replaceAll("\\s+", "");

        String finalExpression = expression;
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < finalExpression.length()) ? finalExpression.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < finalExpression.length()) {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) {
                        double divisor = parseFactor();
                        if (divisor == 0) throw new ArithmeticException("Division by zero");
                        x /= divisor;
                    }
                    else if (eat('%')) {
                        double divisor = parseFactor();
                        if (divisor == 0) throw new ArithmeticException("Modulo by zero");
                        x %= divisor;
                    }
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    if (!eat(')')) throw new RuntimeException("Missing ')'");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(finalExpression.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                return x;
            }
        }.parse();
    }
}