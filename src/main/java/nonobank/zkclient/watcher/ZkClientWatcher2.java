package nonobank.zkclient.watcher;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

public class ZkClientWatcher2 {

	/** zookeeper地址 */
	static final String CONNECT_ADDR = "127.0.0.1:2181,127.0.0.1:2183,127.0.0.1:2182";
	/** session超时时间 */
	static final int SESSION_OUTTIME = 5000;// ms

	public static void main(String[] args) throws Exception {
		ZkClient zkc = new ZkClient(new ZkConnection(CONNECT_ADDR), 5000);

		zkc.createPersistent("/super", "1234");

		// 对父节点添加监听子节点变化,数据的变化，data删除，修改
		zkc.subscribeDataChanges("/super", new IZkDataListener() {
			
			public void handleDataDeleted(String path) throws Exception {
				System.out.println("删除的节点为:" + path);
			}

			public void handleDataChange(String path, Object data) throws Exception {
				System.out.println("变更的节点为:" + path + ", 变更内容为:" + data);
			}
		});

		Thread.sleep(3000);
		//节点数据变更
		zkc.writeData("/super", "456", -1);
		Thread.sleep(1000);

		zkc.delete("/super");
		Thread.sleep(Integer.MAX_VALUE);

	}
}
