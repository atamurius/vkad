package ua.atamurius.vk.music;

import java.io.*;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;

public class MusicPageParser {

    private static final Charset PAGE_CHARSET = Charset.forName("windows-1251");

    private final Pattern mp3 = Pattern.compile(
            "\"(http[^\"]+\\.mp3\\?extra=[^\"]+)\"");

    private final Pattern meta = Pattern.compile(
            "class=\"[^\"]*title_wrap[^>]*><[^>]+><a[^>]*>([^<]+)</a></b>[^<]*" +
                    "<span class=\"title\"[^>]*>(<a[^>]*>)?([^<]+)<");

    public interface ItemConsumer {
        void consume(String url, String author, String title);
    }

    public void parse(File file, ItemConsumer consumer) throws IOException {
        try (BufferedReader in =
                     new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(file),
                                PAGE_CHARSET))) {
            String line;
            String url = null;
            while (null != (line = in.readLine())) {
                Matcher m = mp3.matcher(line);
                if (m.find()) {
                    if (url != null)
                        System.err.println("Warning: URL without title: "+ url);
                    url = m.group(1);
                }
                else {
                    Matcher md = meta.matcher(line);
                    if (url != null && md.find()) {
                        String author = unescapeHtml4(md.group(1));
                        String title = unescapeHtml4(md.group(3));
                        consumer.consume(url, author, title);
                        url = null;
                    }
                }
            }
        }
    }
}
