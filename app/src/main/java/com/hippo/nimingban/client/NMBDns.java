/*
 * Copyright 2016 Hippo Seven
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

package com.hippo.nimingban.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Dns;

public class NMBDns implements Dns {

    private static final String DOMAIN_AC = "h.nimingban.com";
    private static final byte[] IP_AC = {(byte)60, (byte) 190, (byte) 217, (byte) 150};

    private static final List<InetAddress> DNS_AC;

    static {
        List<InetAddress> dnsAC = new ArrayList<>();
        try {
            dnsAC.add(InetAddress.getByAddress(DOMAIN_AC, IP_AC));
        } catch (UnknownHostException e) {
            dnsAC = null;
        }
        DNS_AC = dnsAC;
    }

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        if (hostname == null) {
            throw new UnknownHostException("hostname == null");
        }
        try {
            return Arrays.asList(InetAddress.getAllByName(hostname));
        } catch (UnknownHostException e) {
            if (DOMAIN_AC.equals(hostname) && null != DNS_AC) {
                return DNS_AC;
            } else {
                throw e;
            }
        }
    }
}
