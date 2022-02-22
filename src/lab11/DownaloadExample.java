package lab11;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class DownaloadExample {
    static AtomicInteger count = new AtomicInteger(0);
    static Semaphore sem = new Semaphore(0);

    static String[] toDownload = {
        "https://home.agh.edu.pl/~pszwed/wyklad-c/01-jezyk-c-intro.pdf",
        "https://home.agh.edu.pl/~pszwed/wyklad-c/02-jezyk-c-podstawy-skladni.pdf",
        "https://home.agh.edu.pl/~pszwed/wyklad-c/03-jezyk-c-instrukcje.pdf",
        "https://home.agh.edu.pl/~pszwed/wyklad-c/04-jezyk-c-funkcje.pdf",
        "https://home.agh.edu.pl/~pszwed/wyklad-c/05-jezyk-c-deklaracje-typy.pdf",
        "https://home.agh.edu.pl/~pszwed/wyklad-c/06-jezyk-c-wskazniki.pdf",
        "https://home.agh.edu.pl/~pszwed/wyklad-c/07-jezyk-c-operatory.pdf",
        "https://home.agh.edu.pl/~pszwed/wyklad-c/08-jezyk-c-lancuchy-znakow.pdf",
        "https://home.agh.edu.pl/~pszwed/wyklad-c/09-jezyk-c-struktura-programow.pdf",
        "https://home.agh.edu.pl/~pszwed/wyklad-c/10-jezyk-c-dynamiczna-alokacja-pamieci.pdf",
        "https://home.agh.edu.pl/~pszwed/wyklad-c/11-jezyk-c-biblioteka-we-wy.pdf",
        "https://home.agh.edu.pl/~pszwed/wyklad-c/preprocesor-make-funkcje-biblioteczne.pdf",
    };

    public static void main(String[] args) {
        sequentialDownload();
//        concurrentDownload();
        concurrentDownload2();
        concurrentDownload3();
    }

    static void sequentialDownload() {
        double t1 = System.nanoTime() / 1e6;
        for (String url : toDownload) {
            new Downloader(url).run();  // uwaga tu jest run()
        }
        double t2 = System.nanoTime() / 1e6;
        System.out.printf(Locale.US, "Sequential = %f\n", t2 - t1);
    }

    @SuppressWarnings("unused")
    static void concurrentDownload() {
        double t1 = System.nanoTime() / 1e6;
        for (String url : toDownload) {
            new Thread(new Downloader(url)).start();
        }
        double t2 = System.nanoTime() / 1e6;
        System.out.printf(Locale.US, "Concurrent 1 = %f\n", t2 - t1);
        // W rzeczywistości jest to czas samego uruchomienia nowych wątków,
        // a nie czasu pobierania wszystkich plików
    }

    static void concurrentDownload2() {
        count = new AtomicInteger(0);
        double t1 = System.nanoTime() / 1e6;
        for (String url : toDownload) {
            new Thread(new Downloader(url)).start();
        }
        while (count.get() != toDownload.length) {
            // wait...
            Thread.yield();
        }
        double t2 = System.nanoTime() / 1e6;
        System.out.printf(Locale.US, "Concurrent 2 = %f\n", t2 - t1);
    }

    static void concurrentDownload3() {
        sem = new Semaphore(0);

        double t1 = System.nanoTime() / 1e6;
        for (String url : toDownload) {
            new Thread(new Downloader(url)).start();
        }

        try {
            sem.acquire(toDownload.length);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        double t2 = System.nanoTime() / 1e6;
        System.out.printf(Locale.US, "Concurrent 3 = %f\n", t2 - t1);
    }

    @SuppressWarnings("ClassCanBeRecord")
    static class Downloader implements Runnable {
        private final String url;

        Downloader(String url) {
            this.url = url;
        }

        @SuppressWarnings("ConstantConditions")
        public void run() {
            String fileName = this.url.substring(this.url.lastIndexOf("/") + 1);
            boolean verbose = false;
            if (verbose) System.out.println("Downloading:" + fileName);

            try (InputStream in = new URL(url).openStream(); FileOutputStream out = new FileOutputStream("downloads/" + fileName)) {
                while (true) {
                    // czytaj znak z in
                    int ch = in.read();
                    // jeśli <0 break
                    if (ch < 0) {
                        break;
                    }
                    //zapisz znak do out
                    out.write(ch);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (verbose) System.out.println("Done:" + fileName);

            count.incrementAndGet();
            sem.release();
        }

    }
}
