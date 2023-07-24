import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Main {
    static final int timeout = 10*1000*60;

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String url;
        Document doc;
        Elements mangas;
        do {
            System.out.print("Search manga: ");
            String search = br.readLine().replaceAll(" ", "_");
            url = "https://manganato.com/search/story/"+search;
            doc = get(url);
            mangas = doc.select("a.a-h.text-nowrap.item-title");
        } while (mangas.isEmpty());

        if (doc.selectFirst("div.panel-page-number") != null) {
            int last = Integer.parseInt(doc.selectFirst("a.page-blue.page-last")
                    .text().replaceAll("[^0-9]", ""));
            for (int i = 2; i <= last; i++) {
                doc = get(url+"?page="+i);
                mangas.addAll(doc.select("a.a-h.text-nowrap.item-title"));
            }
        }

        for (int i = 0; i < mangas.size(); i++)
            System.out.printf("[%d] %s%n", i+1, mangas.get(i).text());
        int selection;
        do {
            System.out.printf("Please select a manga [1-%d]: ", mangas.size());
            selection = Integer.parseInt(br.readLine());
        } while (selection < 1 || selection > mangas.size());
        Element manga = mangas.get(selection-1);

        System.out.println("Preparing to download manga: "+manga.text());

        url = manga.attr("href");
        doc = get(url);
        Elements chapters = doc.select("a.chapter-name.text-nowrap");
        Collections.reverse(chapters);

        List<List<String>> data = new ArrayList<>();

        for (int i = 0; i < chapters.size(); i++) {
            url = chapters.get(i).attr("href");
            doc = get(url);
            Element container = doc.selectFirst("div.container-chapter-reader");
            Elements pages = container.children().select("img");
            String chapterName = cleanse(chapters.get(i).text());
            for (int j = 0; j < pages.size(); j++) {
                String img = pages.get(j).attr("src");
                System.out.println("Targeting image: "+img);
                data.add(Arrays.asList(
                        String.format("[%d] %s", i+1, chapterName),
                        String.format("%03d", j+1),
                        img
                ));
            }
        }

        boolean isWindows = System.getProperty("os.name").startsWith("Windows");
        String home = isWindows ? "USERPROFILE" : "HOME";
        String dir = System.getenv(home)+"/manga_test/img/"+manga.text()+"/";
        Runtime runtime = Runtime.getRuntime();
        List<Process> processes = new ArrayList<>();
        for (List<String> datum : data) {
            String fileLocation = dir+datum.get(0)+"/page"+datum.get(1)+".jpg";
            System.out.println("Downloading to file (if not exists): "+fileLocation);
            File file = new File(fileLocation);
            if (!file.isFile()) {
//                file.getParentFile().mkdirs();
//                byte[] imgBytes = Jsoup.connect(datum.get(2))
//                        .timeout(timeout)
//                        .header("Referer", "https://readmanganato.com/")
//                        .ignoreContentType(true).execute().bodyAsBytes();
//                FileOutputStream out = new FileOutputStream(file);
//                out.write(imgBytes);
//                out.close();
                String[] whichCurl = { "which", "curl" };
                String curlPath = new BufferedReader(new InputStreamReader(
                        Runtime.getRuntime().exec(whichCurl).getInputStream()
                )).readLine();
                if (curlPath.equals("")) {
                    System.out.println("Please install curl (https://curl.se/)");
                    System.out.println("in order to download manga pages");
                    System.exit(0);
                }
                String[] curl = {
                        curlPath,
                        "--referer", "https://readmanganato.com/",
                        "--create-dirs",
                        "--output", fileLocation,
                        datum.get(2)
                };
                processes.add(runtime.exec(curl));
            }
        }

        while (hasAlive(processes))
            Thread.sleep(60*1000);

        if (!isWindows) {
            String[] whichImg2pdf = { "which", "img2pdf" };
            String img2pdfPath = new BufferedReader(new InputStreamReader(
                    Runtime.getRuntime().exec(whichImg2pdf).getInputStream()
            )).readLine();
            if (img2pdfPath.equals("")) {
                System.out.println("Please install img2pdf (https://github.com/josch/img2pdf)");
                System.out.println("in order to convert chapter images to PDFs");
                System.exit(0);
            }
            for (int i = 0; i < chapters.size(); i++) {
                String chapterName = "["+(i+1)+"] "+cleanse(chapters.get(i).text());
                System.out.println("Converting chapter \""+chapterName+"\" to PDF");
                String newDir = dir.replace("/img/", "/pdf/");
                Files.createDirectories(Paths.get(newDir));
                String[] img2pdf = {
                        "/bin/bash", "-c",
                        img2pdfPath+
                        " \""+dir+chapterName+"/\"*.jpg "+
                        "--output "+
                        "\""+newDir+chapterName+".pdf\""
                };
                runtime.exec(img2pdf);
            }
        }

        System.out.println("Done");
    }

    static boolean hasAlive(List<Process> processes) {
        for (Process process : processes)
            if (process.isAlive())
                return true;
        return false;
    }

    static Document get(String url) throws IOException {
        return Jsoup.connect(url).timeout(timeout).get();
    }

    // remove invalid filename / folder name characters in Windows
    static String cleanse(String s) {
        return s.replaceAll("[<>:\"/\\|?*]", "");
    }
}
