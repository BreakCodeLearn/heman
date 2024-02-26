package heman.redis;

import redis.clients.jedis.Jedis;

public class HemanInsertData {

    private static final String SOURCE_DB_HOST = "172.16.22.21";
    private static final int SOURCE_DB_PORT = 10999;
    private static final String REPLICA_DB_HOST = "172.16.22.21";
    private static final int REPLICA_DB_PORT = 10998;
    private static final String REDIS_PASSWORD = "him28pass";
    private static final String KEY_NAME = "hemankey";

    public static void main(String[] args) {
        HemanInsertData redisObj = new HemanInsertData();
        redisObj.insertValuesToSourceDB();
        redisObj.printValuesFromReplicaDB();
    }

    public void insertValuesToSourceDB() {
        try (Jedis sourceJedis = createJedisConnection(SOURCE_DB_HOST, SOURCE_DB_PORT)) {
            if (sourceJedis != null) {
                sourceJedis.auth(REDIS_PASSWORD);

                // Flush the source database before inserting values
                sourceJedis.flushDB();

                // Insert values 1-100 into the source Redis database
                for (int i = 1; i <= 100; i++) {
                    sourceJedis.zadd(KEY_NAME, i, String.valueOf(i));
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to connect to source database or insert values: " + e.getMessage());
        }
    }

    public void printValuesFromReplicaDB() {
        try (Jedis replicaJedis = createJedisConnection(REPLICA_DB_HOST, REPLICA_DB_PORT)) {
            if (replicaJedis != null) {
                replicaJedis.auth(REDIS_PASSWORD);

                // Read and print values in reverse order from the replica Redis database
                System.out.println("Values retrieved from replica database (in reverse order):");
                System.out.println(replicaJedis.zrevrange(KEY_NAME, 0, -1));
                replicaJedis.close();
            }
        } catch (Exception e) {
            System.out.println("Failed to connect to replica database or retrieve values: " + e.getMessage());
        }
    }

    private Jedis createJedisConnection(String host, int port) {
        try {
            return new Jedis(host, port);
        } catch (Exception e) {
            System.out.println("Failed to connect to Redis server: " + e.getMessage());
            return null;
        }
    }
}
