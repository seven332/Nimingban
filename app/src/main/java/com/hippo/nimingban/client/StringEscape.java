/*
 * Copyright 2015 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.nimingban.client;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class StringEscape {

    @StringDef({LANGUAGE_JSON})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Language {}

    private static final String LANGUAGE_JSON = "json";

    private static char charAt(String str, int index, @Language String language) throws UnescapeException {
        if (index < 0 || index >= str.length()) {
            throw new UnescapeException(language);
        }
        return str.charAt(index);
    }

    private static String substring(String str, int start, int end, @Language String language) throws UnescapeException {
        if (start >= 0 && start <= end && end <= str.length()) {
            return str.substring(start, end);
        } else {
            throw new UnescapeException(language);
        }
    }

    private static void checkQuote(char quote, @Language String language) throws UnescapeException {
        if (quote != '\'' && quote != '"') {
            throw new UnescapeException(language);
        }
    }

    public static String unescapeJson(String str) throws UnescapeException {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return "";
        }

        int index = 0;
        char c;
        char quote;

        // Get quote
        quote = charAt(str, index++, LANGUAGE_JSON);
        checkQuote(quote, LANGUAGE_JSON);

        StringBuilder sb = new StringBuilder();

        for (;;) {
            c = charAt(str, index++, LANGUAGE_JSON);
            switch (c) {
                case 0:
                case '\n':
                case '\r':
                    throw new UnescapeException(LANGUAGE_JSON);
                case '\\':
                    c = charAt(str, index++, LANGUAGE_JSON);
                    switch (c) {
                        case 'b':
                            sb.append('\b');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 'u':
                            sb.append((char) Integer.parseInt(substring(str, index, index += 4, LANGUAGE_JSON), 16));
                            break;
                        case 'x' :
                            sb.append((char) Integer.parseInt(substring(str, index, index += 2, LANGUAGE_JSON), 16));
                            break;
                        default:
                            sb.append(c);
                    }
                    break;
                default:
                    if (c == quote) {
                        return sb.toString();
                    } else {
                        sb.append(c);
                    }
            }
        }
    }

    public static class UnescapeException extends Exception {

        public UnescapeException(@Language String language) {
            super("Can't unescape " + language);
        }
    }
}
