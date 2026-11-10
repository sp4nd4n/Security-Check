package com.example.securitycheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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
    private static final int BG = Color.rgb(8, 16, 32);
    private static final int PANEL = Color.rgb(17, 29, 50);
    private static final int CYAN = Color.rgb(45, 203, 232);
    private static final int WHITE = Color.rgb(239, 246, 255);
    private static final int MUTED = Color.rgb(145, 164, 190);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private EditText usernameInput, passwordInput;
    private TextView status;
    private String serverIp = "";
    private int serverPort = 0;
    private Button loginButton;

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);

        android.content.SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        serverIp = p.getString("server_ip", "");
        serverPort = p.getInt("server_port", 4444);
        buildScreen();
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(26), dp(20), dp(26), dp(28));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -1));

        LinearLayout topBar = new LinearLayout(this);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout brand = new LinearLayout(this);
        brand.setGravity(Gravity.CENTER_VERTICAL);
        brand.setOrientation(LinearLayout.HORIZONTAL);

        ShieldView smallShield = new ShieldView(this);
        brand.addView(smallShield, new LinearLayout.LayoutParams(dp(34), dp(40)));

        TextView brandName = new TextView(this);
        brandName.setText("SECURITY CHECK");
        brandName.setTextColor(WHITE);
        brandName.setTextSize(13);
        brandName.setLetterSpacing(0.12f);
        brandName.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams brandTextParams =
                new LinearLayout.LayoutParams(-2, -2);
        brandTextParams.leftMargin = dp(10);
        brand.addView(brandName, brandTextParams);

        topBar.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));

        TextView menu = new TextView(this);
        menu.setText("⋮");
        menu.setTextSize(28);
        menu.setTextColor(MUTED);
        menu.setGravity(Gravity.CENTER);
        menu.setBackground(roundRect(PANEL, 12));
        topBar.addView(menu, new LinearLayout.LayoutParams(dp(42), dp(42)));
        menu.setOnClickListener(v -> showMenu(v));
        root.addView(topBar);

        View topSpace = new View(this);
        root.addView(topSpace, new LinearLayout.LayoutParams(1, dp(54)));

        ShieldView heroShield = new ShieldView(this);
        LinearLayout.LayoutParams heroParams =
                new LinearLayout.LayoutParams(dp(88), dp(100));
        heroParams.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(heroShield, heroParams);

        TextView heading = new TextView(this);
        heading.setText("Welcome back");
        heading.setTextColor(WHITE);
        heading.setTextSize(28);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        heading.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams headingParams =
                new LinearLayout.LayoutParams(-1, -2);
        headingParams.topMargin = dp(22);
        root.addView(heading, headingParams);

        TextView subtitle = new TextView(this);
        subtitle.setText("Sign in to continue to your security dashboard");
        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(8), 0, dp(30));
        root.addView(subtitle);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(22), dp(20), dp(22));
        card.setBackground(roundRect(PANEL, 22));
        root.addView(card, new LinearLayout.LayoutParams(-1, -2));

        addFieldLabel(card, "USERNAME");
        usernameInput = field("Enter username", false);
        card.addView(usernameInput, fieldParams());
        addGap(card, 20);

        addFieldLabel(card, "PASSWORD");
        passwordInput = field("Enter password", true);
        card.addView(passwordInput, fieldParams());
        addGap(card, 26);

        loginButton = new Button(this);
        loginButton.setText("SIGN IN");
        loginButton.setTextSize(14);
        loginButton.setLetterSpacing(0.08f);
        loginButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        loginButton.setTextColor(Color.rgb(5, 22, 38));
        loginButton.setAllCaps(false);
        loginButton.setBackground(roundRect(CYAN, 14));
        loginButton.setElevation(dp(3));
        loginButton.setPadding(0, dp(5), 0, dp(5));
        card.addView(loginButton, new LinearLayout.LayoutParams(-1, dp(54)));
        loginButton.setOnClickListener(v -> submitLogin());

        status = new TextView(this);
        status.setText("SERVER  •  " + serverDescription());
        status.setTextColor(MUTED);
        status.setTextSize(12);
        status.setGravity(Gravity.CENTER);
        status.setPadding(dp(10), dp(18), dp(10), 0);
        root.addView(status);

        TextView footer = new TextView(this);
        footer.setText("SECURE ACCESS  •  SECURITY CHECK");
        footer.setTextColor(Color.rgb(83, 105, 133));
        footer.setTextSize(10);
        footer.setLetterSpacing(0.12f);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footerParams =
                new LinearLayout.LayoutParams(-1, -2);
        footerParams.topMargin = dp(28);
        root.addView(footer, footerParams);

        setContentView(scroll);
    }

    private void addFieldLabel(LinearLayout parent, String label) {
        TextView text = new TextView(this);
        text.setText(label);
        text.setTextColor(MUTED);
        text.setTextSize(11);
        text.setLetterSpacing(0.12f);
        text.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        parent.addView(text, new LinearLayout.LayoutParams(-1, -2));
    }

    private EditText field(String hint, boolean password) {
        EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setHint(hint);
        e.setHintTextColor(Color.rgb(105, 126, 153));
        e.setTextColor(WHITE);
        e.setTextSize(15);
        e.setPadding(dp(15), 0, dp(15), 0);
        e.setBackground(roundRect(Color.rgb(10, 21, 39), 12));
        if (password) {
            e.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            e.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        }
        return e;
    }

    private LinearLayout.LayoutParams fieldParams() {
        return new LinearLayout.LayoutParams(-1, dp(54));
    }

    private GradientDrawable roundRect(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private void addGap(LinearLayout parent, int height) {
        View gap = new View(this);
        parent.addView(gap, new LinearLayout.LayoutParams(1, dp(height)));
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

        EditText ip = new EditText(this);
        ip.setSingleLine(true);
        ip.setHint("Server IP address");
        ip.setInputType(InputType.TYPE_CLASS_TEXT);
        ip.setText(serverIp);

        EditText port = new EditText(this);
        port.setSingleLine(true);
        port.setHint("Enter server port");
        port.setInputType(InputType.TYPE_CLASS_NUMBER);
        port.setText("");

        box.addView(ip);
        box.addView(port);

        new AlertDialog.Builder(this)
                .setTitle("Server Preferences")
                .setView(box)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (d, w) -> {
                    String newIp = ip.getText().toString().trim();
                    String portText = port.getText().toString().trim();
                    try {
                        int newPort = Integer.parseInt(portText);
                        if (newIp.isEmpty() || newIp.contains(" ") ||
                                newPort < 1 || newPort > 65535) {
                            throw new IllegalArgumentException();
                        }
                        serverIp = newIp;
                        serverPort = newPort;
                        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                                .putString("server_ip", serverIp)
                                .putInt("server_port", serverPort)
                                .apply();
                        status.setText("SERVER  •  " + serverDescription());
                        Toast.makeText(this, "Preferences saved",
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        Toast.makeText(this,
                                "Enter a valid IP and port (1–65535)",
                                Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    private String serverDescription() {
        return serverIp.isEmpty() ? "Not configured" : serverIp + ":" + serverPort;
    }

    private void submitLogin() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (username.isEmpty()) {
            usernameInput.setError("Enter your username");
            usernameInput.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Enter your password");
            passwordInput.requestFocus();
            return;
        }
        if (serverIp.isEmpty() || serverPort < 1) {
            Toast.makeText(this,
                    "Set Server IP and port in Preferences first",
                    Toast.LENGTH_LONG).show();
            return;
        }

        final String ip = serverIp;
        final int port = serverPort;
        loginButton.setEnabled(false);
        loginButton.setText("CONNECTING...");
        status.setText("SERVER  •  Connecting securely...");

        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://" + ip + ":" + port + "/login");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type",
                        "application/json; charset=utf-8");

                JSONObject body = new JSONObject();
                body.put("username", username);
                body.put("password", password);
                try (OutputStream out = conn.getOutputStream()) {
                    out.write(body.toString().getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();
                final boolean success = responseCode == 200;
                runOnUiThread(() -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("SIGN IN");

                    if (success) {
                        status.setText("SERVER  •  Authentication accepted");
                        showDemoAssessment();
                    } else {
                        status.setText("SERVER  •  Login failed");
                        Toast.makeText(this, "Login failed", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("SIGN IN");
                    status.setText("SERVER  •  Connection unavailable");
                    Toast.makeText(this, "Cannot reach Kali server",
                            Toast.LENGTH_LONG).show();
                });
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    /*
     * This is an explicitly labelled DEMO assessment sequence.
     * It does not secretly scan the device or claim to perform a real security audit.
     */
    private void showDemoAssessment() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(24), dp(22), dp(28));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -1));

        TextView brand = new TextView(this);
        brand.setText("SECURITY CHECK");
        brand.setTextColor(CYAN);
        brand.setTextSize(12);
        brand.setLetterSpacing(0.14f);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(brand);

        TextView heading = new TextView(this);
        heading.setText("Security assessment");
        heading.setTextColor(WHITE);
        heading.setTextSize(27);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams headingParams =
                new LinearLayout.LayoutParams(-1, -2);
        headingParams.topMargin = dp(22);
        root.addView(heading, headingParams);

        TextView subtitle = new TextView(this);
        subtitle.setText("Your sign-in was accepted. The following is a simulated demo sequence.");
        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(14);
        subtitle.setPadding(0, dp(8), 0, dp(20));
        root.addView(subtitle);

        LinearLayout notice = new LinearLayout(this);
        notice.setOrientation(LinearLayout.VERTICAL);
        notice.setPadding(dp(15), dp(13), dp(15), dp(13));
        notice.setBackground(roundRect(Color.rgb(35, 48, 67), 13));
        TextView noticeTitle = new TextView(this);
        noticeTitle.setText("DEMO MODE  •  SIMULATED CHECKS");
        noticeTitle.setTextColor(CYAN);
        noticeTitle.setTextSize(11);
        noticeTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        notice.addView(noticeTitle);
        TextView noticeBody = new TextView(this);
        noticeBody.setText("This animation is illustrative. It does not perform a full device vulnerability scan.");
        noticeBody.setTextColor(WHITE);
        noticeBody.setTextSize(12);
        noticeBody.setPadding(0, dp(6), 0, 0);
        notice.addView(noticeBody);
        LinearLayout.LayoutParams noticeParams =
                new LinearLayout.LayoutParams(-1, -2);
        noticeParams.bottomMargin = dp(20);
        root.addView(notice, noticeParams);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(17), dp(18), dp(17), dp(18));
        panel.setBackground(roundRect(PANEL, 18));
        root.addView(panel, new LinearLayout.LayoutParams(-1, -2));

        TextView scanTitle = new TextView(this);
        scanTitle.setText("ASSESSMENT PROGRESS");
        scanTitle.setTextColor(WHITE);
        scanTitle.setTextSize(12);
        scanTitle.setLetterSpacing(0.1f);
        scanTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        panel.addView(scanTitle);

        ProgressBar progress = new ProgressBar(this, null,
                android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgress(0);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(CYAN));
        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(-1, dp(7));
        progressParams.topMargin = dp(17);
        progressParams.bottomMargin = dp(17);
        panel.addView(progress, progressParams);

        TextView step1 = assessmentStep("1  •  Backend connection", "Verified");
        TextView step2 = assessmentStep("2  •  Authentication", "Accepted");
        TextView step3 = assessmentStep("3  •  Network check", "Demo check pending");
        TextView step4 = assessmentStep("4  •  Security summary", "Pending");
        panel.addView(step1);
        addGap(panel, 13);
        panel.addView(step2);
        addGap(panel, 13);
        panel.addView(step3);
        addGap(panel, 13);
        panel.addView(step4);

        TextView result = new TextView(this);
        result.setText("Preparing demo assessment...");
        result.setTextColor(CYAN);
        result.setTextSize(13);
        result.setPadding(0, dp(18), 0, 0);
        panel.addView(result);

        Button back = new Button(this);
        back.setText("BACK TO SIGN IN");
        back.setAllCaps(false);
        back.setTextColor(Color.rgb(5, 22, 38));
        back.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        back.setBackground(roundRect(CYAN, 14));
        LinearLayout.LayoutParams backParams =
                new LinearLayout.LayoutParams(-1, dp(52));
        backParams.topMargin = dp(22);
        root.addView(back, backParams);
        back.setOnClickListener(v -> buildScreen());

        setContentView(scroll);

        Handler handler = new Handler(Looper.getMainLooper());
        String[] messages = {
                "Starting illustrative checks...",
                "Backend connection: verified",
                "Authentication: accepted",
                "Running simulated network check...",
                "Demo assessment complete"
        };
        int[] values = {5, 25, 50, 75, 100};
        handler.postDelayed(() -> {
            progress.setProgress(values[0]);
            result.setText(messages[0]);
        }, 250);
        handler.postDelayed(() -> {
            progress.setProgress(values[1]);
            result.setText(messages[1]);
            step3.setText("3  •  Network check     In demo");
        }, 850);
        handler.postDelayed(() -> {
            progress.setProgress(values[2]);
            result.setText(messages[2]);
        }, 1450);
        handler.postDelayed(() -> {
            progress.setProgress(values[3]);
            result.setText(messages[3]);
            step4.setText("4  •  Security summary     Preparing");
        }, 2050);
        handler.postDelayed(() -> {
            progress.setProgress(values[4]);
            result.setText(messages[4] + "\nNo real device scan was performed.");
            step3.setText("3  •  Network check     Demo completed");
            step4.setText("4  •  Security summary     Demo report ready");
        }, 2750);
    }

    private TextView assessmentStep(String title, String state) {
        TextView item = new TextView(this);
        item.setText(title + "     " + state);
        item.setTextColor(WHITE);
        item.setTextSize(13);
        return item;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private class ShieldView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        ShieldView(Activity context) {
            super(context);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            float cx = w / 2f;
            float top = h * 0.07f;

            paint.setColor(Color.rgb(16, 72, 103));
            paint.setStyle(Paint.Style.FILL);
            RectF glow = new RectF(w * 0.08f, h * 0.04f,
                    w * 0.92f, h * 0.96f);
            canvas.drawRoundRect(glow, dp(18), dp(18), paint);

            Path shield = new Path();
            shield.moveTo(cx, top);
            shield.cubicTo(w * 0.70f, h * 0.17f, w * 0.82f, h * 0.18f,
                    w * 0.86f, h * 0.22f);
            shield.lineTo(w * 0.82f, h * 0.61f);
            shield.cubicTo(w * 0.79f, h * 0.78f, w * 0.62f, h * 0.89f,
                    cx, h * 0.94f);
            shield.cubicTo(w * 0.38f, h * 0.89f, w * 0.21f, h * 0.78f,
                    w * 0.18f, h * 0.61f);
            shield.lineTo(w * 0.14f, h * 0.22f);
            shield.cubicTo(w * 0.18f, h * 0.18f, w * 0.30f, h * 0.17f,
                    cx, top);
            shield.close();

            paint.setShader(new android.graphics.LinearGradient(
                    0, top, w, h,
                    Color.rgb(45, 203, 232),
                    Color.rgb(24, 119, 190),
                    android.graphics.Shader.TileMode.CLAMP));
            canvas.drawPath(shield, paint);
            paint.setShader(null);

            paint.setColor(Color.rgb(8, 30, 52));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(dp(2), w * 0.045f));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);

            Path check = new Path();
            check.moveTo(w * 0.34f, h * 0.51f);
            check.lineTo(w * 0.46f, h * 0.63f);
            check.lineTo(w * 0.68f, h * 0.39f);
            canvas.drawPath(check, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
