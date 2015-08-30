/*
 * Copyright 2015 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.nimingban;

import java.io.File;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class HttpCookieDaoGenerator {

    private static final String PACKAGE = "com.hippo.network.dao";
    private static final String OUT_DIR = "./app/src/main/java-gen";
    private static final String DELETE_DIR = "./app/src/main/java-gen/com/hippo/network/dao";

    public static void generate() throws Exception {
        Utilities.deleteContents(new File(DELETE_DIR));
        File outDir = new File(OUT_DIR);
        outDir.delete();
        outDir.mkdirs();

        Schema schema = new Schema(1, PACKAGE);
        addHttpCookie(schema);
        new DaoGenerator().generateAll(schema, OUT_DIR);
    }

    private static void addHttpCookie(Schema schema) {
        Entity entity = schema.addEntity("HttpCookieRaw");
        entity.setTableName("HTTP_COOKIE");
        entity.setClassNameDao("HttpCookieDao");
        entity.addIdProperty();
        entity.addStringProperty("name");
        entity.addStringProperty("value");
        entity.addStringProperty("comment");
        entity.addStringProperty("commentURL");
        entity.addBooleanProperty("discard");
        entity.addStringProperty("domain");
        entity.addLongProperty("maxAge");
        entity.addStringProperty("path");
        entity.addStringProperty("portList");
        entity.addBooleanProperty("secure");
        entity.addIntProperty("version");
        entity.addStringProperty("url");
        entity.addLongProperty("whenCreated");
    }
}
