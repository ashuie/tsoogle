package assignment;

import java.io.*;
import java.util.*;

public class RandomWebGenerator {

    public static final String pagecontentdb = "./randomwebpagecontent.db";
    public static final String outputDir = "random_web";
    public static ArrayList<String> words;
    public static HashMap<Page, String> pageContent;

    public static void main(String[] args) throws IOException {
        int pages = 100;
        int links = 10;
        pageContent = new HashMap<>();
        generateWeb(pages, links, outputDir);
    }

    public static void generateWeb(int numPages, int numLinks, String outputDir) throws IOException {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        BufferedReader br = new BufferedReader(new FileReader("./words.txt"));
        words = new ArrayList<>();
        while (br.ready()) {
            words.add(br.readLine());
        }
        List<String> allPages = new ArrayList<>();
        for (int i = 0; i < numPages; i++) {
            allPages.add("page" + (i + 1) + ".html");
        }
        Random rand = new Random();
        Map<String, List<String>> graph = new HashMap<>();
        for (String page : allPages) {
            graph.put(page, new ArrayList<>());
        }
        for (int i = 1; i < numPages; i++) {
            String currPage = allPages.get(i);
            String randPage = allPages.get(rand.nextInt(i));
            graph.get(randPage).add(currPage);
        }
        for (int i = 0; i < numPages; i++) {
            String currPage = allPages.get(i);
            List<String> links = graph.get(currPage);
            int randomLinks = rand.nextInt(numLinks + 1);
            for (int j = 0; j < randomLinks; j++) {
                String randPage = allPages.get(rand.nextInt(numPages));
                if (!links.contains(randPage) && !randPage.equals(currPage)) {
                    links.add(randPage);
                }
            }
        }
        int index = 1;
        for (String page : allPages) {
            String fileName = outputDir + "/" + page;
            try (FileWriter writer = new FileWriter(fileName)) {
                String title = "Page " + index;
                String content = randomText(rand, rand.nextInt(100));
                writer.write("<html>\n<head><title>" + title + " </title></head>\n<body>\n" +
                        "<h1>" + title + "</h1>\n"
                        + "<p>" + content + "</p>\n");
                for (String linkedPage : graph.get(page)) {
                    writer.write("<a href=\"" + linkedPage + "\">Link to " + linkedPage + "</a><br>\n");
                }
                writer.write("</body>\n</html>\n");
                index++;

                pageContent.put(new Page(new File(fileName).toURI().toURL()), content);
            }
        }
        System.out.println("Finished writing to directory: " + outputDir);
        try(ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(pagecontentdb))) {
            oout.writeObject(pageContent);
        } catch (Exception e) {
            System.err.println("Error saving page content");
        }
    }

    private static String randomText(Random rand, int wordCount) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            text.append(words.get(rand.nextInt(words.size())));
            if (i < wordCount - 1) {
                text.append(" ");
            }
        }
        return text.toString();
    }
}