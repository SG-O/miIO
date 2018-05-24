package base;

public class CommandExecutionException extends Exception {
    public enum Error {
        TIMEOUT(0),
        UNKNOWN_METHOD(1),
        DEVICE_NOT_FOUND(2),
        EMPTY_RESPONSE(3),
        INVALID_RESPONSE(4),
        IP_OR_TOKEN_UNKNOWN(5);
        public final int cause;   // Symbol of unit

        Error(int cause) {
            this.cause = cause;
        }
    }

    private Error error;

    @SuppressWarnings("WeakerAccess")
    public CommandExecutionException(Error error) {
        this.error = error;
    }

    public Error getError() {
        return error;
    }

    @Override
    public String toString() {
        return error.name();
    }
}
