package nonobank.curator.barrier;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.framework.recipes.queue.DistributedQueue;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
/**
* @ClassName: CuratorBarrier2  
* @Description: TODO(这里用一句话描述这个类的作用)  
* @author yaolei  
* @date 2018年6月23日  
*
 */
public class CuratorBarrier2 {

	/** zookeeper地址 */
	static final String CONNECT_ADDR = "127.0.0.1:2181,127.0.0.1:2183,127.0.0.1:2182";
	/** session超时时间 */
	static final int SESSION_OUTTIME = 5000;//ms 
	
	static DistributedBarrier barrier = null;
	
	public static void main(String[] args) throws Exception {
		
		//场景、、、、
		
		for(int i = 0; i < 5; i++){
			new Thread(new Runnable() {
				public void run() {
					try {
						RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
						CuratorFramework cf = CuratorFrameworkFactory.builder()
									.connectString(CONNECT_ADDR)
									.sessionTimeoutMs(SESSION_OUTTIME)
									.retryPolicy(retryPolicy)
									.build();
						cf.start();
						barrier = new DistributedBarrier(cf, "/super");
						System.out.println(Thread.currentThread().getName() + "设置barrier!");
						barrier.setBarrier();	//设置
						barrier.waitOnBarrier();	//等待
						System.out.println("---------开始执行程序----------");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			},"t" + i).start();
		}

		Thread.sleep(5000);
		barrier.removeBarrier();	//释放
		
		
	}
}
