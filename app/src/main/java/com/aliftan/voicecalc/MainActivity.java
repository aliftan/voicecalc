package com.aliftan.voicecalc;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import org.aliftan.voicecalc.R;

import java.util.ArrayList;
import java.util.Locale;

import mehdi.sakout.fancybuttons.FancyButton;

public class MainActivity extends AppCompatActivity{
    // TextView used to display the output
    private TextView txtScreen;
    private TextView txtInput;

    // Represent whether the lastly pressed key is numeric or not
    private boolean lastNumeric;

    // Represent that current state is in error or not
    private boolean stateError;

    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the button
        FancyButton btnSpeak = findViewById(R.id.btnSpeak);

        // Find the TextView
        this.txtScreen = findViewById(R.id.txtScreen);
        this.txtInput = findViewById(R.id.txtInput);

        // Find and set OnClickListener to numeric buttons
        setNumericOnClickListener();

        // Find and set OnClickListener to operator buttons, equal button and decimal point button
        setOperatorOnClickListener();
    }

    /**
     * Find and set OnClickListener to numeric buttons.
     */
    private void setNumericOnClickListener() {
        // Create a common OnClickListener
        new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Just append/set the text of clicked button
                Button button = (Button) v;
                if (stateError) {
                    // If current state is Error, replace the error message
                    txtScreen.setText(button.getText());
                    stateError = false;

                } else {
                    // If not, already there is a valid expression so append to it
                    txtScreen.append(button.getText());
                }

                // Set the flag
                lastNumeric = true;
            }
        };
    }

    /**
     * Find and set OnClickListener to operator buttons, equal button and decimal point button.
     */
    private void setOperatorOnClickListener() {
        // Create a common OnClickListener for operators

        new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the current state is Error do not append the operator
                // If the last input is number only, append the operator
                if (lastNumeric && !stateError) {
                    Button button = (Button) v;
                    txtScreen.append(button.getText());
                    lastNumeric = false;
                }
            }
        };

        // Clear button
        findViewById(R.id.btnClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtScreen.setText("");  // Clear the screen
                txtInput.setText("");  // Clear the input
                // Reset all the states and flags
                lastNumeric = false;
                stateError = false;
            }
        });


        findViewById(R.id.btnSpeak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stateError) {
                    // If current state is Error, replace the error message
                    txtScreen.setText("Try Again");
                    stateError = false;
                } else {
                    // If not, already there is a valid expression so append to it
                    promptSpeechInput();
                }
                // Set the flag
                lastNumeric = true;

            }
        });
    }

    /**
     * Logic to calculate the solution.
     */
    private void onEqual() {
        // If the current state is error, nothing to do.
        // If the last input is a number only, solution can be found.
        if (lastNumeric && !stateError) {
            // Read the expression
            String inputNumber = txtInput.getText().toString();

            // Create an Expression (A class from exp4j library)
                try {
            Expression expression = null;
            try {
                expression = new ExpressionBuilder(inputNumber).build();
                double result = expression.evaluate();
                txtScreen.setText(Double.toString(result));
            } catch (Exception e){
                txtScreen.setText("Error, Coba lagi!");
            }

                } catch (ArithmeticException ex) {
                // Display an error message
                txtScreen.setText("Error, Coba lagi!");
                stateError = true;
                lastNumeric = false;
            }
        }
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String change=result.get(0);

                    // english-lang
                    change=change.replace("x","*");
                    change=change.replace("X","*");
                    change=change.replace("add","+");
                    change=change.replace("sub","-");
                    change=change.replace("to","2");
                    change=change.replace(" plus ","+");
                    change=change.replace(" minus ","-");
                    change=change.replace(" times ","*");
                    change=change.replace(" into ","*");
                    change=change.replace(" in2 ","*");
                    change=change.replace(" multiply by ","*");
                    change=change.replace(" divide by ","/");
                    change=change.replace("divide","/");
                    change=change.replace("equal","=");
                    change=change.replace("equals","=");

                    if(change.contains("=")){
                        change=change.replace("=","");
                        txtInput.setText(change);
                        onEqual();
                    }else{
                        txtInput.setText(change);
                    }
                }

                break;
            }

        }
    }
}
