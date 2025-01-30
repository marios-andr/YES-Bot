package com.congueror.yesbot;

/*
import masecla.reddit4j.client.Reddit4J;
import masecla.reddit4j.client.UserAgentBuilder;
import masecla.reddit4j.objects.RedditPost;
import masecla.reddit4j.objects.Sorting;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public final class Reddit {

    private static Reddit4J reddit;

    static void initialize() {
        UserAgentBuilder userAgent = new UserAgentBuilder().appname("Y.E.S.").author(Constants.getSettings().reddit_username()).version("1.0");
        Reddit4J client = Reddit4J.rateLimited()
                .setUsername(Constants.getSettings().reddit_username())
                .setPassword(Constants.getSettings().reddit_password())
                .setClientId(Constants.getSettings().reddit_client())
                .setClientSecret(Constants.getSettings().reddit_secret())
                .setUserAgent(userAgent);

        try {
            client.connect();
            reddit = client;
        } catch (Exception e) {
            Constants.LOG.error("There was an error initializing the reddit API.", e);
        }
    }

    public static RedditPost getRandomSubmission(String subreddit) {
        if (reddit == null)
            initialize();

        Random random = new Random();
        int sorting = random.nextInt(0, 6);
        List<RedditPost> posts;
        try {
            posts = reddit.getSubreddit(subreddit).getListing(Sorting.values()[sorting]).submit();
        } catch (Exception e) {
            Constants.LOG.error("Something went wrong while getting a random submission.", e);
            return null;
        }

        List<String> allowedDomains = List.of("i.imgur.com", "i.redd.it", "preview.redd.it", "redgifs.com", "gfycat.com");

        int counter = 0;
        AtomicReference<RedditPost> post = new AtomicReference<>(null);
        do {
            int index = random.nextInt(0, posts.size());
            post.set(posts.get(index));
            counter++;

            if (counter > 15) {
                post.set(null);
                break;
            }
        } while (!post.get().is_self() || allowedDomains.stream().noneMatch(s -> post.get().getUrl().contains(s)));

        return post.get();
    }
}*/
