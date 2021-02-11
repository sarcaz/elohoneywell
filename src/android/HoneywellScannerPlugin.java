package com.initzero.honeywell;

import android.content.Context;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import honeywell.hedc_usb_com.HEDCUsbCom;

public class HoneywellScannerPlugin extends CordovaPlugin implements HEDCUsbCom.OnConnectionStateListener,HEDCUsbCom.OnBarcodeListener,HEDCUsbCom.OnImageListener {
    private static final String TAG = "HoneywellScanner";
	private CallbackContext callbackContext;
	private HEDCUsbCom m_engine;
	private String    m_Codabar         = "";
    private byte m_presentation_mode    =  3;
    private byte m_manual_mode          =  0;
    private String[] AsciiTab = {
            "NUL", "SOH", "STX", "ETX", "EOT", "ENQ", "ACK", "BEL",	"BS",  "HT",  "LF",  "VT",
            "FF",  "CR",  "SO",  "SI",	"DLE", "DC1", "DC2", "DC3", "DC4", "NAK", "SYN", "ETB",
            "CAN", "EM",  "SUB", "ESC", "FS",  "GS",  "RS",  "US",	"SP",  "DEL",
    };
	public String ConvertToString(byte[] data, int length)
    {
        String s = "";
        String s_final = "";
        for (int i = 0; i < length; i++) {
            if ((data[i]>=0)&&(data[i] < 0x20 ))
            {
                s = String.format("<%s>",AsciiTab[data[i]]);
            }
            else if (data[i] >= 0x7F)
            {
                s = String.format("<0x%02X>",data[i]);
            }
            else {
                s = String.format("%c", data[i]&0xFF);
            }
            s_final+=s;
        }

        return s_final;
    }
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        Context context = cordova.getActivity().getApplicationContext();
        m_engine = new HEDCUsbCom(context,this,this,this);
    }
	@Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if(action.equals("listenForScans")){
			this.callbackContext=callbackContext;
            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            this.callbackContext.sendPluginResult(result);
        }
        if (action.equals("manualMode")){
            m_engine.SetTriggerMode(m_manual_mode);
        }
        if (action.equals("autoMode")){
            m_engine.SetTriggerMode(m_presentation_mode);
        }
        if (action.equals("startReading")){
            m_engine.StartReadingSession();
        }
        if (action.equals("stopReading")){
            m_engine.StopReadingSession();
        }
        return true;
    }
	@Override
	public void OnConnectionStateEvent(final HEDCUsbCom.ConnectionState state) {

        if (state == HEDCUsbCom.ConnectionState.Connected) {
            m_engine.SetTriggerMode(m_presentation_mode);
            PluginResult result = new PluginResult(PluginResult.Status.OK, "connesso");
            result.setKeepCallback(true);
            this.callbackContext.sendPluginResult(result);
        }
        else
        {
            NotifyError("Scanner unavailable");
        }
    }
	@Override
    public  void OnBarcodeData(final byte[] data, final int length) {
		if(this.callbackContext!=null)
        {
			m_Codabar = ConvertToString(data,length);
            PluginResult result = new PluginResult(PluginResult.Status.OK, m_Codabar);
            result.setKeepCallback(true);
            this.callbackContext.sendPluginResult(result);
        }

    }
    @Override
    public void OnImageData(final byte[] rawimage, final int sizeImage) {
	}
	private void NotifyError(String error){
        if(this.callbackContext!=null)
        {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, error);
            result.setKeepCallback(true);
            this.callbackContext.sendPluginResult(result);
        }
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        if (m_engine != null) {
            try {
                m_engine.SetTriggerMode(m_presentation_mode);
            } catch (Exception e) {
                e.printStackTrace();
                NotifyError("Scanner unavailable");
            }
        }
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        if (m_engine != null) {
            // release the scanner claim so we don't get any scanner
            // notifications while paused.
            m_engine.SetTriggerMode(m_manual_mode);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (m_engine != null) {
            // close BarcodeReader to clean up resources.
            m_engine.SetTriggerMode(m_manual_mode);
            m_engine.StopReadingSession();
            m_engine = null;
        }

    }


}