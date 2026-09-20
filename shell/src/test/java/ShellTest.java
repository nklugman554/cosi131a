import com.github.stefanbirkner.systemlambda.SystemLambda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Timeout(value = 2, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
public class ShellTest {

    @BeforeEach
    public void setQuietOutput() {
        // Create in-memory buffers to catch the output quietly
        ByteArrayOutputStream outputBuffer1 = new ByteArrayOutputStream();
        PrintStream quietPrintStream1 = new PrintStream(outputBuffer1);
        System.setOut(quietPrintStream1);
        ByteArrayOutputStream outputBuffer2 = new ByteArrayOutputStream();
        PrintStream quietPrintStream2 = new PrintStream(outputBuffer2);
        System.setErr(quietPrintStream2);
    }

    private String simulateInput(Shell shell, String[] commands) throws Exception {
        InputStream originalIn = System.in;
        String simulatedInput = String.format("%s%n", String.join(System.lineSeparator(), commands));
        try {
            ByteArrayInputStream testInput = new ByteArrayInputStream(simulatedInput.getBytes());
            System.setIn(testInput);
            return SystemLambda.tapSystemOut(shell::runRepl);
        } finally {
            System.setIn(originalIn);
        }
    }

    @Test
    public void testGetPath() {
        Path cwd = Paths.get(System.getProperty("user.dir"));
        Path expected = cwd.resolve("../commandBin/target/classes").toAbsolutePath().normalize();
        Path actual = Shell.getPath();
        assertEquals(expected, actual);
    }

    @Test
    public void testExit() throws Exception {
        String[] input = {"exit"};
        // Test that shell exits and doesn't stay in loop
        String output = simulateInput(new Shell(), input);
        assertEquals(Shell.COMMAND_PROMPT, output);
    }

    @Test
    public void testCommands() throws Exception {
        String[] input = {"ls", "wc ../test-resources/file-1.txt", "exit"};
        String expectedLs = String.format("pom.xml%nsrc%ntarget%ntest-resources%n");
        String expectedWc = String.format("%8d %8d %8d ../test-resources/file-1.txt%n", 6, 5, 23);
        String expected = Shell.COMMAND_PROMPT + expectedLs + Shell.COMMAND_PROMPT + expectedWc + Shell.COMMAND_PROMPT;
        String output = simulateInput(new Shell(), input);
        assertEquals(expected, output);
    }

    @Test
    public void testInvalidCommand() throws Exception {
        String input = String.format("x%nexit%n");
        Shell shell = new Shell();
        InputStream originalIn = System.in;
        try {
            ByteArrayInputStream testInput = new ByteArrayInputStream(input.getBytes());
            System.setIn(testInput);
            String output = SystemLambda.tapSystemErr(shell::runRepl);
            assertEquals(String.format("x: command not found%n"), output);
        } finally {
            System.setIn(originalIn);
        }
    }

    @Test
    public void testCommandAfterError() throws Exception {
        String input = String.format("x%nls%nexit%n");
        String expectedErr = String.format("x: command not found%n");
        String expectedLs = String.format("pom.xml%nsrc%ntarget%ntest-resources%n");
        String expected = Shell.COMMAND_PROMPT + expectedErr + Shell.COMMAND_PROMPT + expectedLs + Shell.COMMAND_PROMPT;

        Shell shell = new Shell();
        InputStream originalIn = System.in;
        try {
            ByteArrayInputStream testInput = new ByteArrayInputStream(input.getBytes());
            System.setIn(testInput);
            String output = SystemLambda.tapSystemErrAndOut(shell::runRepl);
            assertEquals(expected, output);
        } finally {
            System.setIn(originalIn);
        }
    }
}
