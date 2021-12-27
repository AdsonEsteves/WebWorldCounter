package wordcounter;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.customsearch.v1.Customsearch;
import com.google.api.services.customsearch.v1.CustomsearchRequestInitializer;
import com.google.api.services.customsearch.v1.model.Result;
import com.google.api.services.customsearch.v1.model.Search;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

public class testThings {

    private static String CLIENT_ID = "YOUR_CLIENT_ID";
    private static String CLIENT_SECRET = "YOUR_CLIENT_SECRET";

    private static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

    private static String OAUTH_SCOPE = "https://www.googleapis.com/auth/webmasters.readonly";

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        // googleAPIRun();
        // jsoupRun();
        // htmlunitRun();
        showChart();
        showChart2();
    }

    public static void showChart() {
        JFreeChart lineChart = ChartFactory.createLineChart(
                "chartTitle",
                "Years", "Number of Schools",
                createDataset(),
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
        JFrame frame = new JFrame();
        frame.add(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }

    private static DefaultCategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(15, "schools", "1970");
        dataset.addValue(30, "schools", "1980");
        dataset.addValue(60, "schools", "1990");
        dataset.addValue(120, "schools", "2000");
        dataset.addValue(240, "schools", "2010");
        dataset.addValue(300, "schools", "2014");
        return dataset;
    }

    public static void showChart2() {
        XYChart chart = new XYChartBuilder().xAxisTitle("X").yAxisTitle("Y").width(600).height(400).build();
        XYSeries series = chart.addSeries("linha", null, getRandomWalk(200));
        series.setMarker(SeriesMarkers.NONE);
        new SwingWrapper<XYChart>(chart).displayChart();
    }

    private static double[] getRandomWalk(int numPoints) {

        double[] y = new double[numPoints];
        y[0] = 0;
        for (int i = 1; i < y.length; i++) {
            y[i] = y[i - 1] + Math.random() - .5;
        }
        return y;
    }

    public static void googleAPIRun() throws IOException, GeneralSecurityException {

        String searchQuery = "Windows"; // The query to search
        String cx = "9d76013699ebc9a7e"; // Your search engine
        String APIKEY = "AIzaSyCTA2Sj5qLsQO1OEKIm23H16NsMbM232UA"; // Your search engine

        // Instance Customsearch
        Customsearch cs = new Customsearch.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(), null).setApplicationName("Google Search")
                        .setGoogleClientRequestInitializer(new CustomsearchRequestInitializer(APIKEY)).build();

        // Set search parameter
        Customsearch.Cse.List list = cs.cse().list().setCx(cx).setQ(searchQuery).setNum(10)
                // .setStart((long) 11.0)
                .setSort("date:r:20050815:20050931");

        // Execute search
        Search result = list.execute();
        if (result.getItems() != null) {
            for (Result ri : result.getItems()) {
                // Get title, link, body etc. from search
                System.out.println(ri.getTitle() + ", " + ri.getLink());
            }
        }

    }

    public static void jsoupRun() throws IOException {
        // "https://www.google.co.in/search?q=celulares2022&tbs=cdr:1,cd_min:10/11/2016,cd_max:19/11/2016");
        String url = "https://www.google.co.in/search?q=" + URLEncoder.encode("celulares em 2022", "UTF-8") + "&tbs="
                + URLEncoder.encode("cdr:1,cd_min:10/11/2016,cd_max:19/11/2016", "UTF-8");
        url = "https://google.com/search?q=Apple+&tbm=nws&num=100&tbs=cdr%3A1%2Ccd_min%3A01%2F19%2F2016%2Ccd_max%3A01%2F23%2F2016";
        System.out.println(url);
        List<String> searchedLinks = new ArrayList<>();

        // Connect to the url and obain HTML response
        Document doc = Jsoup.connect(url).userAgent("Mozilla").timeout(5000).get();
        // System.out.println(doc.body().html());
        // parsing HTML after examining DOM

        Elements els = doc.select("a[href]");
        for (Element el : els) {
            // Print title, site and abstract
            // System.out.println(el.text());
            String temp = el.attr("href");
            if (temp.startsWith("/url?q=")) {
                // use regex to get domain name
                String link = temp.replace("/url?q=", "").replaceAll("&sa=(.+)", "");

                if (!searchedLinks.contains(link)) {
                    searchedLinks.add(link);
                    System.out.println(link);
                }
            }
        }
    }

    private static final String FILE_NAME = "results.txt";

    private static final String QUERY = "JAVA";

    public static void htmlunitRun() {

        try {
            String url = "https://www.google.com/search?q=" + URLEncoder.encode("celulares em 2022", "UTF-8") + "&tbs="
                    + URLEncoder.encode("cdr:1,cd_min:01/11/2005,cd_max:11/11/2005", "UTF-8");
            WebClient wc = new WebClient();
            HtmlPage page = (HtmlPage) wc.getPage(url);
            List<Object> body = page.getByXPath("//div[@class='g']//a");
            System.out.println(url);
            for (Iterator iter = body.iterator(); iter.hasNext();) {
                HtmlAnchor anchor = (HtmlAnchor) iter.next();
                if (isSkipLink(anchor)) {
                    continue;
                }
                System.out.println(anchor.getHrefAttribute());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Decide if this link has to be processed.
     *
     * @param anchor link
     * @return true if link has to be omitted, false if is to be processed
     */
    private static boolean isSkipLink(HtmlAnchor anchor) {

        return anchor.getHrefAttribute().startsWith("/") || anchor.getHrefAttribute().startsWith("#")
                || anchor.getHrefAttribute().indexOf("/search?q=cache:") > 0;
    }
}
