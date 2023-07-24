# manga-downloader

Scrapes the site https://manganato.com/ to find and download manga.

Uses jsoup 1.16.1

## Dependencies

- Java 8, download from https://www.java.com/en/download/manual.jsp.
- curl for downloading the manga pages as images
- img2pdf if you want to convert the pages into a pdf representing a chapter (doesn't work on Windows, as of right now)

## Running

1. Download the latest `.jar` from [Releases](https://github.com/zahinabrer5/manga-downloader/releases).
2. Run the following command in the directory it was downloaded to:
```
java -jar manga-downloader-x.jar
```
... where `x` is the version string.

## Notes
Manga will be downloaded to the `~/manga` directory on Linux/Mac systems. On Windows, it will (probably) be downloaded to `C:\Users\your_username\manga`.
