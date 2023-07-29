package com.congueror.yesbot;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Submission;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;

import java.net.ProtocolException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class Reddit {

    private static RedditClient reddit;

    static void initialize() {
        UserAgent userAgent = new UserAgent("Y.E.S.", "net.congueror.yesbot", "0.1", Constants.getSettings().reddit_username());

        // Create our credentials
        Credentials credentials = Credentials.script(Constants.getSettings().reddit_username(), Constants.getSettings().reddit_password(),
                Constants.getSettings().reddit_client(), Constants.getSettings().reddit_secret());

        NetworkAdapter adapter = new OkHttpNetworkAdapter(userAgent);

        try {
            reddit = OAuthHelper.automatic(adapter, credentials);
        } catch (Exception e) {
            Constants.LOG.error("There was an error initializing the reddit API.", e);
        }
    }

    public static Submission getRandomSubmission(String subreddit) {
        if (reddit == null)
            initialize();

        int counter = 0;
        AtomicReference<Submission> post = new AtomicReference<>(reddit.subreddit(subreddit).randomSubmission().getSubject());
        List<String> allowedDomains = List.of("i.imgur.com", "i.redd.it", "preview.redd.it", "redgifs.com", "gfycat.com");
        while (!(!post.get().isSelfPost() && (allowedDomains.stream().anyMatch(s -> post.get().getUrl().contains(s))))) {
            post.set(reddit.subreddit(subreddit).randomSubmission().getSubject());
            counter++;
            if (counter > 10) {
                break;
            }
        }
        return post.get();
    }
}
