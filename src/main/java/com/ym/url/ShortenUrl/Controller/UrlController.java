package com.ym.url.ShortenUrl.Controller;

import com.ym.url.ShortenUrl.Service.UrlService;
import com.ym.url.ShortenUrl.dao.UrlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.Map;


@RestController
public class UrlController {

    @Autowired
    private UrlService service;
    @Autowired
    private UrlMapper mapper;


    public static void main(String[] args) {
        String url = "www.yahoo.com";
        String url2 = "www.wwweee.com";
        if(alreadyExists(url2)){
            System.out.println(getShortUrl(url));
        }
        persistUrl(url);
        Integer id = findId(url);
        String shortUrl = String.valueOf(shortenUrl(id));
        updateUrl(shortUrl, id);
        System.out.println("Shortened URL"+shortUrl);
        System.out.println(getBackUrl(shortUrl));

    }


    private static void updateUrl(String shortUrl, int id) {
        EntityManager entityManager = getEntityManager();
        entityManager.getTransaction().begin();
        Query query = entityManager.createQuery("UPDATE UrlMapper u SET u.shortenedUrl = :shortenedUrl WHERE u.id = :uid");
        query.setParameter("shortenedUrl", shortUrl);
        query.setParameter("uid", id);
        query.executeUpdate();
        entityManager.getTransaction().commit();
    }

    @GetMapping("/app/{url}")
    public static String getShortUrl(@PathVariable("url") String url) {
        if (alreadyExists(url)) {
            EntityManager entityManager = getEntityManager();
            Query query = entityManager.createQuery("select u.shortenedUrl from UrlMapper u where u.orignalUrl= :url");
            query.setParameter("url", url);
            return (String) query.getResultList().get(0);
        } else {
            persistUrl(url);
            int id = findId(url);
            String shortUrl = String.valueOf(shortenUrl(id));
            updateUrl(shortUrl, id);
            return shortUrl;
        }
    }

    //  @GetMapping("/app/{url}")
    public static void createShortURL(@PathVariable("url") String url) {
        if (alreadyExists(url)) {
            System.out.println(getShortUrl(url));
        } else {
            persistUrl(url);
            int id = findId(url);
            String shortUrl = String.valueOf(shortenUrl(id));
            updateUrl(shortUrl, id);
            System.out.println(shortUrl);
        }

    }

    private static boolean alreadyExists(String url) {

        EntityManager entityManager = getEntityManager();

        try {
            Query query = entityManager.createQuery("select u.id from UrlMapper u where u.orignalUrl= :url");
            query.setParameter("url", url);
            if (query.getResultList() != null) {
                if (query.getResultList().size() == 1) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        } finally {
            entityManager.close();
        }
        return false;
    }

    private static int findId(String url) {
        EntityManager entityManager = getEntityManager();
        Query query = entityManager.createQuery("select u.id from UrlMapper u where u.orignalUrl= :url");
        query.setParameter("url", url);

        return (int) (query.getResultList().get(0));
    }

    private static void persistUrl(String urlName) {

        EntityManager entityManager = getEntityManager();
        entityManager.getTransaction().begin();

        UrlMapper url = new UrlMapper();
        url.setOrignalUrl(urlName);
        entityManager.persist(url);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    private static EntityManager getEntityManager() {
        return Persistence.createEntityManagerFactory("persistence").createEntityManager();
    }

    private static String getBackUrl(String shortUrl) {
        EntityManager entityManager = getEntityManager();
        Query query = entityManager.createQuery("select u.orignalUrl from UrlMapper u where u.shortenedUrl= :url");
        query.setParameter("url", shortUrl);
        return (String) query.getResultList().get(0);
    }

    private static StringBuffer shortenUrl(int number) {
        char map[] = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        StringBuffer shortUrl = new StringBuffer();
        while (number > 0) {
            shortUrl.append(map[number % 62]);
            number = number / 62;
        }
        return new StringBuffer("http://").append(shortUrl);
    }

}
