/*
 * Copyright 2011 Sebastian Köhler <sebkoehler@whoami.org.uk>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.org.whoami.geoip.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.zip.GZIPInputStream;

public class Updater {

    public static void update(Settings settings) throws MalformedURLException {
        if(settings.getCityDatabasePath().equals(settings.CITYDATABASEPATH)) {
            URL url = new URL(settings.getCityDatabaseURL());
            File file = new File(settings.CITYDATABASEPATH);
            updateFile(url, file, settings.getLastUpdated());
            ConsoleLogger.info(settings.CITYDATABASEPATH + " updated");
        }

        if(settings.getCountryDatabasePath().equals(settings.COUNTRYDATABASEPATH)) {
            URL url = new URL(settings.getCountryDatabaseURL());
            File file = new File(settings.COUNTRYDATABASEPATH);
            updateFile(url, file, settings.getLastUpdated());
            ConsoleLogger.info(settings.COUNTRYDATABASEPATH + " updated");
        }

        if(settings.getIPv6DatabasePath().equals(settings.IPV6DATABASEBATH)) {
            URL url = new URL(settings.getIPv6DatabaseURL());
            File file = new File(settings.IPV6DATABASEBATH);
            updateFile(url, file, settings.getLastUpdated());
            ConsoleLogger.info(settings.IPV6DATABASEBATH + " updated");
        }
        settings.setLastUpdated(new Date().getTime());
        settings.write();
    }

    private static void updateFile(URL url, File file, long lastUpdated) {
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setReadTimeout(10000);
            con.setConnectTimeout(10000);
            con.setIfModifiedSince(lastUpdated);
            con.connect();

            if(con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try(BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                    GZIPInputStream in = new GZIPInputStream(con.getInputStream());
                ) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while((len = in.read(buffer, 0, buffer.length)) > -1) {
                        out.write(buffer, 0, len);
                    }
                }
            }
        } catch(IOException e) {
            ConsoleLogger.info(e.getMessage());
        } finally {
            try {
                con.disconnect();
            } catch(NullPointerException e) {
            }
        }
    }
}
