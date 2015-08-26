import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CheckAllLinkAndText {

    private static Matcher MATCHER;
    private static Pattern PATTERN;
    public static String baseUrl;
    protected static String levelChecks;
    protected static String depthTesting;
    protected Logger log = Logger.getLogger(CheckAllLinkAndText.class);
    private Properties props;
    private ConcurrentHashMap<String, String> mapOfUrlWithAnotherBase = new ConcurrentHashMap<String, String>();
    private ConcurrentHashMap<String, String> mapOfUrlLeadToExternalSites = new ConcurrentHashMap<String, String>();
    private ConcurrentHashMap<String, String> mapOfBrokenLinks = new ConcurrentHashMap<String, String>();
    static ConcurrentHashMap<String, String> newAllUrlsOnPage = new ConcurrentHashMap<String, String>();
    private Proxy proxy;
    ExecutorService serviceToCheckExternalLinks = Executors.newFixedThreadPool(5);
    public static String urls = "Incorrect urls: \n";
    String configFile = "applicationCheck.properties";
    final int step = 10;

    @Test
    public void test() throws InterruptedException {
        storeAndCheckAllLinks();
        log.info("shutdown");
        serviceToCheckExternalLinks.shutdown();
        serviceToCheckExternalLinks.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        if (mapOfBrokenLinks.isEmpty() && mapOfUrlWithAnotherBase.isEmpty()) {
            log.info("It's very good!");
        } else {
            mapOfBrokenLinks.putAll(mapOfUrlWithAnotherBase);
            for (Map.Entry entry : mapOfBrokenLinks.entrySet()) {
                urls += entry.getValue() + " broken on the page " + entry.getKey() + "\n";
            }
            Reporter.log(urls);
            Assert.fail(urls);
        }
    }


    public CheckAllLinkAndText() {
        System.setProperty("http.maxConnections", String.valueOf(30));
        System.setProperty("sun.net.http.errorstream.enableBuffering", "true");
        //baseUrl = System.getenv("baseUrl");
        File propertiesFile = new File("./log4j.properties");
        PropertyConfigurator.configure(propertiesFile.toString());
        props = new Properties();
        try {
            props.load(new FileReader(configFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //baseUrl = System.getProperty("baseUrl");
        if (baseUrl == null || baseUrl.equals("${tests.baseUrl}"))
            baseUrl = System.getenv("checkerBaseUrl");
        if (baseUrl == null) {
            baseUrl = props.getProperty("baseUrl");
            log.info(baseUrl);
        }
        levelChecks = System.getenv("levelChecks");
        if (levelChecks == null) {
            levelChecks = props.getProperty("levelChecks");
            log.info("levelChecks " + levelChecks);
        }
        depthTesting = System.getenv("depthTesting");
        if (depthTesting == null) {
            depthTesting = props.getProperty("depthTesting");
            log.info("Depth testing " + depthTesting);
        }

        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy-ho.msk.inn.ru", 3128));


        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }

    }


    public void addTaskToServiceWithMap(ConcurrentHashMap map) {
        final Integer sizeExtLinks = map.size();
        final Iterator<String> itK = map.keySet().iterator();
        final Iterator<String> itV = map.values().iterator();

        for (int i = 0; i < sizeExtLinks; i += step) {
            final Integer finalI = i;
            serviceToCheckExternalLinks.submit(new Runnable() {
                ConcurrentHashMap<String, String> subMap = new ConcurrentHashMap<String, String>();

                @Override
                public void run() {
                    if (finalI + step < sizeExtLinks) {
                        for (int i = finalI; i < finalI + step; i++) {
                            subMap.put(itK.next(), itV.next());
                        }
                        checkUrl(subMap);
                    } else {
                        for (int i = finalI; i < finalI + (sizeExtLinks - finalI); i++) {
                            subMap.put(itK.next(), itV.next());
                        }
                        checkUrl(subMap);
                    }
                }
            });
        }
    }


    public void storeAndCheckAllLinks() throws InterruptedException {
        final ConcurrentHashMap<String, String> allUrlsOnPage = getAllLinksOnPage(baseUrl);
        for (int i = 0; i < Integer.valueOf(levelChecks); i++) {
            allUrlsOnPage.putAll(storeUrlsOnPage(allUrlsOnPage));
        }
        addTaskToServiceWithMap(mapOfUrlLeadToExternalSites);
        addTaskToServiceWithMap(allUrlsOnPage);
        if (mapOfBrokenLinks.isEmpty() && mapOfUrlWithAnotherBase.isEmpty()) {
            log.info("It's very good!");
        } else {
            mapOfBrokenLinks.putAll(mapOfUrlWithAnotherBase);
            for (Map.Entry entry : mapOfBrokenLinks.entrySet()) {
                urls += entry.getValue() + " broken on the page " + entry.getKey() + "\n";
            }
            Reporter.log(urls);
            Assert.fail(urls);
        }
    }


    public HttpURLConnection sendRequest(String urlPage) {
        HttpURLConnection httpConn = null;
        try {
            URL url = new URL(urlPage);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setDoInput(true); // true if we want to read server's response
            httpConn.setDoOutput(false); // false indicates this is a GET request
        } catch (Exception e) {
            e.printStackTrace();
        }
        return httpConn;

    }

    public boolean verifyCodeOfRequest(String urlForCode) {
        int code = getCodeRequest(urlForCode);
        return code != 500 && code != 404;

    }

    public int getCodeRequest(String urlPage) {
        int code = 200;
        try {
            code = sendRequest(urlPage).getResponseCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code == -1 ? 500 : code;
    }

    public InputStream getInputStreamOfRequest(String urlPage) {
        try {
            return sendRequest(urlPage).getInputStream();
        } catch (Exception e) {
            return null;
        }

    }

    private void checkUrl(final ConcurrentHashMap<String, String> mapOfUrls) {
        int i = 0;
        for (Map.Entry entry : mapOfUrls.entrySet()) {
            if (verifyCodeOfRequest(entry.getValue().toString())) {
                log.info("External link " + entry.getValue().toString() + " is OK");
            } else {
                log.info("External link " + entry.getValue().toString() + " is BAD");
                mapOfBrokenLinks.put(entry.getKey().toString() + " " + i, entry.getValue().toString());
                i++;
            }
        }
    }


    private ConcurrentHashMap<String, String> storeUrlsOnPage(ConcurrentHashMap<String, String> allUrlsOnPage) {
        for (String urlFromProject : allUrlsOnPage.keySet())
            newAllUrlsOnPage.putAll(getAllLinksOnPage(urlFromProject));
        return newAllUrlsOnPage;
    }


    private ConcurrentHashMap<String, String> getAllLinksOnPage(String page) {
        ArrayList<String> linksOnPage = getLinks(page);
        int i = 0;
        ConcurrentHashMap<String, String> urlAllLinksOnPage = new ConcurrentHashMap<String, String>();
        for (String link : linksOnPage) {
            log.info(link);
            if (ExceptionsSearch.ExceptForLinksOnPage.checkExceptions(link)) {
                if (link.contains(baseUrl) && !link.contains("?"))
                    urlAllLinksOnPage.put(link, page);
                else if (link.contains("4game.")
                        && !(mapOfUrlWithAnotherBase.containsValue(link))
                        && (ExceptionsSearch.ExceptForAnotherBase.checkExceptions(link))
                        && !(link.equals("http://4game.com"))
                        && !(link.equals("https://4game.com"))
                        && !(link.equals("https://4game.com/"))) {
                    log.info("substring = " + link.substring(0, link.indexOf(".")));
                    mapOfUrlWithAnotherBase.put(page + " " + i, link);
                } else if (ExceptionsSearch.ExceptForExternalSite.checkExceptions(link)) {
                    mapOfUrlLeadToExternalSites.put(page + " " + i, link);
                }
            }
            i++;
        }
        return urlAllLinksOnPage;
    }

    private ArrayList<String> matchLinksFromPage(String result, String urlPage, boolean src) {
        ArrayList<String> links = new ArrayList<String>();
        PATTERN = Pattern.compile(!src ? "href=\"(\\S+)\"" : "src=\"(\\S+)\"");
        MATCHER = PATTERN.matcher(result);
        MATCHER.find();
        while (MATCHER.find()) {
            String link = MATCHER.group(1);
            if (link.contains("amp;serviceId=") || link.startsWith("#") || link.contains("javascript:void(0)"))
                continue;
            else if (link.contains("http") || link.contains("mailto"))
                links.add(link);
            else if (link.startsWith("//")) {
                if (baseUrl.contains("https:"))
                    links.add("https:" + link);
                else
                    links.add("http:" + link);
            } else if (link.startsWith("?")) {
                links.add(urlPage + link);
            } else if (link.startsWith("./")) {
                links.add(urlPage + link.substring(2));
            } else if (ExceptionsSearch.SpecialPageLinks.checkLinks(link)) {
                if (baseUrl.lastIndexOf("/") == baseUrl.length() - 1 && link.startsWith("/")) {
                    links.add(baseUrl + link.substring(1));
                } else {
                    links.add(baseUrl + link);
                }
            }
        }
        return links;
    }


    private ArrayList<String> getLinks(String urlPage) {
        ArrayList<String> links;
        String result = "";
        try {
            if (verifyCodeOfRequest(urlPage)) {
                BufferedReader in = new BufferedReader(new InputStreamReader(getInputStreamOfRequest(urlPage)));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    result += inputLine;
                }
                in.close();
            }
        } catch (Exception e) {
        }
        links = matchLinksFromPage(result, urlPage, false);
        if (depthTesting.equals("src"))
            links.addAll(matchLinksFromPage(result, urlPage, true));
        return links;
    }

    public static void main(String[] args) {
        CheckAllLinkAndText a = new CheckAllLinkAndText();
        //a.sendRequest("https://ru.4game.com/aion/play/update/");
    }


    
}
