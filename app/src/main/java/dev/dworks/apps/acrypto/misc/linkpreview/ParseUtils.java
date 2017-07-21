//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package dev.dworks.apps.acrypto.misc.linkpreview;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.Iterator;

public class ParseUtils {
    public ParseUtils() {
    }

    public static Link getLinkData(URL url, String html) {
        Link link = new Link();
        link.setUrl(url.getHost());
        Document document = Jsoup.parse(html);
        Elements elements = document.select("meta");
        link.setTitle(document.title());
        Iterator var5 = elements.iterator();

        while(true) {
            Element element;
            label58:
            do {
                while(true) {
                    while(var5.hasNext()) {
                        element = (Element)var5.next();
                        if(!element.attr("property").equalsIgnoreCase("og:image") && !element.attr("name").equalsIgnoreCase("image")) {
                            if(!element.attr("name").equalsIgnoreCase("description") && !element.attr("property").equalsIgnoreCase("og:description")) {
                                continue label58;
                            }

                            link.setDescription(element.attr("content"));
                        } else {
                            link.setImage(element.attr("content"));
                        }
                    }

                    link.setDescription(link.getDescription() != null?link.getDescription():"");
                    link.setTitle(link.getTitle() != null?link.getTitle():"");
                    link.setUrl(link.getUrl() != null?(link.getUrl().startsWith("www.")?link.getUrl():"www." + link.getUrl()):"");
                    link.setImage(link.getImage() != null?link.getImage():getLinkImage(document, url));
                    return link;
                }
            } while(!element.attr("name").equalsIgnoreCase("title") && !element.attr("property").equalsIgnoreCase("og:title"));

            link.setTitle(element.attr("content"));
        }
    }

    private static String getLinkImage(Document document, URL url) {
        Elements elements = document.getElementsByTag("img");
        String src = elements.size() > 0?elements.get(0).attr("src"):null;
        return src != null && src.startsWith("http")?src:(src != null?url.toString() + src:null);
    }
}
