package nonobank;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OneToHundred implements Runnable{

	public volatile int i = 0;
	Lock l = new ReentrantLock();
	Condition c1 = l.newCondition();
	Condition c2 = l.newCondition();
	Condition c3 = l.newCondition();
	
	
	public static void main(String[] args) {
		
		OneToHundred runnable = new OneToHundred();
        Thread t1 = new Thread(runnable);
        Thread t2 = new Thread(runnable);
        Thread t3 = new Thread(runnable);
        t1.setName("1号线程");
        t2.setName("2号线程");
        t3.setName("3号线程");
        t1.start();
        t2.start();
        t3.start();
        
	}

	public void run() {
	  
      while(i<100) {
    	    l.lock();
    	    i++;
    	    
//		    synchronized (this) {
//		    this.notify();
//			i++;
//			System.out.println(Thread.currentThread().getName()+i);
//			try {
//				this.wait();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		 }
       }

}
