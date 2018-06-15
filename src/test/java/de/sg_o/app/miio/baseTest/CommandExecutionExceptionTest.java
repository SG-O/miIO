package de.sg_o.app.miio.baseTest;

import de.sg_o.app.miio.base.CommandExecutionException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommandExecutionExceptionTest {
    private CommandExecutionException e0 = new CommandExecutionException(CommandExecutionException.Error.TIMEOUT);
    private CommandExecutionException e1 = new CommandExecutionException(CommandExecutionException.Error.UNKNOWN_METHOD);
    private CommandExecutionException e2 = new CommandExecutionException(CommandExecutionException.Error.DEVICE_NOT_FOUND);
    private CommandExecutionException e3 = new CommandExecutionException(CommandExecutionException.Error.EMPTY_RESPONSE);
    private CommandExecutionException e4 = new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
    private CommandExecutionException e5 = new CommandExecutionException(CommandExecutionException.Error.IP_OR_TOKEN_UNKNOWN);
    private CommandExecutionException e6 = new CommandExecutionException(CommandExecutionException.Error.INVALID_PARAMETERS);
    private CommandExecutionException e7 = new CommandExecutionException(CommandExecutionException.Error.NOT_IMPLEMENTED);
    private CommandExecutionException e8 = new CommandExecutionException(CommandExecutionException.Error.UNKNOWN);

    private CommandExecutionException e9 = new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE, "Error");

    private CommandExecutionException e10 = new CommandExecutionException(null);
    private CommandExecutionException e11 = new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE, null);
    private CommandExecutionException e12 = new CommandExecutionException(null, "Error");

    @Test
    public void getErrorTest() {
        assertEquals(CommandExecutionException.Error.TIMEOUT, e0.getError());
        assertEquals(CommandExecutionException.Error.UNKNOWN_METHOD, e1.getError());
        assertEquals(CommandExecutionException.Error.DEVICE_NOT_FOUND, e2.getError());
        assertEquals(CommandExecutionException.Error.EMPTY_RESPONSE, e3.getError());
        assertEquals(CommandExecutionException.Error.INVALID_RESPONSE, e4.getError());
        assertEquals(CommandExecutionException.Error.IP_OR_TOKEN_UNKNOWN, e5.getError());
        assertEquals(CommandExecutionException.Error.INVALID_PARAMETERS, e6.getError());
        assertEquals(CommandExecutionException.Error.NOT_IMPLEMENTED, e7.getError());
        assertEquals(CommandExecutionException.Error.UNKNOWN, e8.getError());
        assertEquals(CommandExecutionException.Error.INVALID_RESPONSE, e9.getError());
        assertEquals(CommandExecutionException.Error.UNKNOWN, e10.getError());
        assertEquals(CommandExecutionException.Error.INVALID_RESPONSE, e11.getError());
        assertEquals(CommandExecutionException.Error.UNKNOWN, e12.getError());
    }

    @Test
    public void toStringTest() {
        assertEquals("TIMEOUT: TIMEOUT", e0.toString());
        assertEquals("UNKNOWN_METHOD: UNKNOWN_METHOD", e1.toString());
        assertEquals("DEVICE_NOT_FOUND: DEVICE_NOT_FOUND", e2.toString());
        assertEquals("EMPTY_RESPONSE: EMPTY_RESPONSE", e3.toString());
        assertEquals("INVALID_RESPONSE: INVALID_RESPONSE", e4.toString());
        assertEquals("IP_OR_TOKEN_UNKNOWN: IP_OR_TOKEN_UNKNOWN", e5.toString());
        assertEquals("INVALID_PARAMETERS: INVALID_PARAMETERS", e6.toString());
        assertEquals("NOT_IMPLEMENTED: NOT_IMPLEMENTED", e7.toString());
        assertEquals("UNKNOWN: UNKNOWN", e8.toString());
        assertEquals("INVALID_RESPONSE: Error", e9.toString());
        assertEquals("UNKNOWN: UNKNOWN", e10.toString());
        assertEquals("INVALID_RESPONSE: INVALID_RESPONSE", e11.toString());
        assertEquals("UNKNOWN: Error", e12.toString());
    }
}