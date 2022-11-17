package com.shash.jms;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.MessageListener;
import javax.jms.JMSException;
import javax.jms.ExceptionListener;
import javax.jms.QueueSession;
import javax.jms.QueueReceiver;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;

public class AsyncReceiverTwo implements MessageListener, ExceptionListener
{
    QueueConnection queueConn;
    //AsyncReceiver asyncReceiver;
    //Properties env;
    //InitialContext ctx;
    //Queue queue;
    //QueueConnectionFactory connFactory;
    QueueSession queueSession;
    QueueReceiver queueReceiver;


    public void connectToJms() throws Exception {
           /* Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            env.put(Context.PROVIDER_URL, "tcp://localhost:61616");
            env.put("queue.queueSampleQueue","shashqueue");
            InitialContext ctx = new InitialContext(env);*/

            // lookup the queue object
            //Queue queue = (Queue) ctx.lookup("queueSampleQueue");

            // lookup the queue connection factory
            //QueueConnectionFactory connFactory = (QueueConnectionFactory) ctx.lookup("QueueConnectionFactory");

        setReceiver(this);


            // create a queue connection
            // queueConn = connFactory.createQueueConnection();

            // create a queue session
            // queueSession = queueConn.createQueueSession(false,Session.AUTO_ACKNOWLEDGE);

            // create a queue receiver
            //queueReceiver = queueSession.createReceiver(queue);


            System.out.println("connected jms channel");


    }

    /*public void reconnectToJMS() throws Exception{
        try {
            System.out.println("attempting reconnecting to jms channel");
            Thread.sleep(10000);

            // create a queue connection
            queueConn = connFactory.createQueueConnection();

            // create a queue session
            queueSession = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);



            // create a queue receiver
            queueReceiver = queueSession.createReceiver(queue);

            // set an asynchronous message listener

            queueReceiver.setMessageListener(asyncReceiver);

            // set an asynchronous exception listener on the connection
            //queueConn.setExceptionListener(asyncReceiver);

            // start the connection
            queueConn.start();
            System.out.println("reconnected to jms channel");
        }
        catch (JMSException e){
            System.out.println("reconnecting jms channel in 30s");
            asyncReceiver.reconnectToJMS();
        }

    }*/

    public static void main(String[] args) throws Exception {
        AsyncReceiverTwo receiver = new AsyncReceiverTwo();
        /*try {
            receiver.connectToJms();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            if( e instanceof JMSException){
                throw e;
            }
        }*/
        receiver.connectToJms();
    }
                                                                           
    /**
       This method is called asynchronously by JMS when a message arrives
       at the queue. Client applications must not throw any exceptions in
       the onMessage method.
       @param message A JMS message.
     */
    public void onMessage(Message message){
       TextMessage msg = (TextMessage) message;
        try {
            System.out.println("received: " + msg.getText());
        } catch (JMSException e) {
            System.out.println("Error receiving message");
        }
    }




    public boolean setReceiver(MessageListener listener) {
        try {
            Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            env.put(Context.PROVIDER_URL, "tcp://localhost:61616");
            env.put("queue.queueSampleQueue", "shashqueue");
            InitialContext ctx = new InitialContext(env);

            // lookup the queue object
            Queue queue = (Queue) ctx.lookup("queueSampleQueue");

            // lookup the queue connection factory
            QueueConnectionFactory connFactory = (QueueConnectionFactory) ctx.lookup("QueueConnectionFactory");

            // create a queue connection
             queueConn = connFactory.createQueueConnection();

            // create a queue session
             queueSession = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            // create a queue receiver
            //queueReceiver = queueSession.createReceiver(queue);
            // setReceiver(asyncReceiver);
            queueReceiver = queueSession.createReceiver(queue);

            queueConn.setExceptionListener(this);
            queueConn.start();
            System.out.println("Successfully connected ");
            return true;
        } catch (JMSException je) {
            System.out.println("Could not connect to the JMS Server, will retry in 30 seconds. "
                    + je.getMessage());
            try {
                Thread.sleep(10000L);
            } catch (InterruptedException e) {
                System.out.println(e.toString());
            }
            return setReceiver(listener);
        } catch (Exception e) {
            System.out.println("Could not connect to the JMS Server, will retry in 30 seconds. "
                    + e.toString());
            try {
                Thread.sleep(10000L);
            } catch (InterruptedException ie) {
                System.out.println(ie.toString());
            }
            return setReceiver(listener);

        }

    }


    @Override
    public void onException(JMSException e) {
        System.out.println("Exception handler final connection");
        cleanUp();

    }

    public void cleanUp() {
        try {
            System.out.println("cleaning up");
        } finally {
            if (queueSession != null) {
                try {
                    queueSession.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
                queueSession = null;

            }
            if (queueConn != null) {
                try {
                    queueConn.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
                queueConn = null;
            }
            if (queueReceiver != null) {
                try {
                    queueReceiver.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
                queueReceiver = null;
            }
            System.out.println("Closed connections");

        }


    }

}



