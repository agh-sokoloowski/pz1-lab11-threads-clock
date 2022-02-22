# Laboratorium 11 - Wątki: animacja, pobieranie plików

Wątek to program sekwencyjny, który może być wykonywany współbieżnie z innymi wątkami.

- Wykonywany kod umieszczamy w metodzie `run()`
- Wątki uruchamiamy wywołując metodę `start()`
- Wątki kończą działanie po wyjściu z metody `run()`

:heavy_exclamation_mark: Wywołanie `run()` wykona funkcję, ale nie jako osobny wątek.

## 11.1 Zegar - wątek Clock

Zadeklaruj klasę `Clock`

```java
public class Clock extends Thread{
    @Override
    public void run() {}
 
    public static void main(String[] args) {
        new Clock().start();
    }
}
```

### Kod wykonywany przez wątek

Następnie w metodzie `run()`

- dodaj pętlę nieskończoną
- w pętli odczytuj i drukuj bieżący czas

```java
LocalTime time = LocalTime.now();
System.out.printf("%02d:%02d:%02d\n",
    time.getHour(),
    time.getMinute(),
    time.getSecond());
```

### Usypianie wątku
Prawdopodobnie ten sam czas drukuje się wielokrotnie. Uśpij wątek na jedną sekundę (1000 milisekund) wprowadzając wywołanie metody `sleep()`

## 11.2 Zegar z GUI

Zaimplementujemy zegar analogowy wyświetlający (i przesuwający wskazówki).

- Zegar będzie rysowany wewnątrz klasy `ClockWithGui` dziedziczącej po `JPanel`.
- W funkcji `main()` utworzona zostanie ramka, dodany do niej panel, itd

```java
public class ClockWithGui extends JPanel {
    LocalTime time = LocalTime.now();

    public static void main(String[] args) {
        JFrame frame = new JFrame("Clock");
        frame.setContentPane(new ClockWithGui());
        frame.setSize(700, 700);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setVisible(true);
    }
}
```

### Rysujemy tarczę

Kod umieszczamy w `paintComponent()`

Poniżej przykład rysowania cyfr na tarczy.

- tworzymy macierz przekształcenia afinicznego (powinowactwa)
- definiujemy obrót o wielokrotność 360/12 stopni
- wyznaczamy obraz punktu w przekształceniu.
- Ten punkt ma współrzędne (0,-120). Wartość 120 to promień, znak minus bo współrzędne y rosną w dół. Lokalizacja cyfr nie jest idealna, warto odjąć od x (przed przekształceniem) szerokość tekstu. Jeszcze lepiej zrealizowana metoda powinna odczytać wymiary tekstu dla danej czcionki.

```java
public void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    g2d.translate(getWidth() / 2, getHeight() / 2);

    for (int i = 1; i < 13; i++) {
        AffineTransform at = new AffineTransform();
        at.rotate(2 * Math.PI / 12 * i);
        Point2D src = new Point2D.Float(0, -120);
        Point2D trg = new Point2D.Float();
        at.transform(src, trg);
        g2d.drawString(Integer.toString(i), (int) trg.getX(), (int) trg.getY());
    }
}
```

:heavy_exclamation_mark: Gdyby ktoś potrzebował inspiracji - można zajrzeć na kod rysujący tarczę zegara w [JavaScript](https://www.w3schools.com/graphics/canvas_clock.asp). Kontekst graficzny `ctx` w JavaScript jest analogiem `Grapics2D` w bibliotece Swing. Transformacje afiniczne mają podobną postać...

### Rysujemy wskazówki

Tak rysujemy jedną ze wskazówek (godzinową)...

```java
AffineTransform saveAT = g2d.getTransform();
g2d.rotate(time.getHour()%12*2*Math.PI/12);
g2d.drawLine(0,0,0,-100);
g2d.setTransform(saveAT);
```

Możesz zmienić kształt (np. narysować wielobok) lub użyć pogrubienia `g2d.setStroke(new BasicStroke(???, CAP_ROUND,JOIN_MITER))`

### Kreski na tarczy

Dorysuj samodzielnie...

### Animacja wskazówek

W klasie `ClockWithGui` zadeklaruj klasę wewnętrzną będącą wątkiem.

```java
class ClockThread extends Thread {
    @Override
    public void run() {
        for (;;) {
            time = LocalTime.now();
            System.out.printf("%02d:%02d:%02d\n",
                time.getHour(),
                time.getMinute(),
                time.getSecond());

            //sleep(1000);
            repaint();
        }
    }
}
```

- Dlaczego w `ClockThread` możliwy jest dostęp do atrybutu `time`?
- Za co odpowiada metoda `repaint()`? Jest to metoda wątku czy JPanel?

## 11.3 Pobieranie plików

Celem przykładu jest porównanie czasów sekwencyjnego i współbieżnego pobierania plików.

Całość zamkniemy w jednej klasie/pliku `DownaloadExample`. Ale oczywiście można kod rozdzielić.

```java
public class DownloadExample {
 
    // lista plików do pobrania
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
}
```

### Downloader

Zadeklaruj zagnieżdżoną klasę `Downolader`. [Jeżeli wolisz, możesz utworzyć klasę zewnętrzną]

- Klasa ma metodę `run()` i implementuje interfejs `Runnable` można wiec ją uruchomić jako wątek lub bezpośrednio
- Kopiowanie pliku - zapoznaj się z *try with resource* – [wykład o wyjątkach (pod koniec)](https://home.agh.edu.pl/~pszwed/wiki/lib/exe/fetch.php?media=java:w7-java-wyjatki.pdf)

```java
static class Downloader implements Runnable {
    private final String url;

    Downloader(String url){
        this.url = url;
    }

    public void run(){
        String fileName = //nazwa pliku z url

        try (InputStream in = new URL(url).openStream(); FileOutputStream out = new FileOutputStream(fileName)) {
            for (;;) {
                // czytaj znak z in
                // jeśli <0 break
                //zapisz znak do out
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Done:" + fileName);
    }
}
```

### Pobieranie sekwencyjne

Zaimplementuj, uruchom, przetestuj metodę sequentialDownload

```java
static void sequentialDownload() {
    double t1 = System.nanoTime() / 1e6;
    for (String url : toDownload){
        new Downloader(url).run();  // uwaga tu jest run()
    }
    double t2 = System.nanoTime() / 1e6;
    System.out.printf(Locale.US, "t2-t1=%f\n", t2 - t1);
}
```

### Pobieranie współbieżne v.1

```java
static void concurrentDownload() {
    double t1 = System.nanoTime() / 1e6;
    for (String url : toDownload) {
        // uruchom Downloader jako wątek... czyli wywołaj start()
    }
    double t2 = System.nanoTime() / 1e6;
    System.out.printf(Locale.US, "t2-t1=%f\n", t2 - t1);
}
```

Czy podawany jest poprawny czas? Jeśli nie to dlaczego?

### Pobieranie współbieżne v.2

Zrealizuj rozwiązanie polegające na zliczaniu pobranych plików

- W klasie `DownloadExample` zadeklaruj statyczną zmienną `static int count = 0;`
- W klasie `Downloader` po zakończeniu zwiększ `count` o jeden
- Zaimplemntuj funkcję `concurrentDownload2` z sekwencją oczekiwania na zakończenie pobierania plików

```java
while (count != toDownload.length) {
    // wait...
    Thread.yield();
}
```

### Pobieranie współbieżne v.2.5

Zmień typ danych `count` na `AtomicInteger` i podmień wszystkie operacje. To bezpieczniejsze (i zalecane) rozwiązanie.

### Pobieranie współbieżne v.3

Zamiast aktywnego oczekiwania wątku na zakończenie operacji wprowadzimy mechanizm synchronizacji - [semafor](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Semaphore.html).

Semafor to zmienna całkowita (licznik), na której można wykonać dwie atomowe (niepodzielne) operacje:

- `release()` – zwiększa licznik o 1
- `acquire(int cnt)`
  - zawiesza wątek w oczekiwaniu, aż licznik semafora będzie większy lub równy `cnt`,
  - następnie zmniejsza licznik o `cnt` i odblokowuje oczekujący wątek

Dodaj semfor do klasy `DownloadExample`

```java
static Semaphore sem = new Semaphore(0);
```
 
Po zakończeniu pobierania pliku w `Downolader` wywołaj `sem.release` (zwiększa licznik w semaforze o 1)

W funkcji `concurrentDownload3()` wywołaj `sem.acquire()` podając odpowiednią wartość. Wątek funkcji `main()` będzie czekał na dokończenie wywołania `sem.acquire()`, które nastąpi, kiedy wszystkie wątki `Downloader` zakończą działanie. Następnie odczytaj rzeczywiste czasy pobierania i porównaj z czasem sekwencyjnym.
