package diningPhilosophers;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class DiningPhilosophers {
    static final int N = 5;
    static final int THINKING = 0;
    static final int HUNGRY = 1;
    static final int EATING = 2;
    
    static int[] state = new int[N];
    static Semaphore mutex = new Semaphore(1);
    static Semaphore[] s = new Semaphore[N];
    
    static AtomicInteger[] eatCount = new AtomicInteger[N];
    static volatile boolean running = true;
    
    static {
        for (int i = 0; i < N; i++) {
            s[i] = new Semaphore(0, true);
            eatCount[i] = new AtomicInteger(0);
            state[i] = THINKING;
        }
    }
    
    static int LEFT(int i) {
        return (i + N - 1) % N;
    }
    
    static int RIGHT(int i) {
        return (i + 1) % N;
    }
    
    static class Philosopher implements Runnable {
        private int i;
        
        public Philosopher(int i) {
            this.i = i;
        }
        
        @Override
        public void run() {
            while (running) {
                try {
                    think();
                    take_forks(i);
                    eat();
                    put_forks(i);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                
                    break;
                }
            }
        }
        
        private void think() throws InterruptedException {
            if (!running) return;
            System.out.println("Filosofo " + i + " esta pensando");
            Thread.sleep((long) (Math.random() * 1000));
        }
        
        private void eat() throws InterruptedException {
            if (!running) return;
            
        
        
        
            if (state[LEFT(i)] == EATING || state[RIGHT(i)] == EATING) {
                System.err.printf("VIOLAÇÃO: Filósofo %d comendo com vizinho comendo! LEFT %d=%d, RIGHT %d=%d\n", 
                    i, LEFT(i), state[LEFT(i)], RIGHT(i), state[RIGHT(i)]);
            }
            System.out.printf("Filosofo %d esta comendo (garfos %d e %d)\n", i, i, LEFT(i));
            
            eatCount[i].incrementAndGet();
            Thread.sleep((long) (Math.random() * 1000));
        }
    }
    
    static void take_forks(int i) throws InterruptedException {
        if (!running) return;
        mutex.acquire();
        try {
            state[i] = HUNGRY;
            test(i);
        } finally {
            mutex.release();
        }
        s[i].acquire();
    }
    
    static void put_forks(int i) throws InterruptedException {
        if (!running) return;
        mutex.acquire();
        try {
            state[i] = THINKING;
            test(LEFT(i));
            test(RIGHT(i));
        } finally {
            mutex.release();
        }
    }
    

    static void test(int i) {
        if (state[i] == HUNGRY && 
            state[LEFT(i)] != EATING && 
            state[RIGHT(i)] != EATING) {
            
            state[i] = EATING;
            s[i].release();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Iniciando jantar dos filosofos...");
        System.out.println("Executando por 30 segundos...");
        
        Thread[] philosophers = new Thread[N];
        for (int i = 0; i < N; i++) {
            philosophers[i] = new Thread(new Philosopher(i));
            philosophers[i].start();
        }
        
        Thread stopper = new Thread(() -> {
            try {
                Thread.sleep(30000);
                running = false;
                System.out.println("\n--- Parando execução após 30 segundos ---");
                
                for (Thread philosopher : philosophers) {
                    philosopher.interrupt();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        stopper.start();
        
        try {
            stopper.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
    
        for (int i = 0; i < N; i++) {
            try {
                philosophers[i].join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        showStatistics();
    }
    
    private static void showStatistics() {
        System.out.println("\n=== ESTATÍSTICAS FINAIS ===");
        System.out.println("Tempo de execução: 30 segundos");
        System.out.println("\nNúmero de vezes que cada filósofo comeu:");
        
        int totalMeals = 0;
        for (int i = 0; i < N; i++) {
            int meals = eatCount[i].get();
            totalMeals += meals;
            System.out.printf("Filósofo %d: %d vezes\n", i, meals);
        }
        
        System.out.printf("\nTotal de refeições: %d\n", totalMeals);
        System.out.printf("Média de refeições por filósofo: %.2f\n", (double) totalMeals / N);
        
        int maxMeals = 0;
        int minMeals = Integer.MAX_VALUE;
        int philosopherMax = -1;
        int philosopherMin = -1;
        
        for (int i = 0; i < N; i++) {
            int meals = eatCount[i].get();
            if (meals > maxMeals) {
                maxMeals = meals;
                philosopherMax = i;
            }
            if (meals < minMeals) {
                minMeals = meals;
                philosopherMin = i;
            }
        }
        
        System.out.printf("Filósofo que mais comeu: %d (%d vezes)\n", philosopherMax, maxMeals);
        System.out.printf("Filósofo que menos comeu: %d (%d vezes)\n", philosopherMin, minMeals);
        System.out.printf("Diferença: %d vezes\n", maxMeals - minMeals);
    }
}
