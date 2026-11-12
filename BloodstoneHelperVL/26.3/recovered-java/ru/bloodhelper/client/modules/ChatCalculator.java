/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class ChatCalculator {
    private static final MathContext PRECISION = new MathContext(24, RoundingMode.HALF_UP);

    private ChatCalculator() {
    }

    public static boolean isExpression(String message) {
        return message != null && message.stripLeading().startsWith("=");
    }

    public static Result calculate(String message) {
        if (!ChatCalculator.isExpression(message)) {
            return new Result("", Error.SYNTAX);
        }
        String expression = message.stripLeading().substring(1).trim().replace('\u2212', '-');
        if (expression.isEmpty()) {
            return new Result("", Error.EMPTY);
        }
        if (expression.length() > 512) {
            return new Result("", Error.TOO_COMPLEX);
        }
        try {
            String text;
            Parser parser = new Parser(expression);
            BigDecimal value = parser.expression(0).stripTrailingZeros();
            parser.whitespace();
            if (parser.at != expression.length()) {
                throw new Invalid(Error.SYNTAX);
            }
            if (Math.abs(value.scale()) > 180 || value.precision() > 180) {
                throw new Invalid(Error.TOO_LARGE);
            }
            String string = text = value.signum() == 0 ? "0" : value.toPlainString();
            if (text.length() > 200) {
                throw new Invalid(Error.TOO_LARGE);
            }
            return new Result(text, Error.NONE);
        }
        catch (Invalid error) {
            return new Result("", error.error);
        }
        catch (ArithmeticException | NumberFormatException error) {
            return new Result("", Error.TOO_LARGE);
        }
    }

    public record Result(String value, Error error) {
        public boolean valid() {
            return this.error == Error.NONE;
        }
    }

    public static enum Error {
        NONE,
        EMPTY,
        SYNTAX,
        DIVISION_BY_ZERO,
        TOO_COMPLEX,
        TOO_LARGE;

    }

    private static final class Parser {
        final String text;
        int at;
        int operations;

        Parser(String text) {
            this.text = text;
        }

        void whitespace() {
            while (this.at < this.text.length() && Character.isWhitespace(this.text.charAt(this.at))) {
                ++this.at;
            }
        }

        boolean take(char value) {
            this.whitespace();
            if (this.at < this.text.length() && this.text.charAt(this.at) == value) {
                ++this.at;
                return true;
            }
            return false;
        }

        void budget(int depth) {
            if (depth > 32 || ++this.operations > 256) {
                throw new Invalid(Error.TOO_COMPLEX);
            }
        }

        BigDecimal bound(BigDecimal value) {
            if (Math.abs(value.scale()) > 512 || value.precision() > 512) {
                throw new Invalid(Error.TOO_LARGE);
            }
            return value;
        }

        BigDecimal expression(int depth) {
            this.budget(depth);
            BigDecimal value = this.product(depth);
            while (true) {
                if (this.take('+')) {
                    this.budget(depth);
                    value = this.bound(value.add(this.product(depth), PRECISION));
                    continue;
                }
                if (!this.take('-')) break;
                this.budget(depth);
                value = this.bound(value.subtract(this.product(depth), PRECISION));
            }
            return value;
        }

        BigDecimal product(int depth) {
            BigDecimal value = this.unary(depth);
            while (true) {
                BigDecimal other;
                if (this.take('*') || this.take('\u00d7')) {
                    this.budget(depth);
                    value = this.bound(value.multiply(this.unary(depth), PRECISION));
                    continue;
                }
                if (this.take('/') || this.take('\u00f7')) {
                    this.budget(depth);
                    other = this.unary(depth);
                    if (other.signum() == 0) {
                        throw new Invalid(Error.DIVISION_BY_ZERO);
                    }
                    value = this.bound(value.divide(other, PRECISION));
                    continue;
                }
                if (!this.take('%')) break;
                this.budget(depth);
                other = this.unary(depth);
                if (other.signum() == 0) {
                    throw new Invalid(Error.DIVISION_BY_ZERO);
                }
                value = this.bound(value.remainder(other, PRECISION));
            }
            return value;
        }

        BigDecimal unary(int depth) {
            this.budget(depth);
            if (this.take('+')) {
                return this.unary(depth + 1);
            }
            if (this.take('-')) {
                return this.unary(depth + 1).negate(PRECISION);
            }
            BigDecimal value = this.atom(depth);
            if (this.take('^')) {
                int power;
                BigDecimal exponent = this.unary(depth + 1);
                try {
                    power = exponent.intValueExact();
                }
                catch (ArithmeticException error) {
                    throw new Invalid(Error.SYNTAX);
                }
                if (Math.abs((long)power) > 100L) {
                    throw new Invalid(Error.TOO_COMPLEX);
                }
                if (power < 0 && value.signum() == 0) {
                    throw new Invalid(Error.DIVISION_BY_ZERO);
                }
                return this.bound(value.pow(power, PRECISION));
            }
            return value;
        }

        BigDecimal atom(int depth) {
            this.budget(depth);
            if (this.take('(')) {
                BigDecimal value = this.expression(depth + 1);
                if (!this.take(')')) {
                    throw new Invalid(Error.SYNTAX);
                }
                return value;
            }
            this.whitespace();
            int start = this.at;
            int digits = 0;
            boolean decimal = false;
            while (this.at < this.text.length()) {
                char c = this.text.charAt(this.at);
                if (c >= '0' && c <= '9') {
                    ++digits;
                    ++this.at;
                    continue;
                }
                if (c != '.' && c != ',' || decimal) break;
                decimal = true;
                ++this.at;
            }
            if (digits == 0) {
                throw new Invalid(Error.SYNTAX);
            }
            if (digits > 64) {
                throw new Invalid(Error.TOO_LARGE);
            }
            return new BigDecimal(this.text.substring(start, this.at).replace(',', '.'), PRECISION);
        }
    }

    private static final class Invalid
    extends RuntimeException {
        final Error error;

        Invalid(Error error) {
            super(null, null, false, false);
            this.error = error;
        }
    }
}

