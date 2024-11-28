import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.awt.X11.XBaseWindow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

import static java.lang.System.in;
import static java.lang.System.out;

public class WebsiteScrapper {
    private static final String DB_URL = "jdbc:postgres://localhost:5432/web_scrapper";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "mupro";

    public static void main(String[] args) {
        String inputUrl = null;
        String domainName;
        try {
            System.out.println("Enter the website URL: ");
            inputUrl = new Scanner(in).nextLine();
            if (!validateUrl(inputUrl)) {
                System.out.println("Invalid Url. Exiting...");
                return;
            }

        try {
            domainName = getDomainName(inputUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        File baseDirectory = new File(domainName);
        if(!baseDirectory.exists() && !baseDirectory.mkdirs()){
            System.out.println("Failed to create directory for the website.");
            return;
        }
        LocalDateTime startTime = LocalDateTime.now();
        long totalDownloadedBytes = downloadWebsite(inputUrl, baseDirectory);
        LocalDateTime endTime = LocalDateTime.now();
        long elapsedTime = Duration.between(startTime, endTime).toMillis();
        saveWebsiteReport(domainName, startTime, endTime, elapsedTime,  totalDownloadedBytes);
            System.out.println("Website download complete!!!!!!!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


    }
    public static boolean validateUrl(String url){
        return url.startsWith("http://") || url.startsWith("https://");
    }
    private static String getDomainName(String url)throws MalformedURLException {
        URL parsedUrl = new URL(url);
        return parsedUrl.getHost().replace("www\\.", "");
    }
    private static long downloadWebsite(String url, File baseDirectory) throws Exception {
        Set<String> visitedLinks= new HashSet<>();
        visitedLinks.add(url);

        Document doc = Jsoup.connect(url).get();
        File homepage = new File(baseDirectory, "index.html");
        try(FileOutputStream out = new FileOutputStream(homepage)){
            out.write(doc.html().getBytes());
        }
        Elements links = doc.select("a[href]");
        long totalDownloadedBytes = homepage.length();

        for(Element link: links){
            String linkHref = link.attr("abs:href");
            if(visitedLinks.add(linkHref)){
                System.out.println("Downloading: " + linkHref);
                long bytes = downloadResource(linkHref, baseDirectory);
                totalDownloadedBytes += bytes;
                saveLinkReport(linkHref, bytes);
            }
        }
        return totalDownloadedBytes;
    }

    private static long downloadResource(String resourceUrl, File baseDirectory) throws Exception{
        URL url = new URL(resourceUrl);
        String fileName = resourceUrl.substring(resourceUrl.indexOf("/" +1)).split("\\?")[0];
        File file = new File(baseDirectory, fileName.isEmpty()? "index.html" : fileName);
        byte[] buffer = new byte[1024];
        int bytesRead;
        long totalBytes = 0;
        while ((bytesRead = in.read(buffer)) != -1){
            out.write(buffer, 0, bytesRead);
            totalBytes += bytesRead;
        }
        return totalBytes;
    }
    private static void saveWebsiteReport(String websiteName, LocalDateTime start, LocalDateTime end, long elapsedTime, long totalBytes){
        try(Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement preparedStatement = conn.prepareStatement( "INSERT INTO website (website_name, download_start_date_time, download_end_date_time, total_elapsed_time, total_downloaded_kilobytes) VALUES (?, ?, ?, ?, ?)")){
            preparedStatement.setString(1, websiteName);
            preparedStatement.setTimestamp(2, Timestamp.valueOf(start));
            preparedStatement.setTimestamp(3, Timestamp.valueOf(end));
            preparedStatement.setLong(4, elapsedTime);
            preparedStatement.setLong(5, totalBytes / 1024);
            preparedStatement.executeUpdate();

        }catch (SQLException e){
            out.println(e.getMessage());
        }
    }
    private static void saveLinkReport(String linkName, long bytes) {
        try(Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO link (link_name, total_downloaded_kilobytes) VALUES (?, ?)")){
            preparedStatement.setString(1, linkName);
            preparedStatement.setLong(2, bytes / 1024);
            preparedStatement.executeUpdate();

        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }


}
