/*
 * Copyright (c) 2018 Joerg Bayer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.sg_o.app.miio.base;

public class CommandExecutionException extends Exception {
    private static final long serialVersionUID = -3409332042121819756L;

    public enum Error {
        TIMEOUT(0),
        UNKNOWN_METHOD(1),
        DEVICE_NOT_FOUND(2),
        EMPTY_RESPONSE(3),
        INVALID_RESPONSE(4),
        IP_OR_TOKEN_UNKNOWN(5),
        INVALID_PARAMETERS(6),
        NOT_IMPLEMENTED(7),
        UNKNOWN(8);
        public final int cause;

        Error(int cause) {
            this.cause = cause;
        }
    }

    private Error error;
    private String reason;

    @SuppressWarnings("WeakerAccess")
    public CommandExecutionException(Error error) {
        if (error == null) error = Error.UNKNOWN;
        this.error = error;
        this.reason = error.name();
    }

    public CommandExecutionException(Error error, String reason) {
        if (error == null) error = Error.UNKNOWN;
        this.error = error;
        if (reason == null) reason = error.name();
        this.reason = reason;
    }

    public Error getError() {
        return error;
    }

    @Override
    public String toString() {
        return error.name() + ": " + reason;
    }
}
