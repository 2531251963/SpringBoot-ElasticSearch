package com.es;

import redis.clients.jedis.Jedis;

import java.util.Set;

public class Redis {
    public static void main(String[] args) {
        Jedis jedis=new Jedis("127.0.0.1",6379);//连接redis客户端
        jedis.set("a","a");//创建key value对
        System.out.println(jedis.get("a"));//获取key的value
        Set<String> set=jedis.keys("*");/*获取所有key*/
        for (String key: set) {
            System.out.println(key);
        }
        jedis.del("a");//删除key 返回值1成功 0失败
        jedis.exists("key");//判断key是否存在
        jedis.expire("a",10);//设置key 的过期时间 单位s
        System.out.println(jedis.ttl("a"));//查看key的剩余时间
    }
}
