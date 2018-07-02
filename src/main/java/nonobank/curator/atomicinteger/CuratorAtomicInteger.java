package nonobank.curator.atomicinteger;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;

/**
* @ClassName: CuratorAtomicInteger  
* @Description: 分布式自增id
* @author yaolei  
* @date 2018年6月23日  
*
 */
public class CuratorAtomicInteger {

	/** zookeeper地址 */
	static final String CONNECT_ADDR = "127.0.0.1:2181,127.0.0.1:2183,127.0.0.1:2182";
	/** session超时时间 */
	static final int SESSION_OUTTIME = 5000;//ms 
	
	public static void main(String[] args) throws Exception {
		
		//1 重试策略：初试时间为1s 重试10次
		//统计信息
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
		//2 通过工厂创建连接
		CuratorFramework cf = CuratorFrameworkFactory.builder()
					.connectString(CONNECT_ADDR)
					.sessionTimeoutMs(SESSION_OUTTIME)
					.retryPolicy(retryPolicy)
					.build();
		//3 开启连接
		cf.start();
		//cf.delete().forPath("/super");

		//4 使用DistributedAtomicInteger
		DistributedAtomicInteger atomicIntger = 
				//RetryNTimes；重试间隔
				new DistributedAtomicInteger(cf, "/super", new RetryNTimes(3, 1000));
		
		AtomicValue<Integer> value = atomicIntger.add(1);
		System.out.println(value.succeeded());
		System.out.println(value.postValue());	//最新值
		System.out.println(value.preValue());	//原始值
		
	}
}
