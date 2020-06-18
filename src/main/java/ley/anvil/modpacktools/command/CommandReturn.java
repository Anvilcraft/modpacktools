package ley.anvil.modpacktools.command;

public class CommandReturn {
    private final String ret;
    private final boolean success;

    private CommandReturn(String ret, boolean success) {
        this.ret = ret;
        this.success = success;
    }

    public static CommandReturn fail(String ret) {
        return new CommandReturn(ret, false);
    }

    public static CommandReturn success() {
        return success("");
    }

    public static CommandReturn success(String ret) {
        return  new CommandReturn(ret, true);
    }

    public boolean hadSuccess() {
        return success;
    }

    public String getRet() {
        return ret;
    }
}
