package info.xiaomo.core.mq;

import javax.jms.Connection;
import info.xiaomo.core.common.utils.FileUtil;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * MQ服务
 *
 *
 * 2017年7月28日 下午1:31:13
 */
public abstract class MQService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MQService.class);
	protected final com.jzy.game.engine.mq.MQConfig mqConfig; // 配置
	protected ActiveMQConnectionFactory activeMQConnectionFactory; // 连接工厂
	protected Connection connection; // 连接

	public MQService(com.jzy.game.engine.mq.MQConfig mqConfig) {
		this.mqConfig = mqConfig;
		activeMQConnectionFactory = new ActiveMQConnectionFactory(mqConfig.getMqConnectionUrl());
	}

	public MQService(String configPath) {
		mqConfig = FileUtil.getConfigXML(configPath, "mqConfig.xml", com.jzy.game.engine.mq.MQConfig.class);
		if (mqConfig == null) {
			throw new RuntimeException(String.format("配置文件%s/mqConfig.xml未配置", configPath));
		}
		activeMQConnectionFactory = new ActiveMQConnectionFactory(mqConfig.getMqConnectionUrl());
	}

	/**
	 * 获取连接
	 *
	 * @return
	 *
	 * <p>
	 * 2017年7月28日 下午1:38:28
	 */
	public final Connection getConnection() {
		try {
			if (connection == null) {
				connection = activeMQConnectionFactory.createConnection(mqConfig.getUser(), mqConfig.getPassword());
			}
		} catch (Exception e) {
			LOGGER.error("MQ Connection", e);
			connection = null;
		}
		return connection;
	}

	/**
	 * 关闭连接
	 *
	 *
	 * <p>
	 * 2017年7月28日 下午1:38:20
	 */
	public void closeConnection() {
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception e) {
				LOGGER.error("closeConnection", e);
			}
			connection = null;
		}
	}
}
