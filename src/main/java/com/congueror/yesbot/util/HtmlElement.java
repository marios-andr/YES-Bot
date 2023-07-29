package com.congueror.yesbot.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.congueror.yesbot.Constants.LOG;

public record HtmlElement(String element, Map<String, String> attributes, List<HtmlElement> children) {

    public HtmlElement(String element, Map<String, String> attributes, List<HtmlElement> children) {
        this.element = element;
        this.attributes = attributes == null ? Collections.emptyMap() : Collections.unmodifiableMap(attributes);
        this.children = children == null ? Collections.emptyList() : Collections.unmodifiableList(children);
    }

    public void print() {
        print(0);
    }

    private void print(int depth) {
        for (int i = 0; i < depth; i++) {
            System.out.print("  ");
        }
        System.out.print("[" + element + " ");
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            char del = entry.getValue().contains("\"") ? '\'' : '"';
            System.out.print(entry.getKey() + "=" + del + entry.getValue() + del);
        }
        System.out.print("]: \n");

        for (HtmlElement child : children) {
            child.print(depth++);
        }
    }
}
