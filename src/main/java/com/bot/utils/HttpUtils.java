package com.bot.utils;

import com.bot.ShardingManager;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;


public class HttpUtils {
    private static Logger logger = new Logger(HttpUtils.class.getName());
    private static Config config = Config.getInstance();
    private static Random random = new Random(System.currentTimeMillis());

    public static void postGuildCountToExternalSites() {
        ShardingManager shardingManager = ShardingManager.getInstance();
        int totalGuilds = shardingManager.getTotalGuilds();
        postBotsForDiscord(totalGuilds);
        postBotsGG(totalGuilds, shardingManager.getShards().size());
        postDiscordBotList(totalGuilds);
        postDiscordBoats(totalGuilds);
        postBotsOnDiscord(totalGuilds);
    }

    private static void postBotsOnDiscord(int serverCount) {
        String url = "https://bots.ondiscord.xyz/bot-api/bots/" + config.getConfig(Config.DISCORD_BOT_ID) + "/guilds";
        String token = config.getConfig(Config.BOTS_ON_DISCORD_API_TOKEN);
        JSONObject object = new JSONObject();
        object.put("guildCount", serverCount);

        sendPost(token, url, object);
    }

    private static void postDiscordBotList(int serverCount) {
        String url = "https://discordbotlist.com/api/bots/" + config.getConfig(Config.DISCORD_BOT_ID) + "/stats";
        String token = "Bot " + config.getConfig(Config.DISCORD_BOT_LIST_API_TOKEN);
        JSONObject object = new JSONObject();
        object.put("guilds", serverCount);

        sendPost(token, url, object);
    }

    private static void postDiscordBotsOrg() {

    }

    private static void postDiscordBoats(int count) {
        String url = "https://discord.boats/api/bot/" + config.getConfig(Config.DISCORD_BOT_ID);
        String token = config.getConfig(Config.DISCORD_BOATS_TOKEN);
        JSONObject object = new JSONObject();
        object.put("server_count", count);

        sendPost(token, url, object);
    }

    private static void postBotsGG(int serverCount, int shards) {
        String url = "https://discord.bots.gg/api/v1/bots/" + config.getConfig(Config.DISCORD_BOT_ID) + "/stats";
        String token = config.getConfig(Config.BOTS_GG_API_TOKEN);
        JSONObject object = new JSONObject();
        object.put("guildCount", serverCount);
        object.put("shardCount", shards);

        sendPost(token, url, object);
    }

    private static void postBotsForDiscord(int totalServerCount) {
        String url = "https://botsfordiscord.com/api/bot/" + config.getConfig(Config.DISCORD_BOT_ID);
        String token = config.getConfig(Config.BOTS_FOR_DISCORD_API_TOKEN);
        JSONObject object = new JSONObject();
        object.put("server_count",  totalServerCount);

        sendPost(token, url, object);
    }

    private static void sendPost(String token, String url, JSONObject body) {
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            StringEntity entity = new StringEntity(body.toString());
            HttpPost post = new HttpPost(url);
            post.addHeader("Authorization", token);
            post.addHeader("Content-type", "application/json");
            post.setEntity(entity);

            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() != 200 && response.getStatusLine().getStatusCode() != 204)
                throw new Exception("Status code not 200: " + response);
        } catch (Exception e) {
            logger.severe("Failed to post stats. url: " + url, e);
        }
    }

     //  |****************************************************|
     //  |                       4chan                        |
     //  |****************************************************|

    public static JSONObject getRandom4chanThreadFromBoard(String board) {
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            String boardUrl = "https://a.4cdn.org/" + board + "/threads.json";
            HttpGet get = new HttpGet(boardUrl);

            HttpResponse response = client.execute(get);
            // Convert response into a json array
            String json = IOUtils.toString(response.getEntity().getContent());
            JSONArray array = new JSONArray(json);
            // Choose a random thread in the array
            JSONObject page = array.getJSONObject(random.nextInt(array.length()));
            array = page.getJSONArray("threads");
            JSONObject thread = array.getJSONObject(random.nextInt(array.length()));

            return getInfoForThread(thread.getLong("no"), board);
        } catch (Exception e) {
            return null;
        }
    }

    public static JSONObject getInfoForThread(long id, String board) {
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            String threadUrl = "http://a.4cdn.org/" + board + "/thread/" + id + ".json";
            HttpGet get = new HttpGet(threadUrl);
            HttpResponse response = client.execute(get);
            // Convert response into a json array
            String json = IOUtils.toString(response.getEntity().getContent());
            JSONObject thread = new JSONObject(json);
            JSONArray array = thread.getJSONArray("posts");

            return array.getJSONObject(0);
        } catch (Exception e) {
            return null;
        }
    }
}