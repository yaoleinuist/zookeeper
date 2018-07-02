package nonobank.zkclient.base;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

//没curator强大，但大部分还都是zkclient
public class ZkClientBase {

	/** zookeeper地址 */
	static final String CONNECT_ADDR = "127.0.0.1:2181,127.0.0.1:2183,127.0.0.1:2182";
	/** session超时时间 */
	static final int SESSION_OUTTIME = 5000;// ms

	public static void main(String[] args) throws Exception {
		ZkClient zkc = new ZkClient(new ZkConnection(CONNECT_ADDR), 5000);
		// 1. create and delete方法
		zkc.createEphemeral("/temp");
		// 只能指定key，但value不可以递归
		zkc.createPersistent("/super/c1", true);
		// Thread.sleep(10000);
		zkc.delete("/temp");
		zkc.deleteRecursive("/super");
        String s = zkc.createPersistentSequential("/temp", "测试");
        System.out.println(s);
		// 2. 设置path和data 并且读取子节点和每个节点的内容,只能设置参数，value不可以递归设置
		zkc.createPersistent("/super", "1234");
		zkc.createPersistent("/super/c1", "c1内容");
		zkc.createPersistent("/super/c2", "c2内容");
		//zkclient 没有watch，增删改查，不需要在增删改查节点的过程，解耦。
		List<String> list = zkc.getChildren("/super");
		for (String p : list) {
			System.out.println(p);
			String rp = "/super/" + p;
			String data = zkc.readData(rp);
			System.out.println("节点为：" + rp + "，内容为: " + data);
		}

		// 3. 更新和判断节点是否存在
		// zkc.writeData("/super/c1", "新内容");
		// System.out.println(zkc.readData("/super/c1"));
		// System.out.println(zkc.exists("/super/c1"));

		// 4.递归删除/super内容
		// zkc.deleteRecursive("/super");
	}
}
