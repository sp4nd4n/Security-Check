package com.example.securitycheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final String PREFS = "security_check";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private EditText usernameInput, passwordInput;
    private TextView status;
    private String serverIp = "";
    private int serverPort = 4444;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        android.content.SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        serverIp = p.getString("server_ip", "");
        serverPort = p.getInt("server_port", 4444);
        buildScreen();
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(18), dp(24), dp(24));
        root.setBackgroundColor(0xFFF4F7FB);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = new TextView(this);
        title.setText("Security Check");
        title.setTextSize(24);
        title.setTextColor(0xFF14213D);
        title.setTypeface(null, 1);
        top.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        Button menu = new Button(this);
        menu.setText("⋮");
        menu.setTextSize(24);
        menu.setMinWidth(dp(48));
        menu.setOnClickListener(v -> showMenu(v));
        top.addView(menu);
        root.addView(top);

        TextView subtitle = new TextView(this);
        subtitle.setText("Preset demo login");
        subtitle.setTextColor(0xFF65758B);
        subtitle.setPadding(0, dp(6), 0, dp(24));
        root.addView(subtitle);

        usernameInput = field("Username");
        usernameInput.setText("Test");
        passwordInput = field("Password");
        passwordInput.setText("test@123");
        root.addView(usernameInput);
        addSpace(root, 12);
        root.addView(passwordInput);
        addSpace(root, 18);

        Button login = new Button(this);
        login.setText("Login");
        root.addView(login);
        login.setOnClickListener(v -> submitLogin());

        status = new TextView(this);
        status.setText("Server: " + (serverIp.isEmpty() ? "Not configured" : serverIp + ":" + serverPort));
        status.setTextColor(0xFF65758B);
        status.setPadding(0, dp(22), 0, 0);
        root.addView(status);
        setContentView(root);
    }

    private EditText field(String hint) {
        EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setHint(hint);
        e.setTextSize(16);
        return e;
    }

    private void showMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("Preferences");
        popup.setOnMenuItemClickListener(item -> {
            showPreferences();
            return true;
        });
        popup.show();
    }

    private void showPreferences() {
        LinearLayout box = new LinearLayout(this);
        box.setPadding(dp(20), dp(8), dp(20), 0);
        box.setOrientation(LinearLayout.VERTICAL);

        EditText ip = field("e.g. 192.168.43.100");
        ip.setInputType( android.text.InputType.TYPE_CLASS_TEXT );
        ip.setText(serverIp);

        EditText port = field("e.g. 4444");
        port.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        port.setText(serverPort == 0 ? "" : String.valueOf(serverPort));

        box.addView(ip);
        box.addView(port);

        new AlertDialog.Builder(this)
            .setTitle("Preferences")
            .setView(box)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", (d, w) -> {
                String newIp = ip.getText().toString().trim();
                String portText = port.getText().toString().trim();
                try {
                    int newPort = Integer.parseInt(portText);
                    if (newIp.isEmpty() || newIp.contains(" ") || newPort < 1 || newPort > 65535) {
                        throw new IllegalArgumentException();
                    }
                    serverIp = newIp;
                    serverPort = newPort;
                    getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                        .putString("server_ip", serverIp)
                        .putInt("server_port", serverPort)
                        .apply();
                    status.setText("Server: " + serverIp + ":" + serverPort);
                    Toast.makeText(this, "Preferences saved", Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    Toast.makeText(this, "Enter a valid IP and port (1–65535)", Toast.LENGTH_LONG).show();
                }
            }).show();
    }

    private void submitLogin() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        if (serverIp.isEmpty() || serverPort < 1) {
            Toast.makeText(this, "Set Server IP and port in Preferences first", Toast.LENGTH_LONG).show();
            return;
        }
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter the displayed demo credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        final String ip = serverIp;
        final int port = serverPort;
        status.setText("Connecting...");
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://" + ip + ":" + port + "/login");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                JSONObject body = new JSONObject();
                body.put("username", username);
                body.put("password", password);
                try (OutputStream out = conn.getOutputStream()) {
                    out.write(body.toString().getBytes("UTF-8"));
                }
                int code = conn.getResponseCode();
                final boolean success = code == 200;
                runOnUiThread(() -> {
                    String message = success ? "Login successful" : "Login failed";
                    status.setText(message);
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    status.setText("Cannot connect. Check IP, port and network.");
                    Toast.makeText(this, "Cannot reach Kali server", Toast.LENGTH_LONG).show();
                });
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private void addSpace(LinearLayout root, int height) {
        View v = new View(this);
        root.addView(v, new LinearLayout.LayoutParams(1, dp(height)));
    }
    private int dp(int value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
