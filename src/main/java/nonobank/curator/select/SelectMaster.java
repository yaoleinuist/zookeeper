package nonobank.curator.select;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class SelectMaster {

	/** zookeeper地址 */
	static final String CONNECT_ADDR = "127.0.0.1:2181,127.0.0.1:2183,127.0.0.1:2182";
	/** session超时时间 */
	static final int SESSION_OUTTIME = 5000;// ms
	
	private String name;

	public SelectMaster(final String name) throws Exception {

		// 1 重试策略：初试时间为1s 重试10次
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
		// 2 通过工厂创建连接
		CuratorFramework cf = CuratorFrameworkFactory.builder().connectString(CONNECT_ADDR)
				.sessionTimeoutMs(SESSION_OUTTIME).retryPolicy(retryPolicy).build();

		// 3 建立连接
		cf.start();
		LeaderSelector selector = new LeaderSelector(cf, "/master", new LeaderSelectorListenerAdapter() {
			public void takeLeadership(CuratorFramework client) throws Exception {
				System.out.println(name);
				System.out.println("get master");
				Thread.sleep(3000);
				System.out.println("quit master");
			}
		});
		selector.autoRequeue();
		selector.start();
		Thread.sleep(Integer.MAX_VALUE);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
