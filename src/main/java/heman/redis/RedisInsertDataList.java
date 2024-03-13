package heman.redis;

import redis.clients.jedis.Jedis;
import java.util.List;

public class RedisInsertDataList {

    private static final String SOURCE_DB_HOST = "172.16.22.21";
    private static final int SOURCE_DB_PORT = 10999;
    private static final String REPLICA_DB_HOST = "172.16.22.21";
    private static final int REPLICA_DB_PORT = 10998;
    private static final String REDIS_PASSWORD = "him28pass";
    private static final String KEY_NAME = "hemankeylist";

    public static void main(String[] args) {
        RedisInsertDataList redisObj = new RedisInsertDataList();
        redisObj.insertValuesToSourceDB();
        redisObj.printValuesFromReplicaDB();
    }

    public void insertValuesToSourceDB() {
        try (Jedis sourceJedis = createJedisConnection(SOURCE_DB_HOST, SOURCE_DB_PORT)) {
            if (sourceJedis != null) {
                sourceJedis.auth(REDIS_PASSWORD);
                
                // Delete the key if it already exists
                if (sourceJedis.exists(KEY_NAME)) {
                    sourceJedis.del(KEY_NAME);
                }

                for (int i = 1; i <= 100; i++) {
                    sourceJedis.lpush(KEY_NAME, String.valueOf(i)); // Use LPUSH for inserting into list
                }
                // Print values from the 'source-db'
                System.out.println("\n");
                System.out.println("Print the values from 'source-db'");
                List<String> values = sourceJedis.lrange(KEY_NAME, 0, -1); // Get values using LRANGE
                for (String value : values) {
                    System.out.println(value);
                }
                System.out.println("\n");
            }
        } catch (Exception e) {
            System.out.println("Failed to connect to source database or insert values: " + e.getMessage());
        }
    }

    public void printValuesFromReplicaDB() {
        try (Jedis replicaJedis = createJedisConnection(REPLICA_DB_HOST, REPLICA_DB_PORT)) {
            if (replicaJedis != null) {
                replicaJedis.auth(REDIS_PASSWORD);

                // Read and print values from the replica Redis database
                System.out.println("\n");
                System.out.println("Print the values retrieved from 'replica-db' in REVERSE order");
                List<String> values = replicaJedis.lrange(KEY_NAME, 0, -1); // Get values using LRANGE
                for (int i = values.size() - 1; i >= 0; i--) {
                    System.out.println(values.get(i));
                }
                System.out.println("\n");

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
