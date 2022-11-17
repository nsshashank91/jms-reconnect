package com.shash.jms;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class AsyncReceiver implements MessageListener, ExceptionListener {
    QueueConnection queueConn;
    QueueSession queueSession;
    QueueReceiver queueReceiver;
    boolean connected;
    HikariConfig config;
    HikariDataSource ds;
    private static final Logger logger = LoggerFactory.getLogger(AsyncReceiver.class);

    public AsyncReceiver() {
        System.out.println("inside const");
        config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://localhost:3306/test");
        config.setUsername("root");
        config.setPassword("password");
        config.setMinimumIdle(10);
        config.setMaximumPoolSize(15);
        config.setIdleTimeout(600000);
        config.setKeepaliveTime(600000);
        config.setMaxLifetime(180000);
        config.setConnectionTimeout(2000);
        config.setInitializationFailTimeout(1000);
        config.setConnectionInitSql("select 1 from dual");
        config.setValidationTimeout(2000);
        ds = new HikariDataSource(config);

    }

    public void connectToJMS() {
        try {
            logger.info("connecting to jms....");

            Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            env.put(Context.PROVIDER_URL, "tcp://localhost:61616");
            env.put("queue.queueSampleQueue", "shashqueue");

            // get the initial context
            InitialContext ctx = new InitialContext(env);
            // lookup the queue object
            Queue queue = (Queue) ctx.lookup("queueSampleQueue");
            // lookup the queue connection factory
            QueueConnectionFactory connFactory = (QueueConnectionFactory) ctx.lookup("QueueConnectionFactory");

            while (!connected) {
                try {
                    // create a queue connection
                    queueConn = connFactory.createQueueConnection();

                    // create a queue session
                    queueSession = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

                    // create a queue receiver
                    queueReceiver = queueSession.createReceiver(queue);

                    // set an asynchronous message listener

                    queueReceiver.setMessageListener(this);

                    // set an asynchronous exception listener on the connection
                    queueConn.setExceptionListener(this);

                    // start the connection
                    queueConn.start();
                    System.out.println("JMS connected ");
                    connected = true;


                } catch (JMSException e) {
                    cleanUpResources();
                    connected = false;
                    System.out.println("JMS exception handled in receiver " + e.getMessage());
                    System.out.println("Waiting 10s to reconnect");
                    Thread.sleep(10000L);
                }
            }

        } catch (Exception e) {
            System.out.println("Handling main exception");
        }


    }

    private void cleanUpResources() {
        System.out.println("cleaning up resources");
        if (queueReceiver != null) {
            try {
                queueReceiver.close();
            } catch (JMSException ignore) {

            }
        }
        if (queueSession != null) {
            try {
                queueSession.close();
            } catch (JMSException ignore) {
                System.out.println("exception while closing session " + ignore.getMessage());
            }
        }
        if (queueConn != null) {
            try {
                queueConn.close();
            } catch (JMSException ignore) {

            }
        }
        System.out.println("cleaned up resources");

    }

    public Connection getConnection(){
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            AsyncReceiver asyncReceiver = new AsyncReceiver();
            logger.info("starting jms");
            asyncReceiver.connectToJMS();
           /* while(true){
                asyncReceiver.recordCount();
                Thread.sleep(5000);
            }*/
        } catch (Exception e) {
            logger.info("main exception handled");

        }
        logger.info("started jms");

    }

    /**
     * This method is called asynchronously by JMS when a message arrives
     * at the queue. Client applications must not throw any exceptions in
     * the onMessage method.
     *
     * @param message A JMS message.
     */
    public void onMessage(Message message) {
        TextMessage msg = (TextMessage) message;
        try {
            System.out.println("received: " + msg.getText());
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method is called asynchronously by JMS when some error occurs.
     * When using an asynchronous message listener it is recommended to use
     * an exception listener also since JMS have no way to report errors
     * otherwise.
     *
     * @param exception A JMS exception.
     */
    public void onException(JMSException exception) {
        System.out.println("Exception handled in handler" + exception.getMessage());

    }

    public void recordCount(){
        Connection con = null;
        ResultSet rs = null;
        Statement stmt = null;
        try{
            con = getConnection();
            stmt = con.createStatement();
            String query = "select count(*) as total from test.employee_table";
            rs = stmt.executeQuery(query);
            rs.next();
            System.out.println(rs.getInt("total"));
        }catch (Exception e){
            System.out.println("exception in record count");
        }finally {
            if(rs!=null){
                try {
                    rs.close();
                } catch (SQLException ig) {

                }
            }
            if(stmt!=null){
                try {
                    stmt.close();
                } catch (SQLException ig) {

                }
            }
            if(con!=null){
                try {
                    con.close();
                } catch (SQLException ig) {

                }
            }
        }
    }
}