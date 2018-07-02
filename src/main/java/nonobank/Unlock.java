package nonobank;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Unlock {
	
	public static void main(String[] args) {

		Lock lock = new ReentrantLock();
		CountDownLatch c = new CountDownLatch(3);
		Task1 task1 = new Task1(lock, c);
		Task2 task2 = new Task2(lock, c);
		Task3 task3 = new Task3(lock, c);

		Thread thread1 = new Thread(task1);
		Thread thread2 = new Thread(task2);
		Thread thread3 = new Thread(task3);
		thread1.setName("线程1");
		thread2.setName("线程2");
		thread3.setName("线程3");
		thread1.start();
		thread2.start();
		thread3.start();

	}

}

class Task1 implements Runnable {

	public Lock lock;
	CountDownLatch c;

	public Task1(Lock lock, CountDownLatch c) {
		this.lock = lock;
		this.c = c;
	}

	public void run() {
		System.out.println(Thread.currentThread().getName());
		lock.lock();
		System.out.println(Thread.currentThread().getName() + "-1-1");
		lock.lock();
		try {
			// c.await(10,TimeUnit.SECONDS);
			c.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(Thread.currentThread().getName() + "-1");
		lock.unlock();
		lock.unlock();
	}
}

class Task2 implements Runnable {

	public Lock lock;
	CountDownLatch c;

	public Task2(Lock lock, CountDownLatch c) {
		this.lock = lock;
		this.c = c;
	}

	public void run() {
		System.out.println(Thread.currentThread().getName());
		
		try {
			// c.await(10,TimeUnit.SECONDS);
			c.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		lock.lock();
		System.out.println(Thread.currentThread().getName() + "-2-2");
		lock.lock();
		System.out.println(Thread.currentThread().getName() + "-2");
		lock.unlock();
		lock.unlock();
	}
}

class Task3 implements Runnable {

	public Lock lock;
	CountDownLatch c;

	public Task3(Lock lock, CountDownLatch c) {
		this.lock = lock;
		this.c = c;
	}

	public void run() {
		System.out.println(Thread.currentThread().getName());
		lock.lock();
		System.out.println(Thread.currentThread().getName() + "-3-3");
		lock.lock();
		try {
			// c.await(100000,TimeUnit.SECONDS);
			c.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(Thread.currentThread().getName() + "-3");
		lock.unlock();
		lock.unlock();
	}
}
