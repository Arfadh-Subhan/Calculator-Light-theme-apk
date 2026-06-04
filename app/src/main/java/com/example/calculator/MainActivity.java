package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvDisplay;
    private TextView tvExpression;

    private String currentInput = "0";

    private BigDecimal firstValue = null;
    private BigDecimal secondValue = null;

    private String operator = "";
    private String lastOperator = "";
    private BigDecimal lastOperand = null;

    private boolean isNewInput = true;
    private boolean justCalculated = false;

    private final MathContext mc = new MathContext(12);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDisplay = findViewById(R.id.tvDisplay);
        tvExpression = findViewById(R.id.tvExpression);

        int[] buttons = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
                R.id.btn4, R.id.btn5, R.id.btn6,
                R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnDot, R.id.btnAC,
                R.id.btnPlusMinus, R.id.btnPercent,
                R.id.btnPlus, R.id.btnMinus,
                R.id.btnMultiply, R.id.btnDivide,
                R.id.btnEqual
        };

        for (int id : buttons) {
            findViewById(id).setOnClickListener(this);
        }

        updateDisplay();
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.btnAC) {
            clearAll();
            return;
        }

        if (id == R.id.btn0 || id == R.id.btn1 || id == R.id.btn2 ||
                id == R.id.btn3 || id == R.id.btn4 || id == R.id.btn5 ||
                id == R.id.btn6 || id == R.id.btn7 || id == R.id.btn8 ||
                id == R.id.btn9 || id == R.id.btnDot) {

            inputNumber(((Button) v).getText().toString());
            return;
        }

        if (id == R.id.btnPlusMinus) {
            toggleSign();
            return;
        }

        if (id == R.id.btnPercent) {
            applyPercent();
            return;
        }

        if (id == R.id.btnEqual) {
            calculate(true);
            return;
        }

        if (id == R.id.btnPlus ||
                id == R.id.btnMinus ||
                id == R.id.btnMultiply ||
                id == R.id.btnDivide) {

            setOperator(((Button) v).getText().toString());
        }
    }

    private void inputNumber(String value) {

        if (justCalculated) {
            clearAll();
        }

        if (isNewInput) {
            currentInput = value.equals(".") ? "0." : value;
            isNewInput = false;
        } else {

            if (value.equals(".") && currentInput.contains(".")) return;

            if (currentInput.equals("0") && !value.equals(".")) {
                currentInput = value;
            } else {
                currentInput += value;
            }
        }

        updateDisplay();
    }

    private void setOperator(String newOp) {

        BigDecimal input = new BigDecimal(currentInput);

        if (firstValue == null) {
            firstValue = input;
        } else if (!operator.isEmpty() && !isNewInput) {
            firstValue = compute(firstValue, input, operator);
            currentInput = format(firstValue);
        }

        operator = newOp;
        isNewInput = true;
        justCalculated = false;

        tvExpression.setText(format(firstValue) + " " + operator);
    }

    private void calculate(boolean fromEquals) {

        if (operator.isEmpty()) return;

        BigDecimal input = new BigDecimal(currentInput);

        if (!justCalculated) {
            secondValue = input;
            lastOperand = input;
        }

        BigDecimal result = compute(firstValue, secondValue, operator);

        if (result == null) {
            tvDisplay.setText("Error");
            clearAll();
            return;
        }

        tvExpression.setText(
                format(firstValue) + " " +
                        operator + " " +
                        format(secondValue)
        );

        firstValue = result;
        currentInput = format(result);

        animateResult();

        if (fromEquals) {
            lastOperator = operator;
        }

        operator = "";
        isNewInput = true;
        justCalculated = true;
    }

    private void applyPercent() {

        BigDecimal value = new BigDecimal(currentInput);

        if (firstValue != null && !operator.isEmpty()) {

            // iPhone style percent behavior
            value = firstValue.multiply(value)
                    .divide(BigDecimal.valueOf(100), mc);
        } else {
            value = value.divide(BigDecimal.valueOf(100), mc);
        }

        currentInput = format(value);
        updateDisplay();
    }

    private void toggleSign() {

        BigDecimal value = new BigDecimal(currentInput);
        value = value.negate();

        currentInput = format(value);
        updateDisplay();
    }

    private BigDecimal compute(BigDecimal a, BigDecimal b, String op) {

        switch (op) {

            case "+":
                return a.add(b, mc);

            case "-":
                return a.subtract(b, mc);

            case "×":
                return a.multiply(b, mc);

            case "÷":
                if (b.compareTo(BigDecimal.ZERO) == 0) return null;
                return a.divide(b, mc);
        }

        return null;
    }

    private void clearAll() {

        currentInput = "0";
        firstValue = null;
        secondValue = null;
        operator = "";
        lastOperator = "";
        lastOperand = null;

        isNewInput = true;
        justCalculated = false;

        tvExpression.setText("");
        updateDisplay();
    }

    private void updateDisplay() {
        tvDisplay.setText(currentInput);
    }

    private String format(BigDecimal value) {

        DecimalFormat df = new DecimalFormat("0.###########");
        return df.format(value);
    }

    private void animateResult() {

        tvDisplay.setAlpha(0f);
        updateDisplay();

        ObjectAnimator anim =
                ObjectAnimator.ofFloat(tvDisplay, "alpha", 0f, 1f);

        anim.setDuration(250);
        anim.start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("currentInput", currentInput);
        outState.putString("operator", operator);
        outState.putSerializable("firstValue", firstValue);
        outState.putBoolean("isNewInput", isNewInput);
        outState.putBoolean("justCalculated", justCalculated);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        currentInput = state.getString("currentInput", "0");
        operator = state.getString("operator", "");
        firstValue = (BigDecimal) state.getSerializable("firstValue");
        isNewInput = state.getBoolean("isNewInput", true);
        justCalculated = state.getBoolean("justCalculated", false);

        updateDisplay();
    }
}