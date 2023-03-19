package searchengine.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlFilter {
    private static final Pattern pattern = Pattern.compile("^/.*(?:\\.html)|(^/)#$|^(?>(?!\\.html).)*$");
    private String url;

    public UrlFilter(String url) {
        this.url = url;
    }

    public String filter() {
        return filter(url);
    }

    public static String filter(String url) {
        if (!(url.isEmpty() || url.charAt(0) != '/'))
            return "/";
        Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(0) : "/";
    }
}
