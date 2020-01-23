package com.learning.springbootajax.service;

import com.learning.springbootajax.domain.SocialMetaTag;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SocialMetaTagService {

    private static Logger log = LoggerFactory.getLogger(SocialMetaTagService.class);

    public SocialMetaTag getSocialMetaTagByUrl(String url){
        SocialMetaTag twitter = getTwitterCardByUrl(url);
        if (twitter != null) {
            if (!isEmpty(twitter)) {
                return twitter;
            }
        }
        SocialMetaTag openGraph = getOpenGraphByUrl(url);
        if (openGraph != null) {
            if (!isEmpty(openGraph)) {
                return openGraph;
            }
        }
        //nenhum nem outro
        return null;
    }

    private SocialMetaTag getOpenGraphByUrl(String url) {
        SocialMetaTag tag = new SocialMetaTag();

        try {
            Document doc = Jsoup.connect(url).get();
            tag.setTitle(doc.head().select("meta[property=og:title]").attr("content"));
            tag.setSite(doc.head().select("meta[property=og:site_name]").attr("content"));
            tag.setImage(doc.head().select("meta[property=og:image]").attr("content"));
            tag.setUrl(doc.head().select("meta[property=og:url]").attr("content"));
        } catch (IOException ioex) {
            log.error(ioex.getMessage(), ioex.getCause());
        }
        return tag;
    }

    private SocialMetaTag getTwitterCardByUrl(String url) {
        SocialMetaTag tag = new SocialMetaTag();

        try {
            Document doc = Jsoup.connect(url).get();
            tag.setTitle(doc.head().select("meta[name=twitter:title]").attr("content"));
            tag.setSite(doc.head().select("meta[name=twitter:site]").attr("content"));
            tag.setImage(doc.head().select("meta[name=twitter:image]").attr("content"));
            tag.setUrl(doc.head().select("meta[name=twitter:url]").attr("content"));
        } catch (IOException ioex) {
            log.error(ioex.getMessage(), ioex.getCause());
        }
        return tag;
    }

    private boolean isEmpty(SocialMetaTag tag) {
        if (tag.getImage().isEmpty())  return true;
        if (tag.getSite().isEmpty()) return true;
        if (tag.getUrl().isEmpty()) return true;
        if (tag.getTitle().isEmpty()) return true;
        return false;
    }
}
