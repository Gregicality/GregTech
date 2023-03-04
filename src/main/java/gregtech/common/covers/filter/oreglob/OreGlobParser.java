package gregtech.common.covers.filter.oreglob;

import gregtech.api.util.oreglob.OreGlob;
import gregtech.api.util.oreglob.OreGlobCompileResult;
import gregtech.api.util.oreglob.OreGlobCompileResult.Report;
import gregtech.common.covers.filter.oreglob.node.OreGlobNode;
import gregtech.common.covers.filter.oreglob.node.OreGlobNodes;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static gregtech.common.covers.filter.oreglob.OreGlobParser.TokenType.*;

/**
 * Top-down parser for oreGlob expression.
 * <pre>
 * oreGlob = [ FLAG ], [ or ], EOF
 *
 * or  = and, { '|', { '|' }, and }
 * and = xor, { '&', { '&' }, xor }
 * xor = not, { '^', not }
 *
 * not = '!', { '!' }, '(', [ or ], [ ')', [ not ] ]
 *     | { '!' }, primary, [ not ]
 *
 * primary = LITERAL
 *         | '(', [ or ], [ ')' ]
 *         | ( '*' | '?' ), { '*' | '?' }
 *
 * FLAG = '$', ? every character except whitespaces ?
 *
 * </pre>
 */
public final class OreGlobParser {

    private static final int CHAR_EOF = -1;

    private final String input;
    private final List<Report> reports = new ArrayList<>();

    private final IntSet flags = new IntOpenHashSet();

    private int inputIndex;

    private TokenType tokenType;
    private int tokenStart, tokenLength;
    @Nullable
    private String tokenLiteralValue;

    public OreGlobParser(String input) {
        this.input = input;
    }

    // Get codepoint at current position and incr index
    private int readNextChar() {
        if (input.length() <= this.inputIndex) return CHAR_EOF;
        int i = this.inputIndex;
        this.inputIndex += Character.isSurrogate(input.charAt(i)) ? 2 : 1;
        return input.codePointAt(i);
    }

    private void advance() {
        boolean first = this.inputIndex == 0;
        while (true) {
            int start = this.inputIndex;
            switch (readNextChar()) {
                case ' ': case '\t': case '\n': case '\r':
                    continue;
                case '(':
                    setCurrentToken(LPAR, start, 1);
                    return;
                case ')':
                    setCurrentToken(RPAR, start, 1);
                    return;
                case '|':
                    setCurrentToken(OR, start, 1);
                    return;
                case '&':
                    setCurrentToken(AND, start, 1);
                    return;
                case '!':
                    setCurrentToken(NOT, start, 1);
                    return;
                case '^':
                    setCurrentToken(XOR, start, 1);
                    return;
                case '*':
                    setCurrentToken(ANY, start, 1);
                    return;
                case '?':
                    setCurrentToken(ANY_CHAR, start, 1);
                    return;
                case '$':
                    if (!first) {
                        error("Tags at middle of expression", start, 1);
                    }
                    gatherTags();
                    continue;
                case CHAR_EOF:
                    setCurrentToken(EOF, input.length(), 0);
                    return;
                default:
                    this.inputIndex = start;
                    String literalValue = gatherLiteralValue();
                    setCurrentToken(LITERAL, start, inputIndex - start, literalValue);
                    return;
            }
        }
    }

    private void setCurrentToken(TokenType type, int start, int len) {
        setCurrentToken(type, start, len, null);
    }

    private void setCurrentToken(TokenType type, int start, int len, @Nullable String literalValue) {
        this.tokenType = type;
        this.tokenStart = start;
        this.tokenLength = len;
        this.tokenLiteralValue = literalValue;
    }

    private String getTokenSection() {
        return tokenType == EOF ?
                "** End of line **" :
                this.input.substring(this.tokenStart, this.tokenStart + this.tokenLength);
    }

    private String gatherLiteralValue() {
        StringBuilder stb = new StringBuilder();
        while (true) {
            int i = this.inputIndex;
            int c = readNextChar();
            switch (c) {
                case '\\':
                    c = readNextChar();
                    if (c == CHAR_EOF) {
                        error("End of file after escape character ('\\')", i, 1);
                        return stb.toString();
                    } else {
                        stb.appendCodePoint(c);
                        break;
                    }
                case ' ': case '\t': case '\n': case '\r': case '(': case ')':
                case '|': case '&': case '!': case '^': case '*': case '?': case '$':
                case CHAR_EOF:
                    this.inputIndex = i;
                    return stb.toString();
                default:
                    stb.appendCodePoint(c);
            }
        }
    }

    private void gatherTags() {
        while (true) {
            int i = this.inputIndex;
            int c = readNextChar();
            switch (c) {
                case '\\':
                    c = readNextChar();
                    if (c == CHAR_EOF) {
                        error("End of file after escape character ('\\')", i, 1);
                        return;
                    } else {
                        this.flags.add(c);
                        break;
                    }
                case ' ': case '\t': case '\n': case '\r':
                case CHAR_EOF:
                    return;
                default:
                    this.flags.add(c);
            }
        }
    }

    private boolean advanceIf(TokenType type) {
        if (tokenType != type) return false;
        advance();
        return true;
    }

    public OreGlobCompileResult compile() {
        advance();
        if (tokenType == EOF) {
            return new OreGlobCompileResult(
                    new OreGlob(new OreGlobVisualizer(OreGlobNodes.nothing()), String::isEmpty),
                    this.reports.toArray(new Report[0]));
        } else {
            OreGlobNode expr = or();
            if (tokenType != EOF) { // likely caused by program error, not user issue
                error("Unexpected token " + getTokenSection() + " after end of expression");
            }
            return new OreGlobCompileResult(
                    new OreGlob(new OreGlobVisualizer(expr), new OreGlobInterpreter(expr, !flags.contains('c'))),
                    this.reports.toArray(new Report[0]));
        }
    }

    private OreGlobNode or() {
        OreGlobNode expr = and();
        if (!advanceIf(OR)) return expr;
        List<OreGlobNode> nodes = new ArrayList<>();
        nodes.add(expr);
        do {
            // Eat through OR tokens as much as we can, to prevent scenario where
            // a disgusting C like lang users type || and complain their filter is broken
            // noinspection StatementWithEmptyBody
            while (advanceIf(OR)) ;
            nodes.add(and());
        } while (advanceIf(OR));
        return OreGlobNodes.or(nodes);
    }

    private OreGlobNode and() {
        OreGlobNode expr = xor();
        if (!advanceIf(AND)) return expr;
        List<OreGlobNode> nodes = new ArrayList<>();
        nodes.add(expr);
        do {
            // Eat through AND tokens as much as we can, to prevent scenario where
            // a disgusting C like lang users type && and complain their filter is broken
            // noinspection StatementWithEmptyBody
            while (advanceIf(AND)) ;
            nodes.add(xor());
        } while (advanceIf(AND));
        return OreGlobNodes.and(nodes);
    }

    private OreGlobNode xor() {
        OreGlobNode expr = not();
        if (!advanceIf(XOR)) return expr;
        List<OreGlobNode> nodes = new ArrayList<>();
        nodes.add(expr);
        do {
            // XOR token redundancy is not checked because it doesn't make any sense
            nodes.add(not());
        } while (advanceIf(XOR));
        return OreGlobNodes.xor(nodes);
    }

    private OreGlobNode not() {
        boolean inverted = false;
        while (advanceIf(NOT)) inverted = !inverted;
        OreGlobNode root;

        if (inverted && advanceIf(LPAR)) {
            inverted = false;
            if (advanceIf(RPAR)) { // negated empty, i.e. something
                root = OreGlobNodes.invert(OreGlobNodes.nothing());
            } else {
                root = OreGlobNodes.invert(or());
                switch (tokenType) {
                    case RPAR:
                        advance();
                    case EOF:
                        break;
                    default: // likely caused by program error, not user issue
                        error("Unexpected token " + getTokenSection() + " after end of expression");
                }
            }
        } else {
            root = primary();
        }

        switch (tokenType) {
            case NOT: case LITERAL: case LPAR: case ANY: case ANY_CHAR: // lookahead for not ruleset
                root = OreGlobNodes.setNext(root, not());
            default:
                return inverted ? OreGlobNodes.invert(root) : root;
        }
    }

    private OreGlobNode primary() {
        switch (tokenType) {
            case LITERAL:
                if (tokenLiteralValue != null) {
                    OreGlobNode result = OreGlobNodes.match(tokenLiteralValue);
                    advance();
                    return result;
                } else { // likely caused by program error, not user issue
                    error("Literal token without value");
                    advance();
                    return OreGlobNodes.error();
                }
            case LPAR:
                advance();
                switch (tokenType) {
                    case RPAR: // Empty group, i.e. nothing
                        advance();
                    case EOF:
                        // To preserve consistency between grouped expression below, enclosing parenthesis of nothing match is also optional
                        // For example, this is totally valid ore expression
                        //    (
                        // ...in the same logic the ore expression below is valid
                        //    ( ore* | ingot*
                        return OreGlobNodes.nothing();
                    default:
                        OreGlobNode result = or();
                        advanceIf(RPAR); // optional enclosing parenthesis
                        return result;
                }
            case ANY:
                return nOrMore(0, true);
            case ANY_CHAR:
                return nOrMore(1, false);
            case EOF:
                error("Unexpected end of expression");
                return OreGlobNodes.error();
            default:
                error("Unexpected token '" + getTokenSection() + "'");
                advance();
                return OreGlobNodes.error();
        }
    }

    private OreGlobNode nOrMore(int n, boolean more) {
        while (true) {
            advance();
            switch (tokenType) {
                case ANY_CHAR:
                    n++;
                    break;
                case ANY:
                    more = true;
                    break;
                default:
                    return OreGlobNodes.chars(n, more);
            }
        }
    }

    private void error(String message) {
        error(message, tokenStart, tokenLength);
    }

    private void error(String message, int start, int len) {
        this.reports.add(new Report(message, true, start, len));
    }

    private void warn(String message) {
        warn(message, tokenStart, tokenLength);
    }

    private void warn(String message, int start, int len) {
        this.reports.add(new Report(message, false, start, len));
    }

    enum TokenType {
        LITERAL,
        LPAR, // (
        RPAR, // )
        OR, // |
        AND, // &
        NOT, // !
        XOR, // ^
        ANY, // *
        ANY_CHAR, // ?
        EOF // End of file
    }
}
