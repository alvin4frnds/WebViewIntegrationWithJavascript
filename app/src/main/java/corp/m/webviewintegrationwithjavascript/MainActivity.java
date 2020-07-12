package corp.m.webviewintegrationwithjavascript;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.*;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public boolean isDownloadable(String url) {
        int indexOfDot = url.substring(url.lastIndexOf("/") + 1, url.length()).lastIndexOf(".") + 1;

        return indexOfDot != 0;
    }

    public void downloadFile(String url) {
        int pos = url.lastIndexOf("/") + 1;
        String fileName = url.substring(pos, url.length());
        int indexOfDot = fileName.lastIndexOf(".") + 1;
        String extension = fileName.substring(indexOfDot,fileName.length());

        String mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);

        if( file.exists() ) {
            openFile(MainActivity.this, file);
            // Toast.makeText( MainActivity.this, "File Already Present At " + file, Toast.LENGTH_LONG ).show();
        }else {
            Toast.makeText( MainActivity.this, "Downloading ...", Toast.LENGTH_LONG ).show();

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            final String downloadFile = URLUtil.guessFileName(url, null, mimetype);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, downloadFile);

            DownloadManager dManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (dManager != null) dManager.enqueue(request);

            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    File newFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + downloadFile);
                    Log.e("downloadFile", newFile.getAbsolutePath() + " : " + newFile.getPath());
                    try {
                        newFile.getAbsolutePath();
                        newFile.getCanonicalPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(newFile.exists()) {
                        openFile(MainActivity.this, newFile);
                    }
                }
            }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
    }

    private void openFile(Context context, File file) {
        String fileName = "" + file;
        int indexOfDot = fileName.lastIndexOf(".")+1;
        fileName = fileName.substring(indexOfDot,fileName.length());
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(""+fileName);
        Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
        pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            Uri apkURI = FileProvider.getUriForFile(getApplicationContext(), context.getApplicationContext().getPackageName() + ".provider", file.getAbsoluteFile());
            pdfOpenintent.setDataAndType(apkURI, mime);
            pdfOpenintent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(pdfOpenintent);

        } catch (Exception e) {
            Toast.makeText(context,"No App Found to open this file format", Toast.LENGTH_LONG).show();
        }
    }

    public void askPermissions() {
        try {

            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            )
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                }, 0);
        } catch (Exception e) {
            Toast.makeText(this, "Something Went Wrong, Please Manually  Check The Permissions", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            //grant all permissions
            case 0:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        //    Toast.makeText(LoginActivity.this, "Permission Set", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = findViewById(R.id.WebView);
        String url = "https://devse.medimetry.in/bot?api_key=mUCFuAzCqqAxh3QrqqEwwYoF3CEIBkz9MjgtL0F2&consultationCode=e7lCusyJPo8ZgPGB##powered-by-ARISUN";
        isDownloadable(url);
        webView.loadUrl(url);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled (true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){

                // Download the file, if its possible
                if ( isDownloadable(url) ) {
                    downloadFile(url);

                    return true;
                }

                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url){
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                      String description, String failingUrl){
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message,
                                   final JsResult result){
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result){
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message,
                                    String defaultValue, final JsPromptResult result){
                return true;
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                android.util.Log.d("WebView", consoleMessage.message());
                return true;
            }
        });

        JavascriptInterface JSAndroidBindingClass = new JSTestinInterface();
        webView.addJavascriptInterface(JSAndroidBindingClass, "jsInterfaceName");

        askPermissions();
    }
}
