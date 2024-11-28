import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class WebsiteScrapper {
    private static final String DB_URL = "jdbc:postgres://localhost:5432/web_scrapper";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "mupro";

    public static void main(String[] args){
        try{
            System.out.println("Enter the website URL: ");
            String inputUrl = new Scanner(System.in).nextLine();
        }
    }
    public static boolean validateUrl(String url){
        return url.startsWith("http://") || url.startsWith("https://");
    }
    public static String getDomainName(String url)throws MalformedURLException {
        URL parsedUrl = new URL(url);
        return parsedUrl.getHost().replace("www\\.", "");
    }
    public static long downloadWebsite(String url, File baseDirectory) throws IOException {
        Set<String> visitedLinks= new HashSet<>();
        visitedLinks.add(url);

        Document doc = Jsoup.connect(url).get();
        File homepage = new File(baseDirectory, "index.html");
        try(FileOutputStream out = ){

        }

    }
    
}
