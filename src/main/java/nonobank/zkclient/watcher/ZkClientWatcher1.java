package nonobank.zkclient.watcher;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

public class ZkClientWatcher1 {

	/** zookeeper地址 */
	static final String CONNECT_ADDR = "127.0.0.1:2181,127.0.0.1:2183,127.0.0.1:2182";
	/** session超时时间 */
	static final int SESSION_OUTTIME = 5000;//ms 
	
	
	public static void main(String[] args) throws Exception {
		ZkClient zkc = new ZkClient(new ZkConnection(CONNECT_ADDR), 5000);
		
		//对父节点添加监听子节点变化,订阅子节点的变化，类似mq的lisener，自己节点删除，新增子节点，减少子节点，删除子节点，带回来了节点的变化数据，必须要重复注册
		zkc.subscribeChildChanges("/super", new IZkChildListener() {
			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				System.out.println("回调---parentPath: " + parentPath);
				System.out.println("回调--currentChilds: " + currentChilds);
			}
		});
		zkc.createPersistent("/super");
		zkc.deleteRecursive("/super");
		Thread.sleep(3000);
		
//		zkc.createPersistent("/super");
//		Thread.sleep(1000);
//		
//		zkc.createPersistent("/super" + "/" + "c1", "c1内容");
//		Thread.sleep(1000);
//		
//		zkc.createPersistent("/super" + "/" + "c2", "c2内容");
//		Thread.sleep(1000);		
//		
//		zkc.delete("/super/c2");
//		Thread.sleep(1000);	
//		
//		zkc.deleteRecursive("/super");
		Thread.sleep(Integer.MAX_VALUE);
		
		
	}
}
