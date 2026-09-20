/**
 * {@code pwd}: prints the current working directory.
 *
 * <p><b>Given to you as a worked example</b> — the simplest possible command.
 * {@code Shell.executeCommand} loads and calls this class's {@code main} directly in the same
 * JVM as the shell (no real OS process yet — that arrives in PA-1B), so
 * {@code System.getProperty("user.dir")} already reads the shell's own working directory.
 */
public class Pwd extends ShellCommand {

    public Pwd(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        ShellCommand.start(Pwd.class, args);
    }

    @Override
    protected void runCommand() {
        System.out.println(System.getProperty("user.dir"));
    }
}
