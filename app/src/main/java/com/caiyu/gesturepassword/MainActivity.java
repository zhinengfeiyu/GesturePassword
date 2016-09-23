package com.caiyu.gesturepassword;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView resultTv;
    private Button resetBtn;
    private GesturePasswordView gesturePasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultTv = (TextView) findViewById(R.id.resultTv);
        resetBtn = (Button) findViewById(R.id.resetBtn);
        gesturePasswordView = (GesturePasswordView) findViewById(R.id.gesture);

        gesturePasswordView.setOnCompleteListener(new GesturePasswordView.OnCompleteListener() {
            @Override
            public void onComplete(List<Integer> result) {
                StringBuilder sb = new StringBuilder();
                sb.append("密码：[ ");
                for (int i = 0; i < result.size(); i++) {
                    sb.append(result.get(i) + 1);
                    if (i != result.size() - 1)
                        sb.append(", ");
                }
                sb.append(" ]");
                resultTv.setText(sb.toString());
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultTv.setText("请输入密码");
                gesturePasswordView.reset();
            }
        });

    }
}
