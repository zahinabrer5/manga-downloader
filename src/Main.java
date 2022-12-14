import java.io.*;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);

        String url;
        Document doc;
        Elements mangas;
        do {
            System.out.print("Search manga: ");
            String search = sc.nextLine().replaceAll(" ", "_");
            url = "https://manganato.com/search/story/"+search;
            doc = Jsoup.connect(url).timeout(10*1000*60).get();
            mangas = doc.select("a.a-h.text-nowrap.item-title");
        } while (mangas.isEmpty());

        if (doc.selectFirst("div.panel-page-number") != null) {
            int last = Integer.parseInt(doc.selectFirst("a.page-blue.page-last")
                    .text().replaceAll("[^0-9]", ""));
            for (int i = 2; i <= last; i++) {
                doc = Jsoup.connect(url+"?page="+i).timeout(10*1000*60).get();
                mangas.addAll(doc.select("a.a-h.text-nowrap.item-title"));
            }
        }

        for (int i = 0; i < mangas.size(); i++) {
            System.out.printf("[%d] %s%n", i+1, mangas.get(i).text());
        }
        int selection;
        do {
            System.out.printf("Please select a manga [1-%d]: ", mangas.size());
            selection = sc.nextInt();
        } while (selection < 1 || selection > mangas.size());
        Element manga = mangas.get(selection-1);

        System.out.println("Preparing to download manga: "+manga.text());

        url = manga.attr("href");
        doc = Jsoup.connect(url).timeout(10*1000*60).get();
        Elements chapters = doc.select("a.chapter-name.text-nowrap");
        Collections.reverse(chapters);

        List<List<String>> data = new ArrayList<>();

        for (int i = 0; i < chapters.size(); i++) {
            url = chapters.get(i).attr("href");
            doc = Jsoup.connect(url).timeout(10*1000*60).get();
            Element container = doc.selectFirst("div.container-chapter-reader");
            Elements pages = container.children().select("img");
            for (int j = 0; j < pages.size(); j++) {
                String img = pages.get(j).attr("src");
                System.out.println("Targeting image: "+img);
                data.add(Arrays.asList(
                        String.format("[%d] %s", i+1, chapters.get(i).text()),
                        String.format("%03d", j+1),
                        img
                ));
            }
        }

        String home = "HOME";
        if (System.getProperty("os.name").startsWith("Windows")) {
            home = "USERPROFILE";
        }
        String dir = System.getenv(home)+"/manga/"+manga.text()+"/";
        for (List<String> datum : data) {
            String fileLocation = dir+datum.get(0)+"/page"+datum.get(1)+".jpg";
            System.out.println("Downloading to file (if not exists): "+fileLocation);
            File file = new File(fileLocation);
            if (!file.isFile()) {
                file.getParentFile().mkdirs();
                byte[] imgBytes = Jsoup.connect(datum.get(2))
                        .timeout(10*1000*60)
                        .header("Referer", "https://readmanganato.com/")
                        .ignoreContentType(true).execute().bodyAsBytes();
                FileOutputStream out = new FileOutputStream(file);
                out.write(imgBytes);
                out.close();
            }
        }

        System.out.println("Done");
    }
}
