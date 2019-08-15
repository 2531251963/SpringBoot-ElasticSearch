package com.es;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @ClassName Test
 * @Description TODO
 * @Author Liyihe
 * @Date 19-4-19 下午5:29
 * @Version 1.0
 */
public class Test {
    public static TransportClient client(){
        Settings settings=Settings.builder()
                .put("cluster.name","els")
                .build();
        TransportClient client=new PreBuiltTransportClient(settings);
        try {
            client.addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"),9300));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return client;
    }

    public static void main(String[] args) {
        client();
    }
}
