package com.learning.springbootajax.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@SuppressWarnings("serial")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialMetaTag implements Serializable {
    private String site;
    private String title;
    private String url;
    private String image;


    @Override
    public String toString() {
        return "SocialMetaTag{" +
                "site='" + site + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
