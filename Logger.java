import java.io.PrintStream;

public class Logger {
    private PrintStream out = System.out;
    private PrintStream err = System.err;

    public PrintStream getStream() {
        return this.out;
    }

    public PrintStream getErrorStream() {
        return this.err;
    }

    private int verbosity = 2;

    public Logger() {}

    public Logger(PrintStream out, int verbosity) {
        this.out = out;
        this.verbosity = verbosity;
    }

    public boolean log(int verbosity, String msg) {
        if (this.verbosity >= verbosity) {
            out.println(msg);
            return true;
        }
        return false;
    }

    public boolean warn(int verbosity, String msg) {
        if (this.verbosity >= verbosity) {
            out.println("! " + msg);
            return true;
        }
        return false;
    }

    public void err(String msg) {
        err.println(msg);
    }
}