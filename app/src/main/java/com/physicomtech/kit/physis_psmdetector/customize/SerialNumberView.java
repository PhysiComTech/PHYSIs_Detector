package com.physicomtech.kit.physis_psmdetector.customize;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.physicomtech.kit.physis_psmdetector.R;

public class SerialNumberView extends RelativeLayout {

    public interface OnSetSerialNumberListener {
        void onSetSerialNumber(String serialNum);
    }

    private OnSetSerialNumberListener onSetSerialNumberListener = null;

    public void setOnSetSerialNumberListener(OnSetSerialNumberListener listener){
        onSetSerialNumberListener = listener;
    }

    ImageView btnDrop;
    Button btnSetup;
    EditText etSerialNumber;
    TextView tvSerailNumber;

    private Context context;

    private boolean isEditMode = false;
    private String serialNumber;

    public SerialNumberView(@NonNull Context context) {
        super(context);
    }

    public SerialNumberView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView(context);
    }

    public SerialNumberView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initView(final Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View view = layoutInflater.inflate(R.layout.view_serial_number, this, false);
        addView(view);

        btnDrop = view.findViewById(R.id.btn_drop_serial_number);
        btnSetup = view.findViewById(R.id.btn_set_serial_number);
        etSerialNumber = view.findViewById(R.id.et_serial_number);
        tvSerailNumber = view.findViewById(R.id.tv_serial_number);

        btnDrop.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if(serialNumber == null) {
                    Toast.makeText(context, "PHYSIs KiT의 시리얼 번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                showEditView(!isEditMode);
            }
        });

        btnSetup.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if(onSetSerialNumberListener == null)
                    return;

                if(etSerialNumber.getText().length() != 12) {
                    Toast.makeText(context, "PHYSIs KiT의 시리얼 번호를 확인하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                onSetSerialNumberListener.onSetSerialNumber(etSerialNumber.getText().toString());
                setSerialNumber(etSerialNumber.getText().toString());
                showEditView(false);
            }
        });
    }


    public void showEditView(boolean enable){
        if(isEditMode = enable) {
            btnDrop.setImageResource(R.drawable.ic_drop_up);
            btnSetup.setVisibility(View.VISIBLE);
            etSerialNumber.setVisibility(View.VISIBLE);
            tvSerailNumber.setVisibility(View.GONE);
            etSerialNumber.setText(serialNumber);
        } else {
            btnDrop.setImageResource(R.drawable.ic_drop_down);
            btnSetup.setVisibility(View.GONE);
            etSerialNumber.setVisibility(View.GONE);
            tvSerailNumber.setVisibility(View.VISIBLE);
            hideKeyboard();
        }
    }

    public void setSerialNumber(String serialNum){
        serialNumber = serialNum;
        tvSerailNumber.setText(serialNum);
    }

//    public String getInputSerialNumber(){
//        return etSerialNumber.getText().toString();
//    }

    public String getSerialNumber(){
        return etSerialNumber.getText().toString();
    }

    private void hideKeyboard() {
        if (etSerialNumber.isFocusable()) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etSerialNumber.getWindowToken(), 0);
        }
    }

}
