#!/usr/bin/env python3
"""LAN demo receiver for the fixed, public test credentials."""
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
import argparse
import json
from datetime import datetime, timezone

DEMO_USERNAME = "Test"
DEMO_PASSWORD = "test@123"

class Handler(BaseHTTPRequestHandler):
    def _reply(self, status, payload):
        body = json.dumps(payload).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Connection", "close")
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        if self.path == "/health":
            return self._reply(200, {"status": "ok", "service": "Security Check demo"})
        return self._reply(404, {"error": "not found"})

    def do_POST(self):
        if self.path != "/login":
            return self._reply(404, {"error": "not found"})
        try:
            length = int(self.headers.get("Content-Length", "0"))
            if length < 1 or length > 4096:
                return self._reply(400, {"error": "invalid request size"})
            data = json.loads(self.rfile.read(length).decode("utf-8"))
            username = str(data.get("username", ""))
            password = str(data.get("password", ""))
            ok = (username == DEMO_USERNAME and password == DEMO_PASSWORD)
            stamp = datetime.now(timezone.utc).isoformat(timespec="seconds")

            # This intentionally prints only the published demo pair.
            print(f"[{stamp}] submitted_demo_username={username!r}", flush=True)
            print(f"[{stamp}] submitted_demo_password={password!r}", flush=True)
            print(f"[{stamp}] authentication={'SUCCESS' if ok else 'FAILED'}", flush=True)

            return self._reply(200 if ok else 401, {
                "ok": ok,
                "message": "Demo login successful" if ok else "Invalid demo username or password"
            })
        except (ValueError, UnicodeDecodeError, json.JSONDecodeError):
            return self._reply(400, {"error": "invalid JSON"})

    def log_message(self, fmt, *args):
        print("HTTP:", fmt % args, flush=True)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", type=int, default=4444)
    args = parser.parse_args()
    if not 1 <= args.port <= 65535:
        raise SystemExit("Port must be between 1 and 65535")
    print(f"Security Check demo server listening on 0.0.0.0:{args.port}", flush=True)
    print("Demo credentials are public test values only.", flush=True)
    ThreadingHTTPServer(("0.0.0.0", args.port), Handler).serve_forever()
