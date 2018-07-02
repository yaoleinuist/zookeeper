package nonobank.curator.barrier;

import java.util.Random;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.apache.curator.retry.ExponentialBackoffRetry;
/**
* @ClassName: CuratorBarrier1  
* @Description: 同时开始，同时结束 
* @author yaolei  
* @date 2018年6月23日  
*
 */
public class CuratorBarrier1 {

	/** zookeeper地址 */
	static final String CONNECT_ADDR = "127.0.0.1:2181,127.0.0.1:2183,127.0.0.1:2182";
	/** session超时时间 */
	static final int SESSION_OUTTIME = 5000;//ms 
	
	public static void main(String[] args) throws Exception {
		
	    //场景。。。。
		
		for(int i = 0; i < 5; i++){
			new Thread(new Runnable() {
				public void run() {
					try {
						RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
						CuratorFramework cf = CuratorFrameworkFactory.builder()
									.connectString(CONNECT_ADDR)
									.retryPolicy(retryPolicy)
									.build();
						cf.start();
						//5次
						DistributedDoubleBarrier barrier = new DistributedDoubleBarrier(cf, "/super", 5);
						Thread.sleep(1000 * (new Random()).nextInt(3)); 
						System.out.println(Thread.currentThread().getName() + "已经准备");
						// 当5个人同时到此，double barrier,同时开始，开始等待
						barrier.enter();
						System.out.println("同时开始运行...");
						Thread.sleep(1000 * (new Random()).nextInt(3));
						System.out.println(Thread.currentThread().getName() + "运行完毕");
						// double barrier,同时结束
						barrier.leave();
						System.out.println("同时退出运行...");
						

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			},"t" + i).start();
		}

		
		
	}
}
