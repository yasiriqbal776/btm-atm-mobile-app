package com.ideofuzion.btm.main.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.ideofuzion.btm.BTMApplication;
import com.ideofuzion.btm.R;
import com.ideofuzion.btm.main.settings.profitwalletsetup.ExistingProfitWalletActivity;
import com.ideofuzion.btm.model.BTMUser;
import com.ideofuzion.btm.model.ServerMessage;
import com.ideofuzion.btm.network.VolleyRequestHelper;
import com.ideofuzion.btm.utils.AlertMessage;
import com.ideofuzion.btm.utils.Constants;
import com.ideofuzion.btm.utils.DialogHelper;
import com.ideofuzion.btm.utils.Fonts;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.ideofuzion.btm.main.settings.PinCodeActivity.EXTRA_FROM_REGISTRATION;

/**
 * Created by ideofuzion on 9/23/2017.
 *
 * this activity is used to setup application thresshold value
 */

public class SetupProfitThresholdActivity extends Activity implements Response.Listener<JSONObject>, Response.ErrorListener, Constants.ResultCode {
    TextView text_minMaxBalance_header;
    EditText merchantProfitThreshold;
    Button submit;
    private Typeface fontBold;
    private Typeface fontRegular;
    private Typeface fontSemiBold;
    private DialogHelper dialogHelper;
    private boolean isFromRegistration = false;
    private Button cancel;


    /**
     * this function will be called each time the activity starts
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_setup_profit_threshold);
            initResources();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        }catch (Exception e)
        {}
    }

    /**
     * getting data from intent, initiating other
     * classes objects including fonts and applying
     * fonts to ui resources and applying click
     * listeners to ui resources
     */
    public void initResources() {

        isFromRegistration = getIntent().getBooleanExtra(EXTRA_FROM_REGISTRATION, false);

        dialogHelper = new DialogHelper(this);
        //initializing TypeFaces objects
        fontRegular = Fonts.getInstance(this).getTypefaceRegular();
        fontSemiBold = Fonts.getInstance(this).getTypefaceSemiBold();
        fontBold = Fonts.getInstance(this).getTypefaceBold();

        text_minMaxBalance_header = (TextView) findViewById(R.id.text_minMaxBalance_header);
        merchantProfitThreshold = (EditText) findViewById(R.id.edit_minMaxBalance_minBalance);
        submit = (Button) findViewById(R.id.button_minMaxBalance_submit);

        text_minMaxBalance_header.setTypeface(fontBold);
        merchantProfitThreshold.setTypeface(fontSemiBold);
        submit.setTypeface(fontBold);
        cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    finish();
            }
        });
        if (isFromRegistration) {
            cancel.setVisibility(View.GONE);
        } else {

            cancel.setVisibility(View.VISIBLE);
        }
        cancel.setTypeface(fontBold);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    sendRequestToServer();
                }
            }
        });

        //init data
        merchantProfitThreshold.setText(BTMApplication.getInstance().getBTMUserObj().getMerchantProfitThreshold());

    }

    /**
     * sending profit threshold update request to server
     */
    private void sendRequestToServer() {
        String url = Constants.BASE_SERVER_URL + Constants.ROUTE_UPDATE_MERCHANT_PROFIT_THRESHOLD;

        Map<String, String> updateTaglineParams = new HashMap<>();
        updateTaglineParams.put("merchantId", BTMApplication.getInstance().getBTMUserObj().getUserId());
        updateTaglineParams.put("merchantProfitThreshold", merchantProfitThreshold.getText().toString());

        VolleyRequestHelper.sendPostRequestWithParam(url, updateTaglineParams, this);
        dialogHelper.showProgressDialog();

    }


    /**
     * validating form edit text fields
     * @return
     */
    boolean validateFields() {
        if (merchantProfitThreshold.getText().toString().isEmpty()) {
            AlertMessage.showError(merchantProfitThreshold, "Please enter merchant margin profit margin threshold");
            merchantProfitThreshold.requestFocus();
            return false;
        }

        return true;
    }


    /**
     * this function will be called when the server throws an
     * error when failed to connect to server
     * @param error
     */
    @Override
    public void onErrorResponse(VolleyError error) {
        if (dialogHelper != null) {
            dialogHelper.hideProgressDialog();
        }
        AlertMessage.showError(merchantProfitThreshold, Constants.ERROR_CHECK_INTERNET);

    }

    /**
     * this functoin will be called on success execution of request on server
     * @param response
     */
    @Override
    public void onResponse(JSONObject response) {
        if (dialogHelper != null) {
            dialogHelper.hideProgressDialog();
        }
        if (response != null) {
            ServerMessage serverMessageResponse = new ServerMessage();
            try {
                serverMessageResponse.setData(response.getString("data"));
                serverMessageResponse.setCode(response.getInt("code"));
                serverMessageResponse.setMessage(response.getString("message"));
                if (serverMessageResponse.getCode() == CODE_SUCCESS) {
                    AlertMessage.show(merchantProfitThreshold, "Success");

                    if (!serverMessageResponse.getData().isEmpty()) {
                        Gson gsonForUser = new Gson();
                        BTMUser btmUser = gsonForUser.fromJson(serverMessageResponse.getData(), BTMUser.class);
                        BTMApplication.getInstance().setBTMUserObj(btmUser);
                        if (isFromRegistration) {
                            if(btmUser.getExchangeStatus()) {
                                startActivity(new Intent(SetupProfitThresholdActivity.this, UpdateHotWalletBeneficiaryActivity.class)
                                        .putExtra(EXTRA_FROM_REGISTRATION, true));
                            }else{
                                startActivity(new Intent(SetupProfitThresholdActivity.this, ExistingProfitWalletActivity.class)
                                        .putExtra(EXTRA_FROM_REGISTRATION, true));
                            }
                        } else {
                            finish();
                        }
                    }
                } else {
                    AlertMessage.showError(merchantProfitThreshold, serverMessageResponse.getMessage());
                }//end oe else
            } catch (Exception e) {
                e.printStackTrace();
            }

        }//end of if for response not null

    }

}
