package info.xiaomo.core.network.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
import info.xiaomo.core.common.utils.SysUtil;
import info.xiaomo.core.network.mina.code.HttpServerCodecImpl;
import info.xiaomo.core.network.mina.config.MinaServerConfig;
import info.xiaomo.core.network.mina.handler.HttpServerIoHandler;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http服务
 *
 *
 * @version $Id: $Id
 * @date 2017-03-31
 */
public class HttpServer implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(HttpServer.class);

	private final MinaServerConfig minaServerConfig;

	private final NioSocketAcceptor acceptor;

	private final HttpServerIoHandler ioHandler;

	protected boolean isRunning;    //通信是否在运行
	private OrderedThreadPoolExecutor threadpool;    //默认线程池

	/**
	 * <p>Constructor for HttpServer.</p>
	 */
	public HttpServer(MinaServerConfig minaServerConfig, HttpServerIoHandler ioHandler) {
		this.minaServerConfig = minaServerConfig;
		this.ioHandler = ioHandler;
		acceptor = new NioSocketAcceptor();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		synchronized (this) {
			if (!isRunning) {
				isRunning = true;
				new Thread(new BindServer()).start();
			}
		}
	}

	/**
	 * <p>stop.</p>
	 */
	public void stop() {
		synchronized (this) {
			if (!isRunning) {
				LOG.info("HttpServer " + minaServerConfig.getName() + "is already stoped.");
				return;
			}
			isRunning = false;
			try {
				if (threadpool != null) {
					threadpool.shutdown();
				}
				acceptor.unbind();
				acceptor.dispose();
				LOG.info("Server is stoped.");
			} catch (Exception ex) {
				LOG.error("", ex);
			}
		}
	}

	/**
	 * 绑定端口
	 *
	 *
	 * @date 2017-03-31
	 *
	 */
	private class BindServer implements Runnable {

		private final Logger LOG = LoggerFactory.getLogger(BindServer.class);

		@Override
		public void run() {
			DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
			chain.addLast("codec", new HttpServerCodecImpl());

			// // 线程队列池
			OrderedThreadPoolExecutor threadpool = new OrderedThreadPoolExecutor(minaServerConfig.getOrderedThreadPoolExecutorSize());
			chain.addLast("threadPool", new ExecutorFilter(threadpool));

			acceptor.setReuseAddress(minaServerConfig.isReuseAddress()); // 允许地址重用

			SocketSessionConfig sc = acceptor.getSessionConfig();
			sc.setReuseAddress(minaServerConfig.isReuseAddress());
			sc.setReceiveBufferSize(minaServerConfig.getMaxReadSize());
			sc.setSendBufferSize(minaServerConfig.getSendBufferSize());
			sc.setTcpNoDelay(minaServerConfig.isTcpNoDelay());
			sc.setSoLinger(minaServerConfig.getSoLinger());
			sc.setIdleTime(IdleStatus.READER_IDLE, minaServerConfig.getReaderIdleTime());
			sc.setIdleTime(IdleStatus.WRITER_IDLE, minaServerConfig.getWriterIdleTime());

			acceptor.setHandler(ioHandler);

			try {
				acceptor.bind(new InetSocketAddress(minaServerConfig.getHttpPort()));
				LOG.warn("已开始监听HTTP端口：{}", minaServerConfig.getHttpPort());
			} catch (IOException e) {
				SysUtil.exit(getClass(), e, "监听HTTP端口：{}已被占用", minaServerConfig.getHttpPort());
			}
		}
	}
}
