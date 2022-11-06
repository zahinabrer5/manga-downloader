# manga-downloader

Scrapes the site https://manganato.com/ to find and download manga.

Uses jsoup 1.15.3

## Dependencies

Java 8, download from https://www.java.com/en/download/manual.jsp.

## Running

1. Download the latest `.jar` from [Releases](https://github.com/zahinabrer5/manga-downloader/releases).
2. Run the following command in the directory it was downloaded to:
```
java -jar manga-downloader-x.jar
```
... where `x` is the version string.

## Notes
Manga will be downloaded to the `~/manga` directory on Linux/Mac systems. On Windows, it will (probably) be downloaded to `C:\Users\your_username\manga`.
