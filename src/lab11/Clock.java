package lab11;

import java.time.LocalTime;

public class Clock extends Thread {
    public static void main(String[] args) {
        new Clock().start();
    }

    @Override
    public void run() {
        while (true) {
            LocalTime time = LocalTime.now();
            System.out.printf("\r%02d:%02d:%02d",
                    time.getHour(),
                    time.getMinute(),
                    time.getSecond());
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
