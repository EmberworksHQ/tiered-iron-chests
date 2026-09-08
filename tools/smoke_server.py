#!/usr/bin/env python3
"""Headless dedicated-server smoke test driver (no third-party modules, Windows-friendly).

Starts a server command (a Gradle `runServer` task or a production `java -jar ... nogui`), mirrors its console to a
log file, waits for the "Done (x.xxxs)! For help, type "help"" banner, sends console commands through stdin, then
`stop`s the server and waits for it to exit. If the server never comes up or never stops, the whole process tree is
killed so the test cannot hang a CI job.

    python tools/smoke_server.py --log run-logs/1.21.1/fabric-runServer.log --cmd "datapack list" -- ^
        cmd /c H:\\AutoMods\\mods\\tiered-iron-chests\\gradlew.bat :fabric:runServer --console=plain

    python tools/smoke_server.py --cwd run-prod/fabric-1.21.1 --log run-logs/1.21.1/fabric-production.log ^
        --cmd "datapack list" --cmd "summon minecraft:cat" -- java -Dmixin.debug.export=true -jar fabric-server-launch.jar nogui

Exit code 0 = Done banner seen, all commands sent and the server exited (via `stop` or `--kill`); 1 otherwise.
Pass the server command as separate arguments (a single quoted string makes cmd ignore it and go interactive), give
gradlew.bat an absolute path (NoDefaultCurrentDirectoryInExePath) and, from Git Bash, write `cmd //c` (MSYS path
conversion turns a bare `/c` into `C:/`).
Note: Gradle only forwards stdin to run tasks that set `standardInput = System.in` (Loom does; the NeoForge module
does it in neoforge/build.gradle). Use `--kill` when stdin is not forwarded.
"""
import argparse
import os
import subprocess
import sys
import threading
import time

DONE_MARKER = "Done ("


def kill_tree(proc):
    if proc.poll() is not None:
        return
    if os.name == "nt":
        subprocess.run(["taskkill", "/F", "/T", "/PID", str(proc.pid)], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    else:
        proc.kill()


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--cwd", default=".", help="working directory for the server command")
    parser.add_argument("--log", required=True, help="file that receives the full console output")
    parser.add_argument("--cmd", action="append", default=[], help="console command to send once the server is up (repeatable)")
    parser.add_argument("--cmd-gap", type=float, default=3.0, help="seconds between console commands")
    parser.add_argument("--settle", type=float, default=8.0, help="seconds to wait after the last command before `stop`")
    parser.add_argument("--startup-timeout", type=float, default=900.0, help="seconds to wait for the Done banner")
    parser.add_argument("--stop-timeout", type=float, default=180.0, help="seconds to wait for the process to exit after `stop`")
    parser.add_argument("--kill", action="store_true", help="kill the process tree instead of sending `stop`")
    parser.add_argument("argv", nargs=argparse.REMAINDER, help="-- <server command line>")
    args = parser.parse_args()

    argv = args.argv[1:] if args.argv and args.argv[0] == "--" else args.argv
    if not argv:
        parser.error("missing server command line after --")
    os.makedirs(os.path.dirname(os.path.abspath(args.log)), exist_ok=True)

    proc = subprocess.Popen(argv, cwd=args.cwd, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    done = threading.Event()

    def pump():
        with open(args.log, "w", encoding="utf-8", errors="replace", newline="\n") as log:
            for raw in iter(proc.stdout.readline, b""):
                line = raw.decode("utf-8", "replace").rstrip("\r\n")
                log.write(line + "\n")
                log.flush()
                if DONE_MARKER in line and "For help, type" in line:
                    done.set()

    pump_thread = threading.Thread(target=pump, daemon=True)
    pump_thread.start()

    def send(command):
        try:
            proc.stdin.write((command + "\n").encode("utf-8"))
            proc.stdin.flush()
            print(f"[smoke] > {command}", flush=True)
        except OSError as e:
            print(f"[smoke] could not send {command!r}: {e}", flush=True)

    deadline = time.time() + args.startup_timeout
    while not done.is_set() and proc.poll() is None and time.time() < deadline:
        time.sleep(1)

    ok = False
    if done.is_set():
        print("[smoke] server is up", flush=True)
        for command in args.cmd:
            send(command)
            time.sleep(args.cmd_gap)
        time.sleep(args.settle)
        if args.kill:
            kill_tree(proc)
            ok = True
        else:
            send("stop")
            deadline = time.time() + args.stop_timeout
            while proc.poll() is None and time.time() < deadline:
                time.sleep(1)
            ok = proc.poll() is not None
            if not ok:
                print("[smoke] server did not exit after `stop`, killing", flush=True)
    elif proc.poll() is not None:
        print(f"[smoke] server exited before the Done banner (exit code {proc.returncode})", flush=True)
    else:
        print("[smoke] startup timeout, killing", flush=True)

    kill_tree(proc)
    pump_thread.join(10)
    print(f"[smoke] {'OK' if ok else 'FAILED'} - log: {args.log}", flush=True)
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
