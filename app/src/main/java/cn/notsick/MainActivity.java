package cn.notsick;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextMasterInfo, editTextKey;
    private ImageButton ibtnVisible;
    private boolean flag = false;  // 密码是否可见，true-可见，false-不可见
    private RadioGroup radioGroupFigure, radioGroupType;
    private TextView textViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        editTextMasterInfo = findViewById(R.id.masterInfo);
        editTextMasterInfo.setTransformationMethod(PasswordTransformationMethod.getInstance());
        editTextMasterInfo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    editTextMasterInfo.clearFocus();
                    InputMethodManager imm =
                            (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editTextMasterInfo.getWindowToken(), 1);
                }
                return false;
            }
        });
        ibtnVisible = findViewById(R.id.visible);
        ibtnVisible.setOnClickListener(this);
        radioGroupFigure = findViewById(R.id.figure);
        radioGroupType = findViewById(R.id.type);
        editTextKey = findViewById(R.id.key);
        editTextKey.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    editTextKey.clearFocus();
                }
                return false;
            }
        });
        textViewResult = findViewById(R.id.result);
        Button btnCompute = findViewById(R.id.compute);
        btnCompute.setOnClickListener(this);
        Button btnCopy = findViewById(R.id.copy);
        btnCopy.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.visible:
                if (flag) {
                    editTextMasterInfo.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    flag = false;
                    ibtnVisible.setBackgroundResource(R.drawable.invisible);
                } else {
                    editTextMasterInfo.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    flag = true;
                    ibtnVisible.setBackgroundResource(R.drawable.visible);
                }
                break;
            case R.id.compute:
                String masterInfo = editTextMasterInfo.getText().toString().trim();
                String key = editTextKey.getText().toString().trim();
                RadioButton radioButtonFigure = findViewById(radioGroupFigure.getCheckedRadioButtonId());
                int figure = Integer.parseInt(radioButtonFigure.getText().toString());
                RadioButton radioButtonType = findViewById(radioGroupType.getCheckedRadioButtonId());
                String content = radioButtonType.getText().toString();
                System.out.println(masterInfo + "  " + key + "  " + figure + "  " + content);
                textViewResult.setText(AlgorithmUtil.compute(masterInfo, key, figure, content));
                break;
            case R.id.copy:
                String result = textViewResult.getText().toString().trim();
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Label", result);;
                clipboardManager.setPrimaryClip(clipData);
                Toast toast = Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                break;
        }
    }
}
