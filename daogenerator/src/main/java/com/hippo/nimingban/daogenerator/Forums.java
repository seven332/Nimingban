package com.hippo.nimingban.daogenerator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Forums {

    public static void main(String args[]) throws Exception {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url("http://adnmb1.com/Api/getForumList").build();
        Response response = client.newCall(request).execute();

        Gson gson = new Gson();
        JsonArray ja = gson.fromJson(response.body().charStream(), JsonArray.class);

        List<String> names = new ArrayList<>();
        List<String> ids = new ArrayList<>();

        ja.forEach(group -> ((JsonObject) group).getAsJsonArray("forums").forEach(forum -> {
            JsonObject jo = (JsonObject) forum;
            String showName = jo.has("showName") ? jo.get("showName").getAsString() : null;
            String name = jo.get("name").getAsString();

            names.add(showName == null || showName.isEmpty() ? name : showName);
            ids.add(jo.get("id").getAsString());
        }));

        print(ids);
        print(names);
    }

    private static void print(List<String> list) {
        System.out.print("{");
        list.forEach(it -> {
            System.out.print("\"");
            System.out.print(it.replace("\"", "\\\""));
            System.out.print("\", ");
        });
        System.out.print("}");
        System.out.println("");
    }
}
