package io.github.marios_andr.yesbot.util;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public record HtmlDocument(String type, HtmlElement elements) {

    public static HtmlDocument parse(Reader reader) throws IOException {
        StringBuilder s = new StringBuilder();
        String type;

        for (; ; ) {
            char c = (char) reader.read();

            if (s.toString().equalsIgnoreCase("<!doctype "))
                s.delete(0, s.length());

            if (c == '>') {
                type = s.toString();
                break;
            }

            s.append(c);
        }

        return new HtmlDocument(type, parseElement(reader, null));
    }

    private static HtmlElement parseElement(Reader reader, String topElement) throws IOException {
        StringBuilder s = new StringBuilder();
        String name = null;
        Map<String, String> attributes = new HashMap<>();
        String attributeName = null;
        List<HtmlElement> children = new ArrayList<>();

        boolean inEl = topElement != null;
        boolean first = true;
        boolean hangingAttribute = false;

        char c;
        for (; ; ) {
            int point = reader.read();
            if (point == -1)
                break;

            c = (char) point;

            if (c == '\n')
                continue;

            if (c == '/' && first) { // for closing tags
                for (; ; ) {
                    char c1 = (char) reader.read();

                    if (c1 == '>')
                        break;

                    s.append(c1);
                }

                if (topElement.equals(s.toString()))
                    return null;
            } else
                first = false;

            if (c == '<') {
                if (!s.isEmpty()) {
                    children.add(new HtmlElement(s.toString(), null, null)); // adds plain text to the elements
                    s.delete(0, s.length());
                }

                if (name == null && !inEl) { // if tag has not been update, update it.
                    inEl = true;
                    continue;
                } else { // if tag has been parsed, either child or closing tag proceeds.
                    HtmlElement el = parseElement(reader, name);
                    if (el == null)
                        break;
                    else
                        children.add(el);
                }
            }

            if (inEl) {

                if (name == null) {
                    if (c == ' ') { // assign element name and begin with attributes
                        name = s.toString();
                        s.delete(0, s.length());
                        continue;
                    }

                    if (c == '>') { // assign element name and end
                        name = s.toString();
                        s.delete(0, s.length());
                        inEl = false;
                        continue;
                    }
                } else if (attributeName == null) { // assign attribute name
                    if (c == '=') { // assign attribute with value
                        attributeName = s.toString();
                        hangingAttribute = true;
                        s.delete(0, s.length());
                        continue;
                    } else if (c == ' ') { // assign attribute with no value
                        attributeName = s.toString();
                        s.delete(0, s.length());
                        attributes.put(attributeName, "");
                        attributeName = null;
                        continue;
                    }
                } else if (hangingAttribute) {
                    if (c == '\'' || c == '"') { // add attribute with quotation delimiter
                        for (; ; ) {
                            char c1 = (char) reader.read();

                            if (c1 == c)
                                break;

                            s.append(c1);
                        }
                    } else { // add attribute with no delimiter
                        for (; ; ) {
                            char c1 = (char) reader.read();

                            if (c1 == ' ')
                                break;

                            s.append(c1);
                        }
                    }
                    attributes.put(attributeName, s.toString());
                    s.delete(0, s.length());
                    continue;
                }

                if (c == '>') { // tag ends
                    inEl = false;
                    continue;
                }
            }

            if (c == '>') {
                break;
            }


            s.append(c);
        }

        return new HtmlElement(name, attributes, children);
    }
}
