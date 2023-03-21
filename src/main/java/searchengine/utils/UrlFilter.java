package searchengine.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlFilter {
    // Main patter for match /, /#, /.../xxx.html
    private static final Pattern pattern = Pattern.compile("^/.*?(?:\\.html)|^(?>(?!\\.html).)*$");
    // Alter pattern for match /, /#, /.../xxx.htm, ...html, ...php
    private static final Pattern altPattern = Pattern.compile("^/.*?(?:\\.(x?html?|php))|^(?>(?!\\.html).)*$");
    private String url;

    public UrlFilter(String url) {
        this.url = url;
    }

    public String filter() {
        return filter(url, pattern);
    }

    public String filter(boolean useAltPattern) {
        return useAltPattern ? filter(url, altPattern) : filter(url, pattern);
    }

    public static String filter(String url, Pattern pattern) {
        if (url.isEmpty() || url.charAt(0) != '/')
            return "/";
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(0).equals("/#") ? "/" : matcher.group(0);
        }
        return "/";
    }

    public static String filter(String url) {
        return filter(url, pattern);
    }

    public static String filter(String url, boolean useAltPattern) {
        return useAltPattern ? filter(url, altPattern) : filter(url, pattern);
    }
}
